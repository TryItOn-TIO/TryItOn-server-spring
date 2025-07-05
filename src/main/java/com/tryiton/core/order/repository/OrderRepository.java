package com.tryiton.core.order.repository;

import com.tryiton.core.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderUid(String orderUid);
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product p WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findOrderHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
}
