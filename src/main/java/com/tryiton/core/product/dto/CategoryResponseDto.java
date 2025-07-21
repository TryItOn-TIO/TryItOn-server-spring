package com.tryiton.core.product.dto;

import com.tryiton.core.product.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {
    private Long id;
    private String categoryName;
    private List<CategoryResponseDto> children;

    public static CategoryResponseDto from(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getCategoryName(),
                category.getChildren().stream()
                        .map(CategoryResponseDto::from)
                        .collect(Collectors.toList())
        );
    }
}