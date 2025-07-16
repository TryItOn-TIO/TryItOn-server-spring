package com.tryiton.core.recommend.controller;

import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.recommend.service.PersonalizedService;
import com.tryiton.core.recommend.service.RecommendationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
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
        @RequestParam Long userId,
        @RequestParam(defaultValue = "12") Integer limit) {

        List<ProductResponseDto> products = personalizedService
            .getPersonalizedRecommendations(userId, limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ProductResponseDto>> getTrending() {
        List<ProductResponseDto> products = recommendationService.getTrendingProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/age-group")
    public ResponseEntity<List<ProductResponseDto>> getAgeGroupRecommendations(
        @RequestParam String ageRange,
        @RequestParam(required = false) String gender) {
        List<ProductResponseDto> products = recommendationService
            .getAgeGroupRecommendations(ageRange, gender);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/similar-to/{productId}")
    public ResponseEntity<List<ProductResponseDto>> getSimilarProducts(
        @PathVariable Long productId) {
        List<ProductResponseDto> products = recommendationService.getSimilarProducts(productId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/tryon-based")
    public ResponseEntity<List<ProductResponseDto>> getTryonRecommendations(
        @RequestParam Long userId) {
        List<ProductResponseDto> products = recommendationService
            .getTryonBasedRecommendations(userId);
        return ResponseEntity.ok(products);
    }

}
