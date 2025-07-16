package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RemoveBgRequest {
    private String imageUrl;
    private String size;      // "auto", "preview", "full"
    private String format;    // "auto", "png", "jpg"
    
    public RemoveBgRequest(String imageUrl) {
        this.imageUrl = imageUrl;
        this.size = "auto";
        this.format = "png";
    }
}
