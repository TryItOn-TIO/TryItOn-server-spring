package com.tryiton.core.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ProductRepository productRepository;

    private static final String INDEX_NAME = "products";

    @PostConstruct
    public void init() throws IOException {
        createIndexIfNotExists(); // 인덱스 없으면 생성
        uploadAllProducts(); // 전체 상품 업로드
    }

    private void createIndexIfNotExists() throws IOException {
        boolean exists = elasticsearchClient.indices()
            .exists(e -> e.index(INDEX_NAME))
            .value();

        if (!exists) {
            elasticsearchClient.indices().create(c -> c.index(INDEX_NAME));
            System.out.println("인덱스 생성 완료: " + INDEX_NAME);
        } else {
            System.out.println("인덱스 이미 존재함: " + INDEX_NAME);
        }
    }

    private void uploadAllProducts() throws IOException {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            ProductIndex doc = ProductIndex.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .brand(product.getBrand())
                .build();

            IndexRequest<ProductIndex> request = IndexRequest.of(i -> i
                .index(INDEX_NAME)
                .id(String.valueOf(product.getId()))
                .document(doc)
            );

            elasticsearchClient.index(request);
        }

        System.out.println("상품 업로드 완료: " + products.size() + "개");
    }
}
