package com.tryiton.core.product.repository;

import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 전체 상품 중 찜 많은 순으로 페이징 조회 (인기 상품 후보군)
    List<Product> findAllByDeletedFalseOrderByWishlistCountDesc();

    //  성능 최적화: 상위 100개만 조회
    List<Product> findTop100ByDeletedFalseOrderByWishlistCountDesc();

    // Fetch Join을 사용하여 Product와 Category를 한번에 조회한다.
    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category c WHERE p.deleted = false " +
            "ORDER BY p.wishlistCount DESC",
            countQuery = "SELECT count(p) FROM Product p WHERE p.deleted = false") // 페이징을 위한 count 쿼리
    List<Product> findTop100WithCategory(Pageable pageable);

    // 특정 카테고리의 상품 중 페이징 조회
    Page<Product> findByCategoryAndDeletedFalse(Category category, Pageable pageable);

    Page<Product> findByCategoryInAndDeletedFalseOrderByCreateAtDesc(List<Category> categories,
        Pageable pageable);

    // 추천 알고리즘을 위해 특정 ID 리스트 기반으로 상품 조회
    List<Product> findByIdInAndDeletedFalse(List<Long> ids);

    // 여러 ID 목록으로 상품들을 한 번에 조회
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.deleted = false")
    List<Product> findByIds(@Param("ids") List<Long> ids);
    
    // 여러 ID 목록으로 상품들을 카테고리와 함께 조회 (N+1 문제 해결)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id IN :ids AND p.deleted = false")
    List<Product> findByIdsWithCategory(@Param("ids") List<Long> ids);
    
    // N+1 쿼리 해결: 상품과 태그를 함께 조회
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.category WHERE p.id IN :ids AND p.deleted = false")
    List<Product> findByIdsWithTags(@Param("ids") List<Long> ids);

    // 태그 ID 목록을 기반으로 관련 상품 ID 목록을 조회
    @Query(value = "SELECT DISTINCT product_id FROM product_tag WHERE tag_id IN :tagIds", nativeQuery = true)
    List<Long> findProductIdsByTagIds(@Param("tagIds") List<Long> tagIds);

    // 🔧 상위 카테고리와 모든 하위 카테고리의 상품을 함께 조회
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
        "(p.category.id = :categoryId OR p.category.parentCategory.id = :categoryId) " +
        "ORDER BY p.createAt DESC")
    Page<Product> findByCategoryHierarchyAndDeletedFalse(@Param("categoryId") Long categoryId, Pageable pageable);
    
    // 시드 기반 랜덤 정렬로 페이지네이션 지원
    @Query(value = "SELECT * FROM product WHERE deleted = false AND category_id IN " +
        "(SELECT category_id FROM category WHERE category_id = :categoryId OR parent_category_id = :categoryId) " +
        "ORDER BY RAND(:seed)",
        countQuery = "SELECT count(*) FROM product WHERE deleted = false AND category_id IN " +
        "(SELECT category_id FROM category WHERE category_id = :categoryId OR parent_category_id = :categoryId)",
        nativeQuery = true)
    Page<Product> findRandomByCategoryWithSeed(@Param("categoryId") Long categoryId, 
                                             @Param("seed") int seed, 
                                             Pageable pageable);

    // 사용자가 이미 구매한 상품 ID 목록 조회
    @Query(value = "SELECT DISTINCT p.product_id FROM orders o JOIN order_item oi ON o.order_id = oi.order_id JOIN product_variant pv ON oi.variant_id = pv.variant_id JOIN product p ON pv.product_id = p.product_id WHERE o.user_id = :userId", nativeQuery = true)
    List<Long> findPurchasedProductIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id AND p.deleted = false")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);
    
    // N+1 쿼리 해결: 상품과 카테고리, variants를 함께 조회
    @Query("SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE p.id = :id AND p.deleted = false")
    Optional<Product> findByIdWithCategoryAndVariants(@Param("id") Long id);

    // 카테고리별 최신 8개 상품 조회 (비로그인 사용자용)
    List<Product> findTop8ByCategoryAndDeletedFalseOrderByCreateAtDesc(Category category);

    // 카테고리별 인기순 8개 상품 조회 (찜 개수 기준)
    List<Product> findTop8ByCategoryAndDeletedFalseOrderByWishlistCountDescCreateAtDesc(Category category);

    // 같은 하위 카테고리의 유사한 상품 조회 (기준 상품 제외, 랜덤 정렬)
    @Query(value = "SELECT * FROM product p WHERE p.category_id = :categoryId AND p.product_id != :excludeProductId AND p.deleted = false ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Product> findSimilarProductsByCategory(@Param("categoryId") Long categoryId, @Param("excludeProductId") Long excludeProductId, @Param("limit") int limit);

    // 자동완성 추천 키워드 (6개 제한)
    @Query("SELECT DISTINCT p.productName FROM Product p WHERE p.productName LIKE %:query% OR p.brand LIKE %:query%")
    List<String> findSuggestionsByProductNameOrBrand(@Param("query") String query, Pageable pageable);

    // 검색 (상품명 or 브랜드)
    Page<Product> findByProductNameContainingOrBrandContaining(
        String productName, String brand, Pageable pageable
    );

    // 각 카테고리별 상위 4개 상품 조회
    @Query(value = """
            WITH RankedProducts AS (
                SELECT p.*,
                       ROW_NUMBER() OVER (PARTITION BY p.category_id
                                        ORDER BY p.wishlist_count DESC, p.create_at DESC) as rn
                FROM product p
                WHERE p.deleted = false
            )
            SELECT * FROM RankedProducts
            WHERE rn <= 4
            ORDER BY category_id, wishlist_count DESC, create_at DESC
            """, nativeQuery = true)
    List<Product> findTop4ProductsPerCategory();
}