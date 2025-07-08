package com.tryiton.core.product.service;

import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.product.dto.CategoryProductGroup;
import com.tryiton.core.product.dto.MainProductGuestResponse;
import com.tryiton.core.product.dto.ProductDetailResponseDto;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.dto.ProductSummary;
import com.tryiton.core.product.dto.ProductVariantDto;
import com.tryiton.core.product.dto.TagScoreDto;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.CategoryRepository;
import com.tryiton.core.product.repository.ProductRepository;
import java.util.List;
import com.tryiton.core.product.repository.TagRepository;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
    private final CategoryRepository categoryRepository;

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
        List<Long> likedProductIds = wishlistRepository.findProductIdsByUserId(userId);
        likedProductIds.forEach(candidateIds::remove);

        if (candidateIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Product> finalCandidates = productRepository.findByIds(new ArrayList<>(candidateIds));

        return finalCandidates.stream()
            .map(product -> {
                double contentScore = product.getTags().stream()
                    .mapToDouble(tag -> tagScoreMap.getOrDefault(tag.getId(), 0.0)).sum();
                double popularityScore = product.getWishlistCount() * 0.1;
                double totalScore = contentScore + popularityScore;

                boolean liked = likedProductIds.contains(product.getId());
                return new AbstractMap.SimpleEntry<>(new ProductResponseDto(product, liked),
                    totalScore);
            })
            .sorted(Map.Entry.<ProductResponseDto, Double>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getTopRankedProducts(Long userId) {
        Set<Long> likedProductIds = new HashSet<>();

        // 비로그인 사용자인 경우 빈 Set 사용
        if (userId != null) {
            likedProductIds.addAll(wishlistRepository.findProductIdsByUserId(userId));
        }

        return productRepository.findAllByDeletedFalseOrderByWishlistCountDesc()
            .stream()
            .map(product -> new ProductResponseDto(product,
                likedProductIds.contains(product.getId())))
            .toList();
    }

    public Page<ProductResponseDto> getProductsByCategory(Long userId, Category category, int page,
        int size) {
        // 기존 생성일 기준 정렬
        /*
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Set<Long> likedProductIds = new HashSet<>();

        // 비로그인 사용자인 경우 빈 Set 사용
        if (userId != null) {
            likedProductIds.addAll(wishlistRepository.findProductIdsByUserId(userId));
        }

        return productRepository.findByCategoryHierarchyAndDeletedFalse(category.getId(), pageable)
            .map(product -> new ProductResponseDto(product,
                likedProductIds.contains(product.getId())));
        }

        private void collectAllSubCategories(Category category, List<Category> categoryList) {
            categoryList.add(category);
            for (Category child : category.getChildren()) {
                collectAllSubCategories(child, categoryList);
        }
         */

        // 페이지네이션을 유지하면서 매번 다른 순서로 보여주기 위한 시드 생성
        // 사용자별 + 시간 기반으로 시드 생성하여 일정 시간 동안은 같은 순서 유지
        int seed = generateRandomSeed(userId);
        
        Pageable pageable = PageRequest.of(page, size);
        Set<Long> likedProductIds = new HashSet<>();

        if (userId != null) {
            likedProductIds.addAll(wishlistRepository.findProductIdsByUserId(userId));
        }

        return productRepository.findRandomByCategoryWithSeed(category.getId(), seed, pageable)
            .map(product -> new ProductResponseDto(product, likedProductIds.contains(product.getId())));
    }

    // 사용자별 + 시간 기반 시드 생성 (10분마다 변경)
    private int generateRandomSeed(Long userId) {
        long currentTime = System.currentTimeMillis();
        long timeWindow = currentTime / (10 * 60 * 1000); // 10분 단위
        
        if (userId != null) {
            return (int) ((userId + timeWindow) % Integer.MAX_VALUE);
        } else {
            return (int) (timeWindow % Integer.MAX_VALUE);
        }
    }

    private void collectAllSubCategories(Category category, List<Category> categoryList) {
        categoryList.add(category);
        for (Category child : category.getChildren()) {
            collectAllSubCategories(child, categoryList);
        }
    }

    // 상품 상세 조회 (로그인/비로그인 모두 지원)
    @Transactional(readOnly = true)
    public ProductDetailResponseDto getProductDetail(Long userId, Long productId) {
        Product product = productRepository.findByIdWithCategory(productId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                "ID " + productId + "에 해당하는 상품을 찾을 수 없습니다."));

        // 비로그인 사용자인 경우 찜 상태는 false로 처리
        boolean liked = false;
        if (userId != null) {
            liked = wishlistRepository.findProductIdsByUserId(userId).contains(productId);
        }

        List<ProductVariantDto> variantDto = product.getVariants().stream()
            .map(ProductVariantDto::new)
            .toList();

        return new ProductDetailResponseDto(product, variantDto, liked);
    }

    // 비로그인 사용자용 메인 페이지 상품 조회
    public MainProductGuestResponse getMainPageProductsForGuest() {
        List<CategoryProductGroup> categoryGroups = new ArrayList<>();

        // 모든 카테고리 조회
        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            // 각 카테고리별로 8개씩 상품 조회 (인기순)
            List<Product> products = productRepository
                .findTop8ByCategoryAndDeletedFalseOrderByWishlistCountDescCreatedAtDesc(category);

            List<ProductSummary> productSummaries = products.stream()
                .map(this::convertToProductSummary)
                .collect(Collectors.toList());

            if (!productSummaries.isEmpty()) {
                categoryGroups.add(new CategoryProductGroup(
                    category.getId(),
                    category.getCategoryName(),
                    productSummaries
                ));
            }
        }

        return MainProductGuestResponse.success(categoryGroups);
    }

    private ProductSummary convertToProductSummary(Product product) {
        // 할인된 가격 계산
        int salePrice;
        if (product.getSale() > 0) {
            salePrice = (int) Math.round(product.getPrice() * (100.0 - product.getSale()) / 100.0);
        } else {
            salePrice = product.getPrice(); // 할인이 없으면 정가와 동일
        }

        return new ProductSummary(
            product.getId(),
            product.getProductName(),
            product.getBrand(),
            product.getPrice(),        // 정가
            product.getSale(),         // 할인율
            salePrice,                 // 할인된 가격
            product.getImg1(),
            product.getCategory().getCategoryName(),
            product.getWishlistCount()
        );
    }

    // 유사한 상품 목록 조회 (같은 하위 카테고리의 상품 5개 랜덤 반환)
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getSimilarProducts(Long userId, Long productId) {
        // 기준 상품 조회
        Product baseProduct = productRepository.findByIdWithCategory(productId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                "ID " + productId + "에 해당하는 상품을 찾을 수 없습니다."));

        // 사용자가 찜한 상품 목록 조회
        Set<Long> likedProductIds = new HashSet<>(
            wishlistRepository.findProductIdsByUserId(userId));

        // 같은 하위 카테고리의 상품들을 랜덤으로 조회 (기준 상품 제외, 최대 5개)
        List<Product> similarProducts = productRepository.findSimilarProductsByCategory(
            baseProduct.getCategory().getId(), productId, 5);

        return similarProducts.stream()
            .map(product -> new ProductResponseDto(product,
                likedProductIds.contains(product.getId())))
            .collect(Collectors.toList());
    }
}