package com.tryiton.core.wishlist.repository;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.wishlist.entity.WishlistItem;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    // 유저가 찜한 상품인지 확인
    Optional<WishlistItem> findByMemberAndProduct(Member member, Product product);

    // 유저가 찜한 모든 상품 조회
    List<WishlistItem> findAllByMember(Member member);
}
