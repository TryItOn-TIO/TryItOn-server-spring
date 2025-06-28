package com.tryiton.core.wishlist.repository;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.wishlist.entity.Wishlist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser(Member user);
    Optional<Wishlist> findByUserId(Long userId);




    @Query("SELECT wi.product.id FROM WishlistItem wi WHERE wi.wishlist.user.id = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);

}
