package com.tryiton.core.recommend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.recommend.dto.CollaborativeFilteringMatrix;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecommendationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RecommendationService(RedisTemplate<String, Object> redisTemplate,
        ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // 트렌딩 상품 조회
    public List<Product> getTrendingProducts() {
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

    // 연령대별 추천
    public List<Product> getAgeGroupRecommendations(String ageRange, String gender) {
        String genderKey = gender != null ? gender : "all";
        String key = String.format("recommend:age_group:%s:%s", ageRange, genderKey);
        String data = (String) redisTemplate.opsForValue().get(key);

        if (data != null) {
            try {
                return objectMapper.readValue(data,
                    new TypeReference<List<Product>>() {});
            } catch (Exception e) {
                log.error("연령대별 추천 파싱 오류", e);
            }
        }
        return Collections.emptyList();
    }

    // 유사 상품 추천
    public List<Product> getSimilarProducts(Long productId) {
        String key = "recommend:similar_to:" + productId;
        String data = (String) redisTemplate.opsForValue().get(key);

        if (data != null) {
            try {
                return objectMapper.readValue(data,
                    new TypeReference<List<Product>>() {});
            } catch (Exception e) {
                log.error("유사 상품 추천 파싱 오류", e);
            }
        }
        return Collections.emptyList();
    }

    // Try-on 기반 추천
    public List<Product> getTryonBasedRecommendations(Long userId) {
        String key = "recommend:tryon_based:" + userId;
        String data = (String) redisTemplate.opsForValue().get(key);

        if (data != null) {
            try {
                return objectMapper.readValue(data,
                    new TypeReference<List<Product>>() {});
            } catch (Exception e) {
                log.error("Try-on 추천 파싱 오료", e);
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
}
