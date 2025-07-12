package com.tryiton.core.elasticsearch.repository;

import com.tryiton.core.elasticsearch.document.ProductDocument;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByProductNameContainingOrBrandContaining(String productName, String brand);
}
