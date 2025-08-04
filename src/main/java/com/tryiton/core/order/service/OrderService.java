package com.tryiton.core.order.service;

import com.tryiton.core.address.entity.Address;
import com.tryiton.core.address.repository.AddressRepository;
import com.tryiton.core.common.enums.RecommendAction;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.order.dto.*;
import com.tryiton.core.order.entity.*;
import com.tryiton.core.order.repository.*;
import com.tryiton.core.product.entity.ProductVariant;
import com.tryiton.core.product.repository.ProductVariantRepository;
import com.tryiton.core.recommend.service.RecommendBehaviorLogService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;

    private final RecommendBehaviorLogService recommendBehaviorLogService;
    
    private static final int MAX_RETRY_COUNT = 3; // 최대 재시도 횟수

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userEmail) {
        log.info("주문 생성 시작 - 사용자: {}, 주문 아이템 수: {}", userEmail, requestDto.getOrderItems().size());
        
        Member user = memberRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        Address address = addressRepository.findById(requestDto.getAddressId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다."));

        // 주소 소유자 확인
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 주소만 사용할 수 있습니다.");
        }

        // 주문 아이템 검증
        if (requestDto.getOrderItems() == null || requestDto.getOrderItems().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "주문할 상품이 없습니다.");
        }
        
        // 각 주문 아이템의 null 체크
        for (OrderRequestDto.OrderItemRequest item : requestDto.getOrderItems()) {
            if (item.getVariantId() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "상품 옵션 ID가 누락되었습니다.");
            }
            if (item.getQuantity() <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "주문 수량은 1개 이상이어야 합니다.");
            }
        }

        // 낙관적 락을 고려한 재시도 로직으로 주문 처리
        return createOrderWithRetry(requestDto, user, address, 0);
    }
    
    private OrderResponseDto createOrderWithRetry(OrderRequestDto requestDto, Member user, Address address, int retryCount) {
        try {
            return processOrder(requestDto, user, address);
        } catch (OptimisticLockException e) {
            if (retryCount < MAX_RETRY_COUNT) {
                log.warn("낙관적 락 충돌 발생, 재시도 중... (시도 횟수: {}/{})", retryCount + 1, MAX_RETRY_COUNT);
                // 짧은 대기 후 재시도
                try {
                    Thread.sleep(100 * (retryCount + 1)); // 점진적 백오프
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "주문 처리 중 오류가 발생했습니다.");
                }
                return createOrderWithRetry(requestDto, user, address, retryCount + 1);
            } else {
                log.error("낙관적 락 재시도 횟수 초과 - 사용자: {}", user.getEmail());
                throw new BusinessException(HttpStatus.CONFLICT, "동시에 많은 주문이 발생하여 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }
        }
    }
    
    private OrderResponseDto processOrder(OrderRequestDto requestDto, Member user, Address address) {
        //  N+1 쿼리 해결: 모든 variant를 한 번에 조회
        List<Long> variantIds = requestDto.getOrderItems().stream()
            .map(OrderRequestDto.OrderItemRequest::getVariantId)
            .toList();
        
        List<ProductVariant> variants = productVariantRepository.findAllByIdInWithProduct(variantIds);
        
        // variant ID를 키로 하는 Map 생성
        Map<Long, ProductVariant> variantMap = variants.stream()
            .collect(Collectors.toMap(ProductVariant::getVariantId, v -> v));

        // 1. OrderItem 엔티티 리스트를 생성하고 백엔드에서 금액을 재계산합니다.
        List<OrderItem> orderItems = requestDto.getOrderItems().stream()
                .map(itemDto -> {
                    ProductVariant variant = variantMap.get(itemDto.getVariantId());
                    if (variant == null) {
                        throw new BusinessException(HttpStatus.NOT_FOUND, "상품 옵션을 찾을 수 없습니다.");
                    }
                    
                    // 재고 확인
                    if (variant.getStock() < itemDto.getQuantity()) {
                        throw new BusinessException(HttpStatus.BAD_REQUEST, 
                            "상품 '" + variant.getProduct().getProductName() + "'의 재고가 부족합니다. (요청: " + itemDto.getQuantity() + ", 재고: " + variant.getStock() + ")");
                    }
                    
                    return OrderItem.builder()
                            .product(variant.getProduct()) // 이미 fetch join으로 로딩됨
                            .variant(variant)
                            .quantity(itemDto.getQuantity())
                            .unitPrice(variant.getPrice()) // 백엔드에서 실제 가격 사용
                            .build();
                }).collect(Collectors.toList());

        // 2. 백엔드에서 실제 총 금액을 계산합니다.
        BigDecimal calculatedTotalAmount = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 프론트엔드에서 전송한 금액과 백엔드에서 계산한 금액을 비교 검증합니다.
        if (requestDto.getAmount() != null && requestDto.getAmount().compareTo(calculatedTotalAmount) != 0) {
            log.warn("주문 금액 불일치 - 사용자: {}, 요청 금액: {}, 계산된 금액: {}", 
                user.getEmail(), requestDto.getAmount(), calculatedTotalAmount);
            throw new BusinessException(HttpStatus.BAD_REQUEST, 
                "주문 금액이 일치하지 않습니다. (요청 금액: " + requestDto.getAmount() + ", 계산된 금액: " + calculatedTotalAmount + ")");
        }
        
        BigDecimal totalAmount = calculatedTotalAmount;

        // 4. Order 엔티티를 생성합니다.
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalAmount(totalAmount)
                .orderStatus("PENDING")
                .build();

        // 5. Order와 OrderItem의 양방향 관계를 설정합니다.
        orderItems.forEach(order::addOrderItem);

        // 6. Order를 저장합니다.
        orderRepository.save(order);
        
        // 7. 주문 성공 시 재고를 차감합니다. (낙관적 락이 여기서 발생할 수 있음)
        orderItems.forEach(orderItem -> {
            try {
                ProductVariant variant = orderItem.getVariant();
                variant.decreaseStock(orderItem.getQuantity());
                productVariantRepository.save(variant); // 여기서 OptimisticLockException 발생 가능
            } catch (IllegalArgumentException e) {
                log.error("재고 차감 실패 - 상품: {}, 오류: {}", 
                    orderItem.getProduct().getProductName(), e.getMessage());
                throw new BusinessException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        });

        String orderName = createOrderName(orderItems);

        // 유저 행동 로그 비동기 기록
        for (OrderItem orderItem: order.getOrderItems()){
            Long productId = orderItem.getProduct().getId();
            recommendBehaviorLogService.logUserAction(user.getId(), productId, RecommendAction.BUY);
        }

        log.info("주문 생성 완료 - 주문 ID: {}, 총 금액: {}", order.getId(), totalAmount);
        return new OrderResponseDto(order, orderName);
    }

    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        log.info("주문 취소 시작 - 주문 ID: {}, 사용자: {}", orderId, userEmail);
        
        // 1. 사용자 확인
        Member user = memberRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        // 낙관적 락을 고려한 재시도 로직으로 주문 취소 처리
        cancelOrderWithRetry(orderId, user, 0);
    }
    
    private void cancelOrderWithRetry(Long orderId, Member user, int retryCount) {
        try {
            processCancelOrder(orderId, user);
        } catch (OptimisticLockException e) {
            if (retryCount < MAX_RETRY_COUNT) {
                log.warn("주문 취소 중 낙관적 락 충돌 발생, 재시도 중... (시도 횟수: {}/{})", retryCount + 1, MAX_RETRY_COUNT);
                // 짧은 대기 후 재시도
                try {
                    Thread.sleep(100 * (retryCount + 1)); // 점진적 백오프
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "주문 취소 처리 중 오류가 발생했습니다.");
                }
                cancelOrderWithRetry(orderId, user, retryCount + 1);
            } else {
                log.error("주문 취소 중 낙관적 락 재시도 횟수 초과 - 주문 ID: {}, 사용자: {}", orderId, user.getEmail());
                throw new BusinessException(HttpStatus.CONFLICT, "동시에 많은 요청이 발생하여 주문을 취소할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }
        }
    }
    
    private void processCancelOrder(Long orderId, Member user) {
        // 2. 주문 조회 (OrderItem도 함께 조회)
        Order order = orderRepository.findByIdWithOrderItems(orderId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
        
        // 3. 주문 소유자 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 주문만 취소할 수 있습니다.");
        }
        
        // 4. 주문 상태 확인 (PENDING 상태에서만 취소 가능)
        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 처리된 주문은 취소할 수 없습니다.");
        }
        
        // 5. 재고 복원 (낙관적 락이 여기서 발생할 수 있음)
        order.getOrderItems().forEach(orderItem -> {
            try {
                ProductVariant variant = orderItem.getVariant();
                variant.increaseStock(orderItem.getQuantity());
                productVariantRepository.save(variant); // 여기서 OptimisticLockException 발생 가능
                log.debug("재고 복원 - 상품: {}, 수량: {}", variant.getProduct().getProductName(), orderItem.getQuantity());
            } catch (IllegalArgumentException e) {
                log.error("재고 복원 실패 - 상품: {}, 오류: {}", 
                    orderItem.getProduct().getProductName(), e.getMessage());
                // 재고 복원 실패는 로그만 남기고 주문 취소는 계속 진행
            }
        });
        
        // 6. 주문 삭제 (실제로는 상태를 CANCELLED로 변경하는 것이 좋지만, 요청에 따라 삭제)
        orderRepository.delete(order);
        
        log.info("주문 취소 완료 - 주문 ID: {}", orderId);
    }

    private String createOrderName(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) return "주문 상품 없음";
        String firstItemName = orderItems.get(0).getProduct().getProductName();
        return orderItems.size() > 1 ? firstItemName + " 외 " + (orderItems.size() - 1) + "건" : firstItemName;
    }
}