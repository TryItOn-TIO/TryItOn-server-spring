package com.tryiton.core.avatar.dto.response;

public class AvatarTryOnResponse {
    // 가상 피팅이 완료된 최종 이미지의 S3 URL
    private final String tryOnImgUrl;

    // private 생성자로 팩토리 메서드를 통한 생성을 유도합니다.
    private AvatarTryOnResponse(String tryOnImgUrl) {
        this.tryOnImgUrl = tryOnImgUrl;
    }

    /**
     * 최종 이미지 URL을 사용하여 응답 객체를 생성하는 정적 팩토리 메서드
     * @param tryOnImgUrl 최종 생성된 이미지의 S3 URL
     * @return AvatarTryOnResponse 객체
     */
    public static AvatarTryOnResponse of(String tryOnImgUrl) {
        return new AvatarTryOnResponse(tryOnImgUrl);
    }
}
