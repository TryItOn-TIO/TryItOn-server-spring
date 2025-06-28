package com.tryiton.core.product.service;

import com.tryiton.core.product.dto.ProductDetailResponseDto;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.dto.ProductVariantDto;
import com.tryiton.core.product.dto.TagScoreDto;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import java.util.List;
import java.util.NoSuchElementException;
import com.tryiton.core.product.repository.TagRepository;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* 나이대 별 인기 상품 구현 해야함!! */
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final WishlistRepository wishlistRepository;

    public List<ProductResponseDto> getPersonalizedRecommendations(Long userId) {
        List<TagScoreDto> favoriteTags = tagRepository.findUserFavoriteTags(userId);
        Map<Long, Double> tagScoreMap = favoriteTags.stream()
            .collect(Collectors.toMap(TagScoreDto::getTagId, TagScoreDto::getScore));
        Set<Long> candidateIds = new HashSet<>();
        if (!favoriteTags.isEmpty()) {
            candidateIds.addAll(
                productRepository.findProductIdsByTagIds(new ArrayList<>(tagScoreMap.keySet())));
        }

        productRepository.findPurchasedProductIdsByUserId(userId).forEach(candidateIds::remove);
        wishlistRepository.findProductIdsByUserId(userId).forEach(candidateIds::remove);

        if (candidateIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Product> finalCandidates = productRepository.findByIds(new ArrayList<>(candidateIds));

        return finalCandidates.stream()
            .map(product -> {
                double contentScore = product.getTags().stream()
                    .mapToDouble(tag -> tagScoreMap.getOrDefault(tag.getId(), 0.0)).sum();
                double popularityScore = product.getWishlistCount() * 0.1;
                return new AbstractMap.SimpleEntry<>(product, contentScore + popularityScore);
            })
            .sorted(Map.Entry.<Product, Double>comparingByValue().reversed())
            .limit(10)
            .map(entry -> new ProductResponseDto(entry.getKey()))
            .collect(Collectors.toList());
    }

    public Page<ProductResponseDto> getTopRankedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAllByDeletedFalseOrderByWishlistCountDesc(pageable)
            .map(ProductResponseDto::new);
    }

    public Page<ProductResponseDto> getProductsByCategory(Category category, int page, int size) {
        List<Category> categoriesToSearch = new ArrayList<>();
        collectAllSubCategories(category, categoriesToSearch);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByCategoryInAndDeletedFalseOrderByCreatedAtDesc(
            categoriesToSearch, pageable).map(ProductResponseDto::new);
    }

    private void collectAllSubCategories(Category category, List<Category> categoryList) {
        categoryList.add(category);
        for (Category child : category.getChildren()) {
            collectAllSubCategories(child, categoryList);
        }
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