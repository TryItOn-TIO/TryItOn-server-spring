package com.tryiton.core.recommend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.common.service.SharedDataAccessor;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.recommend.dto.CollaborativeFilteringMatrix;
import com.tryiton.core.recommend.dto.LambdaBatchDto;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RecommendationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
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

    // 트렌딩 상품 조회 - ProductResponseDto 반환
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getTrendingProducts(Long userId) {
        try {
            // 공유 데이터 접근자를 통해 먼저 조회 시도
            if (sharedDataAccessor.hasSharedData("trending")) {
                log.info("공유 데이터 접근자를 통해 트렌딩 상품 조회");
                List<LambdaBatchDto> lambdaProducts = sharedDataAccessor.getSharedData("trending", List.class);
                if (lambdaProducts != null && !lambdaProducts.isEmpty()) {
                    return convertLambdaProductsToResponseDto(lambdaProducts, userId);
                }
            }
            
            // 기존 방식으로 조회
            Object rawData = redisTemplate.opsForValue().get("recommend:trending");
            log.info("트렌딩 상품 조회 데이터 타입: {}", rawData != null ? rawData.getClass().getName() : "null");
            
            List<LambdaBatchDto> lambdaProducts = null;
            if (rawData instanceof List) {
                lambdaProducts = (List<LambdaBatchDto>) rawData;
            } else if (rawData instanceof String) {
                lambdaProducts = objectMapper.readValue((String) rawData, new TypeReference<List<LambdaBatchDto>>() {});
            }
            
            if (lambdaProducts != null && !lambdaProducts.isEmpty()) {
                log.info("트렌딩 상품 조회 데이터 존재");
                
                // 공유 데이터로 저장 (sharedDataAccessor는 String을 기대하므로 변환)
                String dataToSave = objectMapper.writeValueAsString(lambdaProducts);
                sharedDataAccessor.saveSharedData("trending", dataToSave);
                
                return convertLambdaProductsToResponseDto(lambdaProducts, userId);
            }
        } catch (Exception e) {
            log.error("트렌딩 상품 조회 실패: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    // 기존 호환성을 위한 메서드 (Product 엔티티 반환)
    public List<Product> getTrendingProductsAsEntity() {
        try {
            // 공유 데이터 접근자를 통해 먼저 조회 시도
            if (sharedDataAccessor.hasSharedData("trending")) {
                List<Product> products = sharedDataAccessor.getSharedData("trending", List.class);
                if (products != null && !products.isEmpty()) {
                    return products;
                }
            }
            
            // 기존 방식으로 조회
            Object rawData = redisTemplate.opsForValue().get("recommend:trending");
            
            List<Product> products = null;
            if (rawData instanceof List) {
                products = (List<Product>) rawData;
            } else if (rawData instanceof String) {
                products = objectMapper.readValue((String) rawData, new TypeReference<List<Product>>() {});
            }
            
            if (products != null && !products.isEmpty()) {
                // 공유 데이터로 저장 (sharedDataAccessor는 String을 기대하므로 변환)
                String dataToSave = objectMapper.writeValueAsString(products);
                sharedDataAccessor.saveSharedData("trending", dataToSave);
                
                return products;
            }
        } catch (Exception e) {
            log.error("트렌딩 상품 조회 실패", e);
        }
        return Collections.emptyList();
    }

    // 연령대별 추천 - ProductResponseDto 반환
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAgeGroupRecommendations(Long userId, String ageRange, String gender) {
        String genderKey = gender != null ? gender : "all";
        String key = String.format("recommend:age_group:%s:%s", ageRange, genderKey);
        String sharedKey = String.format("age_group:%s:%s", ageRange, genderKey);
        
        try {
            // 공유 데이터 접근자를 통해 먼저 조회 시도
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                List<LambdaBatchDto> lambdaProducts = sharedDataAccessor.getSharedData(sharedKey, List.class);
                if (lambdaProducts != null) {
                    return convertLambdaProductsToResponseDto(lambdaProducts, userId);
                }
            }
            
            // 기존 방식으로 조회
            Object rawData = redisTemplate.opsForValue().get(key);
            
            List<LambdaBatchDto> lambdaProducts = null;
            if (rawData instanceof List) {
                lambdaProducts = (List<LambdaBatchDto>) rawData;
            } else if (rawData instanceof String) {
                lambdaProducts = objectMapper.readValue((String) rawData, new TypeReference<List<LambdaBatchDto>>() {});
            }
            
            if (lambdaProducts != null) {
                // 공유 데이터로 저장 (sharedDataAccessor는 String을 기대하므로 변환)
                String dataToSave = objectMapper.writeValueAsString(lambdaProducts);
                sharedDataAccessor.saveSharedData(sharedKey, dataToSave);
                
                return convertLambdaProductsToResponseDto(lambdaProducts, userId);
            }
        } catch (Exception e) {
            log.error("연령대별 추천 파싱 오류", e);
        }
        return Collections.emptyList();
    }

    // 유사 상품 추천 - ProductResponseDto 반환
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getSimilarProducts(Long userId, Long productId) {
        String key = "recommend:similar_to:" + productId;
        String sharedKey = "similar_to:" + productId;
        
        try {
            // 공유 데이터 접근자를 통해 먼저 조회 시도
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                List<LambdaBatchDto> lambdaProducts = sharedDataAccessor.getSharedData(sharedKey, List.class);
                if (lambdaProducts != null) {
                    return convertLambdaProductsToResponseDto(lambdaProducts, userId);
                }
            }
            
            // 기존 방식으로 조회
            Object rawData = redisTemplate.opsForValue().get(key);
            
            List<LambdaBatchDto> lambdaProducts = null;
            if (rawData instanceof List) {
                lambdaProducts = (List<LambdaBatchDto>) rawData;
            } else if (rawData instanceof String) {
                lambdaProducts = objectMapper.readValue((String) rawData, new TypeReference<List<LambdaBatchDto>>() {});
            }
            
            if (lambdaProducts != null) {
                // 공유 데이터로 저장 (sharedDataAccessor는 String을 기대하므로 변환)
                String dataToSave = objectMapper.writeValueAsString(lambdaProducts);
                sharedDataAccessor.saveSharedData(sharedKey, dataToSave);
                
                return convertLambdaProductsToResponseDto(lambdaProducts, userId);
            }
        } catch (Exception e) {
            log.error("유사 상품 추천 파싱 오류", e);
        }
        return Collections.emptyList();
    }

    // Try-on 기반 추천 - ProductResponseDto 반환 (로그인 사용자용)
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getTryonBasedRecommendations(Long userId) {
        String key = "recommend:tryon_based:" + userId;
        String sharedKey = "tryon_based:" + userId;
        
        try {
            // 공유 데이터 접근자를 통해 먼저 조회 시도
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                List<LambdaBatchDto> lambdaProducts = sharedDataAccessor.getSharedData(sharedKey, List.class);
                if (lambdaProducts != null) {
                    return convertLambdaProductsToResponseDto(lambdaProducts, userId);
                }
            }
            
            // 기존 방식으로 조회
            Object rawData = redisTemplate.opsForValue().get(key);
            
            List<LambdaBatchDto> lambdaProducts = null;
            if (rawData instanceof List) {
                lambdaProducts = (List<LambdaBatchDto>) rawData;
            } else if (rawData instanceof String) {
                lambdaProducts = objectMapper.readValue((String) rawData, new TypeReference<List<LambdaBatchDto>>() {});
            }
            
            if (lambdaProducts != null) {
                // 공유 데이터로 저장 (sharedDataAccessor는 String을 기대하므로 변환)
                String dataToSave = objectMapper.writeValueAsString(lambdaProducts);
                sharedDataAccessor.saveSharedData(sharedKey, dataToSave);
                
                return convertLambdaProductsToResponseDto(lambdaProducts, userId);
            }
        } catch (Exception e) {
            log.error("Try-on 추천 파싱 오류", e);
        }
        return Collections.emptyList();
    }

    // 협업필터링 매트릭스 조회
    public CollaborativeFilteringMatrix getCFMatrix() {
        String key = "recommend:cf_matrix";
        String sharedKey = "cf_matrix";
        
        try {
            // 공유 데이터 접근자를 통해 먼저 조회 시도
            if (sharedDataAccessor.hasSharedData(sharedKey)) {
                return sharedDataAccessor.getSharedData(sharedKey, CollaborativeFilteringMatrix.class);
            }
            
            // 기존 방식으로 조회
            Object rawData = redisTemplate.opsForValue().get(key);
            
            CollaborativeFilteringMatrix matrix = null;
            if (rawData instanceof CollaborativeFilteringMatrix) {
                matrix = (CollaborativeFilteringMatrix) rawData;
            } else if (rawData instanceof String) {
                matrix = objectMapper.readValue((String) rawData, CollaborativeFilteringMatrix.class);
            }
            
            if (matrix != null) {
                // 공유 데이터로 저장
                sharedDataAccessor.saveSharedData(sharedKey, matrix);
                
                return matrix;
            }
        } catch (Exception e) {
            log.error("CF 매트릭스 파싱 오류", e);
        }
        return null;
    }

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
