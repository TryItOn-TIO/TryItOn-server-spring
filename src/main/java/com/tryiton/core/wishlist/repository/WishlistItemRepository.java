package com.tryiton.core.wishlist.repository;

import com.tryiton.core.wishlist.entity.WishlistItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findAllByWishlist_WishlistIdOrderByCreatedAtDesc(Long wishlistId);
}
