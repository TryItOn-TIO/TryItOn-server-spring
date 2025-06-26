package com.tryiton.core.product.service;

import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.repository.CategoryRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NoSuchElementException("해당 카테고리를 찾을 수 없습니다. id=" + categoryId));
    }
}
