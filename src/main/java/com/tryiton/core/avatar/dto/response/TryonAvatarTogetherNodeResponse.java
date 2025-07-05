package com.tryiton.core.avatar.dto.response;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TryonAvatarTogetherNodeResponse {

    private List<TryonResult> combinations;

    @Getter
    @Builder
    public static class TryonResult {
        private String tryonImgUrl;
        private String topProductName;
        private String topCategoryName;
        private String bottomProductName;
        private String bottomCategoryName;
    }
}