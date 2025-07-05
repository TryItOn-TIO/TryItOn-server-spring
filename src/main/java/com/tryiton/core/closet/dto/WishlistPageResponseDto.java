package com.tryiton.core.closet.dto;

import com.tryiton.core.product.dto.ProductResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistPageResponseDto {
    
    private List<ProductResponseDto> products;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;
    private boolean last;
    
    public static WishlistPageResponseDto from(Page<ProductResponseDto> page) {
        return WishlistPageResponseDto.builder()
                .products(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .last(page.isLast())
                .build();
    }
}
