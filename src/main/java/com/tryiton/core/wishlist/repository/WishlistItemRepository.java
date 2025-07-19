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
    List<WishlistItem> findAllByWishlistIdWithProductOrderByCreatedAtDesc(
        @Param("wishlistId") Long wishlistId);

    // Product -> Category -> parentCategory.id로 타고 올라가서 필터링
    @Query("""
            SELECT wi FROM WishlistItem wi
            JOIN FETCH wi.product p
            JOIN p.category c
            WHERE wi.wishlist.wishlistId = :wishlistId
            AND c.parentCategory.id = :parentCategoryId
            ORDER BY wi.createdAt DESC
        """)
    List<WishlistItem> findByWishlistIdAndProductParentCategoryId(
        @Param("wishlistId") Long wishlistId,
        @Param("parentCategoryId") Long parentCategoryId
    );

    // 쿼리 최적화: userId로 직접 조회하여 DB 왕복 1회로 줄임
    @Query("""
            SELECT wi FROM WishlistItem wi
            JOIN FETCH wi.product p
            JOIN p.category c
            WHERE wi.wishlist.user.id = :userId
            AND c.parentCategory.id = :parentCategoryId
            ORDER BY wi.createdAt DESC
        """)
    List<WishlistItem> findByUserIdAndProductParentCategoryIdWithProduct(
        @Param("userId") Long userId,
        @Param("parentCategoryId") Long parentCategoryId
    );
}
