package com.tryiton.core.product.controller;

import com.tryiton.core.product.dto.MainProductGuestResponse;
import com.tryiton.core.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class MainProductController {

    private final ProductService productService;

    @GetMapping("/main")
    public ResponseEntity<MainProductGuestResponse> getMainProducts() {
        try {
            MainProductGuestResponse response = productService.getMainPageProductsForGuest();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MainProductGuestResponse.error("상품 조회에 실패했습니다."));
        }
    }
}
