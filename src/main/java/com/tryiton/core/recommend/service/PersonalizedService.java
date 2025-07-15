package com.tryiton.core.recommend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.recommend.dto.PersonalizedRecommendationResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Value("${aws.lambda.personalized-recommendation.url:}")
    private String lambdaUrl;

    @Value("${aws.lambda.personalized-recommendation.timeout:10}")
    private int timeoutSeconds;

    public PersonalizedService(WebClient.Builder webClientBuilder,
        ObjectMapper objectMapper,
        RecommendationService fallbackService) {
        this.webClient = webClientBuilder
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();
        this.objectMapper = objectMapper;
        this.fallbackService = fallbackService;
    }

    /**
     * 실시간 개인화 추천 조회
     */
    public List<Product> getPersonalizedRecommendations(Long userId, Integer limit) {
        if (lambdaUrl == null || lambdaUrl.isEmpty()) {
            log.warn("Lambda URL이 설정되지 않음. 폴백 추천 사용");
            return getFallbackRecommendations(limit);
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

                return result.getRecommendations();
            }

        } catch (Exception e) {
            log.error("개인화 추천 Lambda 호출 실패: 사용자 {}, 오류: {}", userId, e.getMessage());
        }

        return getFallbackRecommendations(limit);
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
            JsonNode responseJson = objectMapper.readTree(responseBody);

            if (responseJson.has("statusCode")) {
                int statusCode = responseJson.get("statusCode").asInt();
                if (statusCode != 200) {
                    throw new RuntimeException("Lambda 함수 오류: " + statusCode);
                }

                if (responseJson.has("body")) {
                    String bodyStr = responseJson.get("body").asText();
                    JsonNode bodyJson = objectMapper.readTree(bodyStr);
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
                List<Product> products = objectMapper.convertValue(
                    dataJson.get("recommendations"),
                    new TypeReference<List<Product>>() {}
                );
                response.setRecommendations(products);
            } else {
                response.setRecommendations(Collections.emptyList());
            }

            return response;

        } catch (Exception e) {
            log.error("추천 데이터 파싱 오류: {}", e.getMessage());
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

    private List<Product> getFallbackRecommendations(Integer limit) {
        try {
            log.info("폴백 추천 실행 (트렌딩 상품)");
            return fallbackService.getTrendingProducts()
                .stream()
                .limit(limit != null ? limit : 12)
                .toList();
        } catch (Exception e) {
            log.error("폴백 추천 실패", e);
            return Collections.emptyList();
        }
    }

}
