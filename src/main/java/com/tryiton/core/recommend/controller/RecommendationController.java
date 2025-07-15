package com.tryiton.core.recommend.controller;

import com.tryiton.core.product.entity.Product;
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
    public ResponseEntity<List<Product>> getPersonalizedRecommendations(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "12") Integer limit) {

        List<Product> products = personalizedService
            .getPersonalizedRecommendations(userId, limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<Product>> getTrending() {
        List<Product> products = recommendationService.getTrendingProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/age-group")
    public ResponseEntity<List<Product>> getAgeGroupRecommendations(
        @RequestParam String ageRange,
        @RequestParam(required = false) String gender) {
        List<Product> products = recommendationService
            .getAgeGroupRecommendations(ageRange, gender);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/similar-to/{productId}")
    public ResponseEntity<List<Product>> getSimilarProducts(
        @PathVariable Long productId) {
        List<Product> products = recommendationService.getSimilarProducts(productId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/tryon-based")
    public ResponseEntity<List<Product>> getTryonRecommendations(
        @RequestParam Long userId) {
        List<Product> products = recommendationService
            .getTryonBasedRecommendations(userId);
        return ResponseEntity.ok(products);
    }

}
