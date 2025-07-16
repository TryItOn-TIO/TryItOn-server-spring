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
        return ResponseEntity.ok(products);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ProductResponseDto>> getTrending() {
        List<ProductResponseDto> products = recommendationService.getTrendingProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/age-group")
    public ResponseEntity<List<ProductResponseDto>> getAgeGroupRecommendations(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestParam(required = false) String gender) {
        LocalDate birthDate = customUserDetails.getUser().getBirthDate();
        String ageRange = "20s";

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
        @AuthenticationPrincipal() CustomUserDetails customUserDetails) {
        List<ProductResponseDto> products = recommendationService
            .getTryonBasedRecommendations(customUserDetails.getUser().getId());
        return ResponseEntity.ok(products);
    }

}
