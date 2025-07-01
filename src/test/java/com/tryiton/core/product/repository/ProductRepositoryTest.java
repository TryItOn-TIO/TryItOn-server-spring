package com.tryiton.core.product.repository;

import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // JPA 관련 설정만 로드하여 테스트, 자동으로 In-Memory DB를 사용합니다.
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    // TestEntityManager는 테스트 환경에서만 사용되는 JPA EntityManager의 헬퍼 클래스입니다.
    // 영속성 컨텍스트를 직접 제어하여 테스트 데이터를 쉽게 준비하고 검증할 수 있습니다.
    @Autowired
    private TestEntityManager entityManager;

    private Category category1;
    private Category category2;
    private Product product1, product2, product3, deletedProduct;

    @BeforeEach
    void setUp() {
        // Category 데이터 준비
        category1 = new Category();
        entityManager.persist(category1);

        category2 = new Category();
        entityManager.persist(category2);

        // --- 💡 핵심 수정 부분: Builder에 필수 필드인 img1 추가 ---
        product1 = Product.builder()
            .productName("티셔츠")
            .category(category1)
            .brand("TIO")
            .price(10000)
            .img1("http://example.com/img1.jpg") // img1 값 추가
            .build();
        product1.increaseWishlistCount();
        entityManager.persist(product1);

        product2 = Product.builder()
            .productName("스웨터")
            .category(category1)
            .brand("TIO")
            .price(20000)
            .img1("http://example.com/img2.jpg") // img1 값 추가
            .build();
        product2.increaseWishlistCount();
        product2.increaseWishlistCount();
        entityManager.persist(product2);

        product3 = Product.builder()
            .productName("청바지")
            .category(category2)
            .brand("Style")
            .price(30000)
            .img1("http://example.com/img3.jpg") // img1 값 추가
            .build();
        entityManager.persist(product3);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("삭제되지 않은 상품들을 찜(wishlist) 많은 순으로 정렬하여 조회한다")
    void findAllByDeletedFalseOrderByWishlistCountDesc_ShouldReturnSortedProducts() {
        // when
        List<Product> products = productRepository.findAllByDeletedFalseOrderByWishlistCountDesc();

        // then
        assertThat(products).hasSize(3);
        assertThat(products.get(0).getProductName()).isEqualTo("스웨터"); // 찜 2개
        assertThat(products.get(1).getProductName()).isEqualTo("티셔츠"); // 찜 1개
    }

    @Test
    @DisplayName("특정 카테고리에 속하고 삭제되지 않은 상품들을 페이징하여 조회한다")
    void findByCategoryAndDeletedFalse_ShouldReturnPagedProducts() {
        // given
        Pageable pageable = PageRequest.of(0, 1); // 첫 번째 페이지, 1개 아이템

        // when
        Page<Product> productPage = productRepository.findByCategoryAndDeletedFalse(category1, pageable);

        // then
        assertThat(productPage.getTotalElements()).isEqualTo(2);
        assertThat(productPage.getContent()).hasSize(1);
        Product foundProduct = productPage.getContent().get(0);

        assertThat(foundProduct.getCategory().getId()).isEqualTo(category1.getId());
    }

    @Test
    @DisplayName("여러 ID 목록으로 삭제되지 않은 상품들을 조회한다")
    void findByIdInAndDeletedFalse_ShouldReturnMatchingProducts() {
        // given
        List<Long> idsToFind = List.of(product1.getId(), product3.getId());

        // when
        List<Product> products = productRepository.findByIdInAndDeletedFalse(idsToFind);

        // then
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getId).containsExactlyInAnyOrder(product1.getId(), product3.getId());
    }

    @Test
    @DisplayName("태그 ID 목록을 기반으로 관련 상품 ID 목록을 조회한다 (Native Query)")
    void findProductIdsByTagIds_ShouldReturnProductIds() {
        // given
        // 태그와 상품-태그 연결 데이터 추가
        Tag tag1 = Tag.builder().tagName("티셔츠").build();
        Tag tag2 = Tag.builder().tagName("스트릿").build();
        entityManager.persist(tag1);
        entityManager.persist(tag2);

        // Product 엔티티의 tags 필드에 값을 추가할 수 있는 메서드가 필요합니다.
        // 예: product1.addTag(tag1);
        // 이 예제에서는 product_tag 테이블에 직접 데이터를 삽입하는 방식으로 시뮬레이션합니다.
        entityManager.getEntityManager().createNativeQuery("INSERT INTO product_tag (product_id, tag_id) VALUES (?, ?)")
            .setParameter(1, product1.getId())
            .setParameter(2, tag1.getId())
            .executeUpdate();

        entityManager.getEntityManager().createNativeQuery("INSERT INTO product_tag (product_id, tag_id) VALUES (?, ?)")
            .setParameter(1, product2.getId())
            .setParameter(2, tag1.getId())
            .executeUpdate();

        entityManager.getEntityManager().createNativeQuery("INSERT INTO product_tag (product_id, tag_id) VALUES (?, ?)")
            .setParameter(1, product3.getId())
            .setParameter(2, tag2.getId())
            .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // when
        List<Long> productIds = productRepository.findProductIdsByTagIds(List.of(tag1.getId()));

        // then
        assertThat(productIds).hasSize(2);
        assertThat(productIds).containsExactlyInAnyOrder(product1.getId(), product2.getId());
    }
}