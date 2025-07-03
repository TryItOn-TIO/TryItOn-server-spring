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
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        Member user = memberRepository.findById(requestDto.getUserId()).orElseThrow(() -> new BusinessException(
            HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        Address address = addressRepository.findById(requestDto.getAddressId()).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다."));

        // 1. OrderItem 엔티티 리스트를 먼저 생성합니다.
        List<OrderItem> orderItems = requestDto.getOrderItems().stream()
                .map(itemDto -> {
                    ProductVariant variant = productVariantRepository.findById(itemDto.getVariantId()).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "상품 옵션을 찾을 수 없습니다."));
                    return OrderItem.builder()
                            .product(variant.getProduct())
                            .variant(variant)
                            .quantity(itemDto.getQuantity())
                            .unitPrice(variant.getPrice())
                            .build();
                }).collect(Collectors.toList());

        // 2. 생성된 OrderItem 리스트로 총액을 계산합니다.
        BigDecimal totalAmount = calculateTotalAmount(orderItems);

        // 3. Order 엔티티를 생성합니다.
        Order order = Order.builder().user(user).address(address).totalAmount(totalAmount).orderStatus("PENDING").build();

        // 4. Order와 OrderItem의 양방향 관계를 설정합니다. 이 과정에서 orderItem.setOrder(order)가 호출됩니다.
        orderItems.forEach(order::addOrderItem);

        // 5. Order를 저장합니다. Cascade 설정 덕분에 OrderItem도 함께 저장됩니다.
        orderRepository.save(order);

        String orderName = createOrderName(orderItems);
        return new OrderResponseDto(order, orderName);
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String createOrderName(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) return "주문 상품 없음";
        String firstItemName = orderItems.get(0).getProduct().getProductName();
        return orderItems.size() > 1 ? firstItemName + " 외 " + (orderItems.size() - 1) + "건" : firstItemName;
    }
    
    @Transactional
    public OrderResponseDto createSimpleOrder(SimpleOrderRequestDto request) {
        Member user = memberRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다."));

        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalAmount(request.getAmount())
                .orderStatus("PENDING")
                .build();

        orderRepository.save(order);
        return new OrderResponseDto(order, request.getOrderName());
    }
}