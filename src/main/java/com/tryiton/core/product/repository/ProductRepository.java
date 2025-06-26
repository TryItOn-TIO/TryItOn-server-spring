package com.tryiton.core.product.repository;

import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 전체 상품 중 찜 많은 순으로 페이징 조회
    Page<Product> findAllByDeletedFalseOrderByWishlistCountDesc(Pageable pageable);

    // 특정 카테고리의 상품 중 페이징 조회
    Page<Product> findByCategoryAndDeletedFalse(Category category, Pageable pageable);

    // 추천 알고리즘 상품: 특정 ID 리스트 기반 조회
    List<Product> findByIdInAndDeletedFalse(List<Long> ids);
}
