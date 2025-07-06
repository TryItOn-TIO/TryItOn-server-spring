package com.tryiton.core.order.service;

import com.tryiton.core.address.entity.Address;
import com.tryiton.core.address.repository.AddressRepository;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.order.dto.*;
import com.tryiton.core.order.entity.*;
import com.tryiton.core.order.repository.*;
import com.tryiton.core.product.entity.ProductVariant;
import com.tryiton.core.product.repository.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userEmail) {
        Member user = memberRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        Address address = addressRepository.findById(requestDto.getAddressId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다."));

        // 1. OrderItem 엔티티 리스트를 생성합니다.
        List<OrderItem> orderItems = requestDto.getOrderItems().stream()
                .map(itemDto -> {
                    ProductVariant variant = productVariantRepository.findById(itemDto.getVariantId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "상품 옵션을 찾을 수 없습니다."));
                    return OrderItem.builder()
                            .product(variant.getProduct())
                            .variant(variant)
                            .quantity(itemDto.getQuantity())
                            .unitPrice(variant.getPrice()) // 할인된 가격
                            .build();
                }).collect(Collectors.toList());

        // 2. 프론트엔드에서 계산한 금액을 그대로 사용합니다.
        BigDecimal totalAmount = requestDto.getAmount();

        // 3. Order 엔티티를 생성합니다.
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalAmount(totalAmount)
                .orderStatus("PENDING")
                .build();

        // 4. Order와 OrderItem의 양방향 관계를 설정합니다.
        orderItems.forEach(order::addOrderItem);

        // 5. Order를 저장합니다.
        orderRepository.save(order);

        String orderName = createOrderName(orderItems);
        return new OrderResponseDto(order, orderName);
    }

    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        // 1. 사용자 확인
        Member user = memberRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        // 2. 주문 조회
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
        
        // 3. 주문 소유자 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 주문만 취소할 수 있습니다.");
        }
        
        // 4. 주문 상태 확인 (PENDING 상태에서만 취소 가능)
        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 처리된 주문은 취소할 수 없습니다.");
        }
        
        // 5. 주문 삭제 (실제로는 상태를 CANCELLED로 변경하는 것이 좋지만, 요청에 따라 삭제)
        orderRepository.delete(order);
    }

    private String createOrderName(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) return "주문 상품 없음";
        String firstItemName = orderItems.get(0).getProduct().getProductName();
        return orderItems.size() > 1 ? firstItemName + " 외 " + (orderItems.size() - 1) + "건" : firstItemName;
    }
}