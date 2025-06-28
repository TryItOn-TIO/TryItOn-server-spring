package com.tryiton.core.product.repository;

import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // --- 이 아래 메소드들을 기존 파일에 추가하거나, 있는 것은 그대로 사용합니다 ---

    // 전체 상품 중 찜 많은 순으로 페이징 조회 (인기 상품 후보군)
    Page<Product> findAllByDeletedFalseOrderByWishlistCountDesc(Pageable pageable);

    // 특정 카테고리의 상품 중 페이징 조회
    Page<Product> findByCategoryAndDeletedFalse(Category category, Pageable pageable);

    Page<Product> findByCategoryInAndDeletedFalseOrderByCreatedAtDesc(List<Category> categories, Pageable pageable);

    // 추천 알고리즘을 위해 특정 ID 리스트 기반으로 상품 조회
    List<Product> findByIdInAndDeletedFalse(List<Long> ids);

    // 여러 ID 목록으로 상품들을 한 번에 조회
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.deleted = false")
    List<Product> findByIds(@Param("ids") List<Long> ids);

    // 태그 ID 목록을 기반으로 관련 상품 ID 목록을 조회
    @Query(value = "SELECT DISTINCT product_id FROM product_tag WHERE tag_id IN :tagIds", nativeQuery = true)
    List<Long> findProductIdsByTagIds(@Param("tagIds") List<Long> tagIds);

    // 사용자가 이미 구매한 상품 ID 목록 조회
    @Query(value = "SELECT DISTINCT p.product_id FROM orders o JOIN order_item oi ON o.order_id = oi.order_id JOIN product_variant pv ON oi.variant_id = pv.variant_id JOIN product p ON pv.product_id = p.product_id WHERE o.user_id = :userId", nativeQuery = true)
    List<Long> findPurchasedProductIdsByUserId(@Param("userId") Long userId);
}