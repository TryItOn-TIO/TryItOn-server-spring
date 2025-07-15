package com.tryiton.core.wishlist.repository;

import com.tryiton.core.wishlist.entity.WishlistItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findAllByWishlist_WishlistIdOrderByCreatedAtDesc(Long wishlistId);
    
    // N+1 쿼리 해결: WishlistItem과 Product를 함께 조회
    @Query("SELECT wi FROM WishlistItem wi JOIN FETCH wi.product WHERE wi.wishlist.wishlistId = :wishlistId ORDER BY wi.createdAt DESC")
    List<WishlistItem> findAllByWishlistIdWithProductOrderByCreatedAtDesc(@Param("wishlistId") Long wishlistId);
}
