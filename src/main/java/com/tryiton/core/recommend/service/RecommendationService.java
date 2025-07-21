package com.tryiton.core.recommend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.common.service.SharedDataAccessor;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.recommend.dto.CachedProductDto;
import com.tryiton.core.recommend.dto.CollaborativeFilteringMatrix;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RecommendationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository; // 호환성을 위해 유지
    private final WishlistRepository wishlistRepository;
    private final SharedDataAccessor sharedDataAccessor;

    public RecommendationService(
            @Qualifier("recommendRedisTemplate") RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            ProductRepository productRepository,
            WishlistRepository wishlistRepository,
            SharedDataAccessor sharedDataAccessor) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
        this.wishlistRepository = wishlistRepository;
        this.sharedDataAccessor = sharedDataAccessor;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getTrendingProducts(Long userId) {
        final String cacheKey = "recommend:trending";
        final String sharedKey = "trending";
        try {
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                List<CachedProductDto> cachedProducts = sharedDataAccessor.getSharedData(sharedKey, new TypeReference<>() {});
                if (cachedProducts != null && !cachedProducts.isEmpty()) {
                    return convertCachedProductsToResponseDto(cachedProducts, userId);
                }
            }
            
            Object rawData = redisTemplate.opsForValue().get(cacheKey);
            List<CachedProductDto> cachedProducts = parseRedisData(cacheKey, rawData, new TypeReference<>() {});

            if (cachedProducts != null && !cachedProducts.isEmpty()) {
                sharedDataAccessor.saveSharedData(sharedKey, cachedProducts);
                return convertCachedProductsToResponseDto(cachedProducts, userId);
            }
        } catch (Exception e) {
            log.error("트렌딩 상품 조회 실패: {}. DB에서 폴백합니다.", e.getMessage());
        }

        // Redis 조회 실패 또는 데이터 없음 시 DB에서 직접 조회
        log.warn("트렌딩 상품을 Redis 캐시에서 찾을 수 없어 DB에서 직접 조회합니다.");
        List<Product> productsFromDb = productRepository.findTop100WithCategory(PageRequest.of(0, 100));
        return convertProductsToResponseDto(productsFromDb, userId);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAgeGroupRecommendations(Long userId, String ageRange, String gender) {
        String genderKey = gender != null ? gender : "all";
        String key = String.format("recommend:age_group:%s:%s", ageRange, genderKey);
        String sharedKey = String.format("age_group:%s:%s", ageRange, genderKey);
        
        try {
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                List<CachedProductDto> cachedProducts = sharedDataAccessor.getSharedData(sharedKey, new TypeReference<>() {});
                if (cachedProducts != null && !cachedProducts.isEmpty()) {
                    return convertCachedProductsToResponseDto(cachedProducts, userId);
                }
            }
            
            Object rawData = redisTemplate.opsForValue().get(key);
            List<CachedProductDto> cachedProducts = parseRedisData(key, rawData, new TypeReference<>() {});
            
            if (cachedProducts != null && !cachedProducts.isEmpty()) {
                sharedDataAccessor.saveSharedData(sharedKey, cachedProducts);
                return convertCachedProductsToResponseDto(cachedProducts, userId);
            }
        } catch (Exception e) {
            log.error("연령대별 추천 파싱 오류: {}. DB에서 폴백합니다.", e.getMessage());
        }

        log.warn("연령대별 추천 상품을 Redis 캐시에서 찾을 수 없어 DB에서 직접 조회합니다.");
        Page<Long> productIds = productRepository.findTopNProductIds(PageRequest.of(0, 50)); // 인기 상품으로 대체
        List<Product> productsFromDb = productRepository.findByIdsWithDetails(productIds.getContent());
        return convertProductsToResponseDto(productsFromDb, userId);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getSimilarProducts(Long userId, Long productId) {
        String key = "recommend:similar_to:" + productId;
        String sharedKey = "similar_to:" + productId;
        
        try {
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                List<CachedProductDto> cachedProducts = sharedDataAccessor.getSharedData(sharedKey, new TypeReference<>() {});
                if (cachedProducts != null && !cachedProducts.isEmpty()) {
                    return convertCachedProductsToResponseDto(cachedProducts, userId);
                }
            }
            
            Object rawData = redisTemplate.opsForValue().get(key);
            List<CachedProductDto> cachedProducts = parseRedisData(key, rawData, new TypeReference<>() {});
            
            if (cachedProducts != null && !cachedProducts.isEmpty()) {
                sharedDataAccessor.saveSharedData(sharedKey, cachedProducts);
                return convertCachedProductsToResponseDto(cachedProducts, userId);
            }
        } catch (Exception e) {
            log.error("유사 상품 추천 파싱 오류: {}. DB에서 폴백합니다.", e.getMessage());
        }

        log.warn("유사 상품을 Redis 캐시에서 찾을 수 없어 DB에서 직접 조회합니다.");
        Product product = productRepository.findByIdWithCategory(productId).orElse(null);
        if (product == null) return Collections.emptyList();
        List<Product> similarFromDb = productRepository.findSimilarProductsByCategory(product.getCategory().getId(), productId, 20);
        return convertProductsToResponseDto(similarFromDb, userId);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getTryonBasedRecommendations(Long userId) {
        String key = "recommend:tryon_based:" + userId;
        String sharedKey = "tryon_based:" + userId;
        
        try {
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                List<CachedProductDto> cachedProducts = sharedDataAccessor.getSharedData(sharedKey, new TypeReference<>() {});
                if (cachedProducts != null && !cachedProducts.isEmpty()) {
                    return convertCachedProductsToResponseDto(cachedProducts, userId);
                }
            }
            
            Object rawData = redisTemplate.opsForValue().get(key);
            List<CachedProductDto> cachedProducts = parseRedisData(key, rawData, new TypeReference<>() {});
            
            if (cachedProducts != null && !cachedProducts.isEmpty()) {
                sharedDataAccessor.saveSharedData(sharedKey, cachedProducts);
                return convertCachedProductsToResponseDto(cachedProducts, userId);
            }
        } catch (Exception e) {
            log.error("Try-on 추천 파싱 오류: {}. DB에서 폴백합니다.", e.getMessage());
        }

        log.warn("Try-on 추천 상품을 Redis 캐시에서 찾을 수 없어 DB에서 직접 조회합니다.");
        Page<Long> productIds = productRepository.findTopNProductIds(PageRequest.of(0, 20)); // 인기 상품으로 대체
        List<Product> productsFromDb = productRepository.findByIdsWithDetails(productIds.getContent());
        return convertProductsToResponseDto(productsFromDb, userId);
    }

    private <T> List<T> parseRedisData(String key, Object rawData, TypeReference<List<T>> typeReference) {
        if (rawData == null) {
            return null;
        }
        try {
            if (rawData instanceof String) {
                return objectMapper.readValue((String) rawData, typeReference);
            } else {
                return objectMapper.convertValue(rawData, typeReference);
            }
        } catch (Exception e) {
            log.error("Redis 데이터 파싱 오류. Key: {}, RawData Type: {}, Error: {}", key, rawData.getClass().getName(), e.getMessage());
            return null;
        }
    }

    private List<ProductResponseDto> convertProductsToResponseDto(List<Product> products, Long userId) {
        if (products == null || products.isEmpty()) return Collections.emptyList();
        Set<Long> likedProductIds = getUserLikedProductIds(userId);
        return products.stream()
            .map(product -> new ProductResponseDto(product, likedProductIds.contains(product.getId())))
            .collect(Collectors.toList());
    }

    /**
     * 캐시된 상품 정보 리스트를 최종 응답 DTO 리스트로 변환합니다.
     * 이 메소드는 DB에서 상품 정보를 조회하지 않습니다.
     * @param cachedProducts Redis에서 가져온 상품 정보 리스트
     * @param userId 현재 사용자 ID
     * @return 프론트엔드로 보낼 최종 DTO 리스트
     */
    private List<ProductResponseDto> convertCachedProductsToResponseDto(List<CachedProductDto> cachedProducts, Long userId) {
        if (cachedProducts == null || cachedProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // 찜 목록만 DB에서 조회합니다.
        Set<Long> likedProductIds = getUserLikedProductIds(userId);

        return cachedProducts.stream()
            .map(cachedProduct -> new ProductResponseDto(cachedProduct, likedProductIds.contains(cachedProduct.getProductId())))
            .collect(Collectors.toList());
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

    // ==========================================================================================
    // 아래는 호환성 유지를 위한 레거시 또는 관리용 메소드들입니다.
    // ==========================================================================================

    // 기존 호환성을 위한 메서드 (Product 엔티티 반환)
    public List<Product> getTrendingProductsAsEntity() {
        return productRepository.findTop100WithCategory(PageRequest.of(0, 100));
    }
    
    // 협업필터링 매트릭스 조회
    public CollaborativeFilteringMatrix getCFMatrix() {
        String key = "recommend:cf_matrix";
        String sharedKey = "cf_matrix";
        
        try {
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                return sharedDataAccessor.getSharedData(sharedKey, CollaborativeFilteringMatrix.class);
            }
            
            Object rawData = redisTemplate.opsForValue().get(key);
            
            CollaborativeFilteringMatrix matrix = null;
            if (rawData instanceof CollaborativeFilteringMatrix) {
                matrix = (CollaborativeFilteringMatrix) rawData;
            } else if (rawData instanceof String) {
                matrix = objectMapper.readValue((String) rawData, CollaborativeFilteringMatrix.class);
            }
            
            if (matrix != null) {
                sharedDataAccessor.saveSharedData(sharedKey, matrix);
                return matrix;
            }
        } catch (Exception e) {
            log.error("CF 매트릭스 파싱 오류", e);
        }
        return null;
    }

    /*
    // Redis 데이터를 ProductResponseDto로 변환하는 공통 메서드
    private List<ProductResponseDto> parseRedisDataToProductResponseDto(String data, Long userId) {
        try {
            List<LambdaBatchDto> lambdaProducts = objectMapper.readValue(data, new TypeReference<List<LambdaBatchDto>>() {});
            log.info("트렌딩 상품 조회 데이터(LambdaBatchDto): {}", lambdaProducts);
            return convertLambdaProductsToResponseDto(lambdaProducts, userId);
        } catch (Exception e) {
            log.error("Redis 데이터 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // LambdaProductDto를 ProductResponseDto로 변환
    private List<ProductResponseDto> convertLambdaProductsToResponseDto(List<LambdaBatchDto> lambdaProducts, Long userId) {
        if (lambdaProducts.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = lambdaProducts.stream()
            .map(LambdaBatchDto::getProductId)
            .toList();

        // DB에서 실제 Product 정보 조회 (Category와 함께 조회하여 Lazy Loading 문제 해결)
        List<Product> products = productRepository.findByIdsWithCategory(productIds);
        Set<Long> likedProductIds = getUserLikedProductIds(userId);
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, product -> product));

        return lambdaProducts.stream()
            .map(lambdaProduct -> {
                Product product = productMap.get(lambdaProduct.getProductId());
                if (product != null) {
                    return new ProductResponseDto(product, likedProductIds.contains(product.getId()));
                } else {
                    return createProductResponseDtoFromLambda(lambdaProduct, likedProductIds.contains(lambdaProduct.getProductId()));
                }
            })
            .filter(dto -> dto != null)
            .toList();
    }

    // Lambda 데이터로 ProductResponseDto 생성
    private ProductResponseDto createProductResponseDtoFromLambda(LambdaBatchDto lambdaProduct, boolean liked) {
        try {
            return new ProductResponseDto(
                lambdaProduct.getProductId(),
                lambdaProduct.getProductName() != null ? lambdaProduct.getProductName() : "상품명 없음",
                lambdaProduct.getImg1() != null ? lambdaProduct.getImg1() : "",
                0, // price
                0, // sale
                0, // salePrice
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
    */
}
