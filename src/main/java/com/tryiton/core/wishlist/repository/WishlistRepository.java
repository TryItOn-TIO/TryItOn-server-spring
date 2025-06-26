package com.tryiton.core.wishlist.repository;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.wishlist.entity.Wishlist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUser(Member user);
    Optional<Wishlist> findByUserId(Long userId);
}
