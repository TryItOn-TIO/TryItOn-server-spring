package com.tryiton.core.product.service;

import com.tryiton.core.product.dto.ProductDetailResponseDto;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.dto.ProductVariantDto;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* 나이대 별 인기 상품 구현 해야함!! */

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // [전체 탭] 추천 알고리즘이 선택한 상품들 (ID 리스트 기반 조회)
    public List<ProductResponseDto> getRecommendedProducts(List<Long> productIds) {
        return productRepository.findByIdInAndDeletedFalse(productIds).stream()
            .map(ProductResponseDto::new)
            .toList();
    }

    // 찜 수 기준 랭킹 상품 (페이징 포함)
    public Page<ProductResponseDto> getTopRankedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("wishlistCount").descending());
        return productRepository.findAllByDeletedFalseOrderByWishlistCountDesc(pageable)
            .map(ProductResponseDto::new);
    }

    /* 나이대 별 인기 상품 구현 해야하는 부분 */

    // 카테고리별(상의/아우터/바지 등) 최신 등록된 순으로 상품 정렬 (페이징 포함)
    public Page<ProductResponseDto> getProductsByCategory(Category category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByCategoryAndDeletedFalse(category, pageable)
            .map(ProductResponseDto::new);
    }

    // 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductDetailResponseDto getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다."));

        List<ProductVariantDto> variantDto = product.getVariants().stream()
            .map(ProductVariantDto::new)
            .toList();

        return new ProductDetailResponseDto(product, variantDto);
    }
}
