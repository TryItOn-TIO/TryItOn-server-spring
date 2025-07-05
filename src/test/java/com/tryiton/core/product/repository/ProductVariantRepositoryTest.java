package com.tryiton.core.product.repository;

import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.entity.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

//@ActiveProfiles("test")
//@DataJpaTest
//class ProductVariantRepositoryTest {
//
//    @Autowired
//    private ProductVariantRepository productVariantRepository;
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    private Product testProduct;
//    private ProductVariant variant1, variant2, variant3;
//
//    @BeforeEach
//    void setUp() {
//        // given: 테스트 데이터 준비
//        Category category = new Category();
//        entityManager.persist(category);
//
//        testProduct = Product.builder()
//            .productName("테스트 티셔츠")
//            .brand("TIO")
//            .category(category)
//            .img1("http://example.com/image.jpg")
//            .price(15000)
//            .build();
//        entityManager.persist(testProduct);
//
//        // 상품 옵션(variant) 데이터 준비
//        variant1 = ProductVariant.builder()
//            .product(testProduct).size("M").color("Black").quantity(10)
//            .build();
//        entityManager.persist(variant1);
//
//        variant2 = ProductVariant.builder()
//            .product(testProduct).size("L").color("Black").quantity(5)
//            .build();
//        entityManager.persist(variant2);
//
//        variant3 = ProductVariant.builder()
//            .product(testProduct).size("M").color("White").quantity(20)
//            .build();
//        entityManager.persist(variant3);
//
//        entityManager.flush();
//        entityManager.clear();
//    }
//
//    @Test
//    @DisplayName("특정 상품(Product)으로 모든 옵션(Variant)들을 조회해야 한다")
//    void findByProduct_ShouldReturnAllVariantsForProduct() {
//        // when
//        // findByProduct는 product 객체를 받으므로, ID로 다시 조회해서 사용합니다.
//        Product persistedProduct = entityManager.find(Product.class, testProduct.getId());
//        List<ProductVariant> variants = productVariantRepository.findByProduct(persistedProduct);
//
//        // then
//        assertThat(variants).hasSize(3);
//        assertThat(variants).extracting(ProductVariant::getSize).contains("M", "L", "M");
//        assertThat(variants).extracting(ProductVariant::getColor).contains("Black", "White");
//    }
//
//    @Test
//    @DisplayName("상품(Product), 사이즈, 색상으로 정확한 옵션(Variant) 하나를 조회해야 한다")
//    void findByProductAndSizeAndColor_ShouldReturnCorrectVariant() {
//        // when
//        Product persistedProduct = entityManager.find(Product.class, testProduct.getId());
//        ProductVariant foundVariant = productVariantRepository.findByProductAndSizeAndColor(
//            persistedProduct, "M", "White"
//        );
//
//        // then
//        assertThat(foundVariant).isNotNull();
//        assertThat(foundVariant.getVariantId()).isEqualTo(variant3.getVariantId());
//        assertThat(foundVariant.getSize()).isEqualTo("M");
//        assertThat(foundVariant.getColor()).isEqualTo("White");
//        assertThat(foundVariant.getQuantity()).isEqualTo(20);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 옵션으로 조회 시, null을 반환해야 한다")
//    void findByProductAndSizeAndColor_WhenVariantDoesNotExist_ShouldReturnNull() {
//        // when
//        Product persistedProduct = entityManager.find(Product.class, testProduct.getId());
//        ProductVariant foundVariant = productVariantRepository.findByProductAndSizeAndColor(
//            persistedProduct, "S", "Blue" // 존재하지 않는 사이즈와 색상
//        );
//
//        // then
//        assertThat(foundVariant).isNull();
//    }
//}