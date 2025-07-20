package com.tryiton.core.recommend.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.recommend.service.PersonalizedService;
import com.tryiton.core.recommend.service.RecommendationService;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final PersonalizedService personalizedService;

    public RecommendationController(RecommendationService recommendationService,
        PersonalizedService personalizedService) {
        this.recommendationService = recommendationService;
        this.personalizedService = personalizedService;
    }

    @GetMapping("/for-you")
    public ResponseEntity<List<ProductResponseDto>> getPersonalizedRecommendations(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestParam(defaultValue = "8") Integer limit) {
        Long userId = customUserDetails.getUser().getId();
        List<ProductResponseDto> products = personalizedService
            .getPersonalizedRecommendations(userId, limit);
        // ====== 로그 추가 시작 ======
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(products);
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).info("[개인화 추천 응답] {}", json);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).warn("[개인화 추천 응답 로그 변환 실패] {}", e.getMessage());
        }
        // ====== 로그 추가 끝 ======
        return ResponseEntity.ok(products);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ProductResponseDto>> getTrending(
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        // 비로그인 사용자인 경우 userId를 null로 처리
        Long userId = (customUserDetails != null) ? customUserDetails.getUser().getId() : null;
        List<ProductResponseDto> products = recommendationService.getTrendingProducts(userId);
        // ====== 로그 추가 시작 ======
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(products);
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).info("[인기상품 응답] {}", json);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).warn("[인기상품 응답 로그 변환 실패] {}", e.getMessage());
        }
        // ====== 로그 추가 끝 ======
        return ResponseEntity.ok(products);
    }

    @GetMapping("/age-group")
    public ResponseEntity<List<ProductResponseDto>> getAgeGroupRecommendations(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestParam(required = false) String gender) {
        String ageRange = "";
        Long userId;

        if (customUserDetails == null) {
            ageRange = "20s";
            userId = null;
        } else {
            userId = customUserDetails.getUser().getId();

            LocalDate birthDate = customUserDetails.getUser().getBirthDate();
            LocalDate currentDate = LocalDate.now();
            int age = Period.between(birthDate, currentDate).getYears();

            if (age >= 20 && age <= 29) {
                ageRange = "20s";
            } else if (age >= 30 && age <= 39) {
                ageRange = "30s";
            } else if (age >= 40 && age <= 49) {
                ageRange = "40s";
            } else if (age >= 50 && age <= 59) {
                ageRange = "50s";
            } else if (age >= 60) {
                ageRange = "60s";
            }
        }

        List<ProductResponseDto> products = recommendationService
            .getAgeGroupRecommendations(userId, ageRange, gender);
        // ====== 로그 추가 시작 ======
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(products);
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).info("[연령대별 추천 응답] {}", json);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).warn("[연령대별 추천 응답 로그 변환 실패] {}", e.getMessage());
        }
        // ====== 로그 추가 끝 ======
        return ResponseEntity.ok(products);
    }

    @GetMapping("/similar-to/{productId}")
    public ResponseEntity<List<ProductResponseDto>> getSimilarProducts(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long productId) {
        // 비로그인 사용자인 경우 userId를 null로 처리
        Long userId = (customUserDetails != null) ? customUserDetails.getUser().getId() : null;
        List<ProductResponseDto> products = recommendationService.getSimilarProducts(userId, productId);
        // ====== 로그 추가 시작 ======
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(products);
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).info("[유사상품 응답] {}", json);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).warn("[유사상품 응답 로그 변환 실패] {}", e.getMessage());
        }
        // ====== 로그 추가 끝 ======
        return ResponseEntity.ok(products);
    }

    @GetMapping("/tryon-based")
    public ResponseEntity<List<ProductResponseDto>> getTryonRecommendations(
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        List<ProductResponseDto> products = recommendationService
            .getTryonBasedRecommendations(customUserDetails.getUser().getId());
        // ====== 로그 추가 시작 ======
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(products);
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).info("[Tryon 기반 추천 응답] {}", json);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RecommendationController.class).warn("[Tryon 기반 추천 응답 로그 변환 실패] {}", e.getMessage());
        }
        // ====== 로그 추가 끝 ======
        return ResponseEntity.ok(products);
    }

}
