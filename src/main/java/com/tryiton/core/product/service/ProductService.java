package com.tryiton.core.product.service;

import com.tryiton.core.common.enums.RecommendAction;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.product.dto.*;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.CategoryRepository;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.product.repository.TagRepository;
import com.tryiton.core.recommend.service.RecommendBehaviorLogService;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RecommendBehaviorLogService recommendBehaviorLogService;

    @Cacheable(value = "productDetail", key = "'product:' + #productId", unless = "#result == null")
    public ProductDetailResponseDto getProductDetail(Long userId, Long productId) {
        Product product = productRepository.findByIdWithCategoryAndVariants(productId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                "ID " + productId + "에 해당하는 상품을 찾을 수 없습니다."));

        boolean liked = userId != null && 
            wishlistRepository.existsByUserIdAndProductId(userId, productId);

        List<ProductVariantDto> variantDto = product.getVariants().stream()
            .map(ProductVariantDto::new)
            .toList();

        if(userId != null) {
            recommendBehaviorLogService.logUserAction(userId, productId, RecommendAction.CLICK);
        }

        return new ProductDetailResponseDto(product, variantDto, liked);
    }

    @Cacheable(value = "categoryProducts", key = "'category:' + #category.id + ':page:' + #page + ':size:' + #size")
    public Page<ProductSummaryDto> getProductsByCategory(Long userId, Category category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("wishlistCount").descending().and(Sort.by("createAt").descending()));

        Page<ProductSummaryDto> products = productRepository.findSummaryByCategoryHierarchy(
            category.getId(), pageable);

        if (products == null || !products.hasContent()) {
            return Page.empty();
        }

        if (userId != null) {
            Set<Long> likedProductIds = new HashSet<>(
                wishlistRepository.findProductIdsByUserId(userId));
            products.forEach(dto -> dto.setLiked(likedProductIds.contains(dto.getId())));
        }

        return products;
    }

    @Cacheable(value = "mainProducts", key = "'main:products'", unless = "#result == null")
    public MainProductGuestResponse getMainPageProductsForGuest() {
        List<Product> allProducts = productRepository.findTop4ProductsPerCategory();
        Map<Category, List<Product>> productsByCategory = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategory));

        List<CategoryProductGroup> categoryGroups = productsByCategory.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<Product> products = entry.getValue();
                    List<ProductSummary> productSummaries = products.stream()
                            .map(this::convertToProductSummary)
                            .collect(Collectors.toList());
                    return new CategoryProductGroup(
                        category.getId(),
                        category.getCategoryName(),
                        productSummaries
                    );
                })
                .filter(group -> !group.getProducts().isEmpty())
                .collect(Collectors.toList());

        return MainProductGuestResponse.success(categoryGroups);
    }

    @CacheEvict(value = {"productDetail", "categoryProducts", "mainProducts"}, key = "'product:' + #product.id")
    @Transactional
    public void updateProduct(Product product) {
        productRepository.save(product);
    }

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

        List<Product> finalCandidates = productRepository.findByIdsWithTags(new ArrayList<>(candidateIds));

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
        if (userId != null) {
            likedProductIds.addAll(wishlistRepository.findProductIdsByUserId(userId));
        }
        return productRepository.findTop100WithCategory(PageRequest.of(0, 100))
                .stream()
                .map(product -> new ProductResponseDto(product,
                        likedProductIds.contains(product.getId())))
                .toList();
    }

    public List<ProductResponseDto> getSimilarProducts(Long userId, Long productId) {
        Product baseProduct = productRepository.findByIdWithCategory(productId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                "ID " + productId + "에 해당하는 상품을 찾을 수 없습니다."));

        Set<Long> likedProductIds = new HashSet<>(
            wishlistRepository.findProductIdsByUserId(userId));

        List<Product> similarProducts = productRepository.findSimilarProductsByCategory(
            baseProduct.getCategory().getId(), productId, 5);

        return similarProducts.stream()
            .map(product -> new ProductResponseDto(product,
                likedProductIds.contains(product.getId())))
            .collect(Collectors.toList());
    }

    private ProductSummary convertToProductSummary(Product product) {
        int salePrice;
        if (product.getSale() > 0) {
            salePrice = (int) Math.round(product.getPrice() * (100.0 - product.getSale()) / 100.0);
        } else {
            salePrice = product.getPrice();
        }

        return new ProductSummary(
            product.getId(),
            product.getProductName(),
            product.getBrand(),
            product.getPrice(),
            product.getSale(),
            salePrice,
            product.getImg1(),
            product.getCategory().getCategoryName(),
            product.getWishlistCount()
        );
    }

    public List<String> getSearchSuggestions(String query) {
        Pageable limit = PageRequest.of(0, 6);
        return productRepository.findSuggestionsByProductNameOrBrand(query, limit);
    }

    public SearchProductResponse searchProducts(String query, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByProductNameContainingOrBrandContaining(query, query, pageable);

        Set<Long> likedProductIds = new HashSet<>();
        if (userId != null) {
            likedProductIds.addAll(wishlistRepository.findProductIdsByUserId(userId));
        }

        List<ProductResponseDto> productDto = productPage.stream()
            .map(product -> ProductResponseDto.from(product, likedProductIds.contains(product.getId())))
            .collect(Collectors.toList());

        return new SearchProductResponse(productDto, productPage.getTotalElements());
    }
}
