package com.tryiton.core.order.repository;

import com.tryiton.core.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderUid(String orderUid);
}
