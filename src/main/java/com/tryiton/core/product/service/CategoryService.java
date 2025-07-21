package com.tryiton.core.product.service;

import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                "Category not found with id: " + categoryId));
    }

    @Cacheable(value = "categories", key = "#categoryId")
    public Category findByIdWithChildren(Long categoryId) {
        return categoryRepository.findByIdWithChildren(categoryId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                "Category not found with id: " + categoryId));
    }

    @Cacheable(value = "categoryTreeIds", key = "#categoryId")
    public List<Long> getCategoryAndAllChildrenIds(Long categoryId) {
        return categoryRepository.findAllChildrenIdsByParentId(categoryId);
    }
}