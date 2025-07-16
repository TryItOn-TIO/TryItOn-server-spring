package com.tryiton.core.recommend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.recommend.dto.LambdaProductDto;
import com.tryiton.core.recommend.dto.PersonalizedRecommendationResponse;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class PersonalizedService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RecommendationService fallbackService;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    @Value("${aws.lambda.personalized-recommendation.url:}")
    private String lambdaUrl;

    @Value("${aws.lambda.personalized-recommendation.timeout:10}")
    private int timeoutSeconds;

    public PersonalizedService(WebClient.Builder webClientBuilder,
        ObjectMapper objectMapper,
        RecommendationService fallbackService,
        ProductRepository productRepository,
        WishlistRepository wishlistRepository) {
        this.webClient = webClientBuilder
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();
        this.objectMapper = objectMapper;
        this.fallbackService = fallbackService;
        this.productRepository = productRepository;
        this.wishlistRepository = wishlistRepository;
    }

    /**
     * 실시간 개인화 추천 조회
     */
    public List<ProductResponseDto> getPersonalizedRecommendations(Long userId, Integer limit) {
        if (lambdaUrl == null || lambdaUrl.isEmpty()) {
            log.warn("Lambda URL이 설정되지 않음. 폴백 추천 사용");
            return getFallbackRecommendations(userId, limit);
        }

        try {
            log.info("실시간 개인화 추천 요청: 사용자 {}, 개수 {}", userId, limit);

            Map<String, Object> payload = createPayload(userId, limit);

            Mono<PersonalizedRecommendationResponse> response = webClient.post()
                .uri(lambdaUrl)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseResponse)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                    .filter(this::isRetryableException))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .onErrorResume(this::handleError);

            PersonalizedRecommendationResponse result = response.block();

            if (result != null && result.isSuccess()) {
                log.info("개인화 추천 성공: 사용자 {}, {}개 상품, 캐시: {}",
                    userId, result.getRecommendations().size(), result.isFromCache());

                return convertLambdaProductsToResponseDto(result.getRecommendations(), userId);
            }

        } catch (Exception e) {
            log.error("개인화 추천 Lambda 호출 실패: 사용자 {}, 오류: {}", userId, e.getMessage());
        }

        return getFallbackRecommendations(userId, limit);
    }

    /**
     * 사용자 캐시 무효화
     */
    public void invalidateUserCache(Long userId) {
        if (lambdaUrl == null || lambdaUrl.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "invalidate_cache");
            payload.put("user_id", userId);

            webClient.post()
                .uri(lambdaUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                    response -> log.info("사용자 {} 캐시 무효화 완료", userId),
                    error -> log.warn("사용자 {} 캐시 무효화 실패: {}", userId, error.getMessage())
                );

        } catch (Exception e) {
            log.warn("캐시 무효화 요청 실패: 사용자 {}", userId);
        }
    }

    private Map<String, Object> createPayload(Long userId, Integer limit) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("limit", limit != null ? Math.min(limit, 50) : 12);
        payload.put("action", "recommend");
        return payload;
    }

    private PersonalizedRecommendationResponse parseResponse(String responseBody) {
        try {
            log.debug("Lambda 원본 응답: {}", responseBody);
            JsonNode responseJson = objectMapper.readTree(responseBody);

            if (responseJson.has("statusCode")) {
                int statusCode = responseJson.get("statusCode").asInt();
                if (statusCode != 200) {
                    throw new RuntimeException("Lambda 함수 오류: " + statusCode);
                }

                if (responseJson.has("body")) {
                    String bodyStr = responseJson.get("body").asText();
                    log.debug("Lambda body 문자열: {}", bodyStr);
                    JsonNode bodyJson = objectMapper.readTree(bodyStr);
                    log.debug("파싱된 body JSON: {}", bodyJson);
                    return parseRecommendationData(bodyJson);
                }
            } else {
                return parseRecommendationData(responseJson);
            }

            throw new RuntimeException("잘못된 응답 형식");

        } catch (Exception e) {
            log.error("Lambda 응답 파싱 오류: {}", e.getMessage());
            throw new RuntimeException("응답 파싱 실패", e);
        }
    }

    private PersonalizedRecommendationResponse parseRecommendationData(JsonNode dataJson) {
        try {
            log.debug("추천 데이터 파싱 시작: {}", dataJson);
            PersonalizedRecommendationResponse response = new PersonalizedRecommendationResponse();

            if (dataJson.has("error")) {
                response.setSuccess(false);
                response.setError(dataJson.get("error").asText());
                return response;
            }

            response.setSuccess(true);
            response.setUserId(dataJson.get("user_id").asLong());
            response.setFromCache(dataJson.has("from_cache") && dataJson.get("from_cache").asBoolean());

            if (dataJson.has("recommendations")) {
                JsonNode recommendationsNode = dataJson.get("recommendations");
                log.debug("추천 상품 배열: {}", recommendationsNode);
                
                List<LambdaProductDto> products = objectMapper.convertValue(
                    recommendationsNode,
                    new TypeReference<List<LambdaProductDto>>() {}
                );
                log.debug("파싱된 상품 개수: {}", products.size());
                if (!products.isEmpty()) {
                    log.debug("첫 번째 상품: ID={}, Name={}, Brand={}", 
                        products.get(0).getProductId(), 
                        products.get(0).getProductName(), 
                        products.get(0).getBrand());
                }
                response.setRecommendations(products);
            } else {
                response.setRecommendations(Collections.emptyList());
            }

            return response;

        } catch (Exception e) {
            log.error("추천 데이터 파싱 오류: {}", e.getMessage(), e);
            PersonalizedRecommendationResponse errorResponse = new PersonalizedRecommendationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError("데이터 파싱 실패");
            return errorResponse;
        }
    }

    private Mono<PersonalizedRecommendationResponse> handleError(Throwable error) {
        log.error("Lambda 호출 오류: {}", error.getMessage());

        PersonalizedRecommendationResponse errorResponse = new PersonalizedRecommendationResponse();
        errorResponse.setSuccess(false);
        errorResponse.setError(error.getMessage());

        return Mono.just(errorResponse);
    }

    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            int statusCode = ex.getStatusCode().value();
            return statusCode >= 500 || statusCode == 429;
        }
        return throwable instanceof java.net.ConnectException ||
            throwable instanceof java.util.concurrent.TimeoutException;
    }

    private List<ProductResponseDto> getFallbackRecommendations(Long userId, Integer limit) {
        try {
            log.info("폴백 추천 실행 (트렌딩 상품)");
            List<Product> products = fallbackService.getTrendingProductsAsEntity()
                .stream()
                .limit(limit != null ? limit : 12)
                .toList();
            
            return convertToProductResponseDto(products, userId);
        } catch (Exception e) {
            log.error("폴백 추천 실패", e);
            return Collections.emptyList();
        }
    }

    private List<ProductResponseDto> convertLambdaProductsToResponseDto(List<LambdaProductDto> lambdaProducts, Long userId) {
        if (lambdaProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // Lambda에서 받은 product ID들로 실제 Product 엔티티들을 조회
        List<Long> productIds = lambdaProducts.stream()
            .map(LambdaProductDto::getProductId)
            .toList();

        // N+1 문제 방지를 위해 카테고리와 함께 조회
        List<Product> products = productRepository.findByIdsWithTags(productIds);
        
        // 사용자의 찜한 상품 ID 목록 조회
        Set<Long> likedProductIds = getUserLikedProductIds(userId);

        // Lambda에서 받은 순서대로 정렬하여 반환
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, product -> product));

        return lambdaProducts.stream()
            .map(lambdaProduct -> {
                Product product = productMap.get(lambdaProduct.getProductId());
                if (product != null) {
                    return new ProductResponseDto(product, likedProductIds.contains(product.getId()));
                } else {
                    // DB에서 찾을 수 없는 경우, Lambda 데이터로 임시 ProductResponseDto 생성
                    log.warn("상품 ID {}를 DB에서 찾을 수 없음", lambdaProduct.getProductId());
                    return createProductResponseDtoFromLambda(lambdaProduct, likedProductIds.contains(lambdaProduct.getProductId()));
                }
            })
            .filter(dto -> dto != null) // null 체크 추가
            .toList();
    }

    private ProductResponseDto createProductResponseDtoFromLambda(LambdaProductDto lambdaProduct, boolean liked) {
        try {
            // Lambda 데이터만으로 ProductResponseDto를 생성하는 임시 방법
            // 실제로는 DB에서 조회하는 것이 좋지만, 데이터가 없는 경우의 fallback
            return new ProductResponseDto(
                lambdaProduct.getProductId(),
                lambdaProduct.getProductName() != null ? lambdaProduct.getProductName() : "상품명 없음",
                lambdaProduct.getImg1() != null ? lambdaProduct.getImg1() : "",
                lambdaProduct.getPrice() != null ? lambdaProduct.getPrice() : 0,
                0, // sale - Lambda에서 제공하지 않음
                lambdaProduct.getPrice() != null ? lambdaProduct.getPrice() : 0, // salePrice
                liked,
                lambdaProduct.getBrand() != null ? lambdaProduct.getBrand() : "브랜드 없음",
                0, // wishlistCount - Lambda에서 제공하지 않음
                null, // createdAt - Lambda에서 제공하지 않음
                null, // categoryId - Lambda에서 제공하지 않음
                null  // categoryName - Lambda에서 제공하지 않음
            );
        } catch (Exception e) {
            log.error("Lambda 데이터로 ProductResponseDto 생성 실패: productId={}, error={}", 
                lambdaProduct.getProductId(), e.getMessage());
            return null;
        }
    }

    private List<ProductResponseDto> convertToProductResponseDto(List<Product> products, Long userId) {
        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        // 사용자의 찜한 상품 ID 목록 조회
        Set<Long> likedProductIds = getUserLikedProductIds(userId);

        return products.stream()
            .map(product -> new ProductResponseDto(product, likedProductIds.contains(product.getId())))
            .toList();
    }

    private Set<Long> getUserLikedProductIds(Long userId) {
        try {
            List<Long> likedProductIds = wishlistRepository.findProductIdsByUserId(userId);
            return likedProductIds.stream().collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("사용자 {}의 찜 목록 조회 실패: {}", userId, e.getMessage());
            return Collections.emptySet();
        }
    }

}
