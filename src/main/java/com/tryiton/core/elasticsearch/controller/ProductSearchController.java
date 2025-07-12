package com.tryiton.core.elasticsearch.controller;

import com.tryiton.core.elasticsearch.document.ProductDocument;
import com.tryiton.core.elasticsearch.service.ProductSearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String query) {
        return ResponseEntity.ok(productSearchService.getSuggestions(query));
    }

    @GetMapping
    public ResponseEntity<List<ProductDocument>> search(@RequestParam String query) {
        return ResponseEntity.ok(productSearchService.searchProducts(query));
    }
}
