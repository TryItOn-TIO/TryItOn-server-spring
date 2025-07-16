package com.tryiton.core.recommend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.recommend.dto.CollaborativeFilteringMatrix;
import com.tryiton.core.recommend.dto.LambdaProductDto;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecommendationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    public RecommendationService(RedisTemplate<String, Object> redisTemplate,
        ObjectMapper objectMapper,
        ProductRepository productRepository,
        WishlistRepository wishlistRepository) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
        this.wishlistRepository = wishlistRepository;
    }

    // 트렌딩 상품 조회 - ProductResponseDto 반환 (비로그인 사용자용)
    public List<ProductResponseDto> getTrendingProducts() {
        try {
            String data = (String) redisTemplate.opsForValue().get("recommend:trending");
            if (data != null && !data.isEmpty()) {
                return parseRedisDataToProductResponseDto(data, null);
            }
        } catch (Exception e) {
            log.error("트렌딩 상품 조회 실패", e);
        }
        return Collections.emptyList();
    }

    // 기존 호환성을 위한 메서드 (Product 엔티티 반환)
    public List<Product> getTrendingProductsAsEntity() {
        try {
            String data = (String) redisTemplate.opsForValue().get("recommend:trending");
            if (data != null && !data.isEmpty()) {
                return objectMapper.readValue(data, new TypeReference<List<Product>>() {});
            }
        } catch (Exception e) {
            log.error("트렌딩 상품 조회 실패", e);
        }
        return Collections.emptyList();
    }

    // 연령대별 추천 - ProductResponseDto 반환 (비로그인 사용자용)
    public List<ProductResponseDto> getAgeGroupRecommendations(String ageRange, String gender) {
        String genderKey = gender != null ? gender : "all";
        String key = String.format("recommend:age_group:%s:%s", ageRange, genderKey);
        String data = (String) redisTemplate.opsForValue().get(key);

        if (data != null) {
            try {
                return parseRedisDataToProductResponseDto(data, null);
            } catch (Exception e) {
                log.error("연령대별 추천 파싱 오류", e);
            }
        }
        return Collections.emptyList();
    }

    // 유사 상품 추천 - ProductResponseDto 반환 (비로그인 사용자용)
    public List<ProductResponseDto> getSimilarProducts(Long productId) {
        String key = "recommend:similar_to:" + productId;
        String data = (String) redisTemplate.opsForValue().get(key);

        if (data != null) {
            try {
                return parseRedisDataToProductResponseDto(data, null);
            } catch (Exception e) {
                log.error("유사 상품 추천 파싱 오류", e);
            }
        }
        return Collections.emptyList();
    }

    // Try-on 기반 추천 - ProductResponseDto 반환 (로그인 사용자용)
    public List<ProductResponseDto> getTryonBasedRecommendations(Long userId) {
        String key = "recommend:tryon_based:" + userId;
        String data = (String) redisTemplate.opsForValue().get(key);

        if (data != null) {
            try {
                return parseRedisDataToProductResponseDto(data, userId);
            } catch (Exception e) {
                log.error("Try-on 추천 파싱 오류", e);
            }
        }
        return Collections.emptyList();
    }

    // 협업필터링 매트릭스 조회
    public CollaborativeFilteringMatrix getCFMatrix() {
        String data = (String) redisTemplate.opsForValue().get("recommend:cf_matrix");
        if (data != null) {
            try {
                return objectMapper.readValue(data, CollaborativeFilteringMatrix.class);
            } catch (Exception e) {
                log.error("CF 매트릭스 파싱 오류", e);
            }
        }
        return null;
    }

    // Redis 데이터를 ProductResponseDto로 변환하는 공통 메서드
    private List<ProductResponseDto> parseRedisDataToProductResponseDto(String data, Long userId) {
        try {
            // 먼저 Lambda 형식(LambdaProductDto)으로 파싱 시도
            try {
                List<LambdaProductDto> lambdaProducts = objectMapper.readValue(data, 
                    new TypeReference<List<LambdaProductDto>>() {});
                return convertLambdaProductsToResponseDto(lambdaProducts, userId);
            } catch (Exception e) {
                // Lambda 형식이 아니면 Product 엔티티 형식으로 파싱 시도
                List<Product> products = objectMapper.readValue(data, 
                    new TypeReference<List<Product>>() {});
                return convertProductsToResponseDto(products, userId);
            }
        } catch (Exception e) {
            log.error("Redis 데이터 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // LambdaProductDto를 ProductResponseDto로 변환
    private List<ProductResponseDto> convertLambdaProductsToResponseDto(List<LambdaProductDto> lambdaProducts, Long userId) {
        if (lambdaProducts.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = lambdaProducts.stream()
            .map(LambdaProductDto::getProductId)
            .toList();

        // DB에서 실제 Product 정보 조회
        List<Product> products = productRepository.findAllById(productIds);
        Set<Long> likedProductIds = getUserLikedProductIds(userId);
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, product -> product));

        return lambdaProducts.stream()
            .map(lambdaProduct -> {
                Product product = productMap.get(lambdaProduct.getProductId());
                if (product != null) {
                    return new ProductResponseDto(product, likedProductIds.contains(product.getId()));
                } else {
                    return createProductResponseDtoFromLambda(lambdaProduct, 
                        likedProductIds.contains(lambdaProduct.getProductId()));
                }
            })
            .filter(dto -> dto != null)
            .toList();
    }

    // Product 엔티티를 ProductResponseDto로 변환
    private List<ProductResponseDto> convertProductsToResponseDto(List<Product> products, Long userId) {
        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> likedProductIds = getUserLikedProductIds(userId);
        return products.stream()
            .map(product -> new ProductResponseDto(product, likedProductIds.contains(product.getId())))
            .toList();
    }

    // Lambda 데이터로 ProductResponseDto 생성
    private ProductResponseDto createProductResponseDtoFromLambda(LambdaProductDto lambdaProduct, boolean liked) {
        try {
            return new ProductResponseDto(
                lambdaProduct.getProductId(),
                lambdaProduct.getProductName() != null ? lambdaProduct.getProductName() : "상품명 없음",
                lambdaProduct.getImg1() != null ? lambdaProduct.getImg1() : "",
                lambdaProduct.getPrice() != null ? lambdaProduct.getPrice() : 0,
                0, // sale
                lambdaProduct.getPrice() != null ? lambdaProduct.getPrice() : 0, // salePrice
                liked,
                lambdaProduct.getBrand() != null ? lambdaProduct.getBrand() : "브랜드 없음",
                0, // wishlistCount
                null, // createdAt
                null, // categoryId
                null  // categoryName
            );
        } catch (Exception e) {
            log.error("Lambda 데이터로 ProductResponseDto 생성 실패: productId={}, error={}", 
                lambdaProduct.getProductId(), e.getMessage());
            return null;
        }
    }

    // 사용자 찜 목록 조회
    private Set<Long> getUserLikedProductIds(Long userId) {
        try {
            if (userId == null) {
                return Collections.emptySet();
            }
            List<Long> likedProductIds = wishlistRepository.findProductIdsByUserId(userId);
            return likedProductIds.stream().collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("사용자 {}의 찜 목록 조회 실패: {}", userId, e.getMessage());
            return Collections.emptySet();
        }
    }
}
