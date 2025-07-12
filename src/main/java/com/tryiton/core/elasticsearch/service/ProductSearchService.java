package com.tryiton.core.elasticsearch.service;

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.prefix;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tryiton.core.elasticsearch.document.ProductDocument;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME = "products";

    public List<String> getSuggestions(String keyword) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .size(10)
                .query(q -> q
                    .bool(b -> b
                        .should(prefix(p -> p.field("productName").value(keyword)))
                        .should(prefix(p -> p.field("brand").value(keyword)))
                    )
                )
            );

            SearchResponse<ProductDocument> response = elasticsearchClient.search(request, ProductDocument.class);

            return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .flatMap(doc -> Stream.of(doc.getProductName(), doc.getBrand()))
                .filter(name -> name.toLowerCase().startsWith(keyword.toLowerCase()))
                .distinct()
                .limit(6)
                .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("자동완성 검색 실패", e);
        }
    }

    public List<ProductDocument> searchProducts(String keyword) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .size(20)
                .query(q -> q
                    .multiMatch(m -> m
                        .query(keyword)
                        .fields("productName", "brand")
                        .fuzziness("AUTO") // 오타 허용 (예: "hoode" → "hoodie")
                    )
                )
            );

            SearchResponse<ProductDocument> response = elasticsearchClient.search(request, ProductDocument.class);

            return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("검색 실패", e);
        }
    }
}
