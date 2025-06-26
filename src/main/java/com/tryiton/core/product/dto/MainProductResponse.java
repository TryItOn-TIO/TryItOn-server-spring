package com.tryiton.core.product.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class MainProductResponse {

    private List<ProductResponseDto> recommended;
    private Page<ProductResponseDto> ranked;
}
