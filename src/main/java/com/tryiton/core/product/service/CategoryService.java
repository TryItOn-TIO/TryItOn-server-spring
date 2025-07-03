package com.tryiton.core.product.service;

import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Category not found with id: " + categoryId));
    }
}