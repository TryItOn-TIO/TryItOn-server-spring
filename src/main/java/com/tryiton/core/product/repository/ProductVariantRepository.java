package com.tryiton.core.product.repository;

import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.entity.ProductVariant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // 특정 상품의 모든 옵션(사이즈/색상) 조회
    List<ProductVariant> findByProduct(Product product);

    // 특정 상품의 특정 옵션(사이즈, 컬러) 1개를 조회하고 싶을 때
    // 장바구니 추가 또는 재고 확인을 위해 정확한 옵션 1개를 찾아야하기 때문에
    ProductVariant findByProductAndSizeAndColor(Product product, String size, String color);
}
