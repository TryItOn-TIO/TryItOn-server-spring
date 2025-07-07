package com.tryiton.core.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainProductGuestResponse {
    private boolean success;
    private String message;
    private List<CategoryProductGroup> categories;
    
    public static MainProductGuestResponse success(List<CategoryProductGroup> categories) {
        return new MainProductGuestResponse(true, "성공", categories);
    }
    
    public static MainProductGuestResponse error(String message) {
        return new MainProductGuestResponse(false, message, null);
    }
}
