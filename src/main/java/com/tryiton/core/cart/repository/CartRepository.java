package com.tryiton.core.cart.repository;

import com.tryiton.core.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMemberId(Long memberId);
    
    // N+1 쿼리 해결: CartItem과 연관된 Product, Variant를 한 번에 조회
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.variant v " +
           "LEFT JOIN FETCH v.product p " +
           "WHERE c.member.id = :memberId")
    Optional<Cart> findByMemberIdWithItems(@Param("memberId") Long memberId);
}
