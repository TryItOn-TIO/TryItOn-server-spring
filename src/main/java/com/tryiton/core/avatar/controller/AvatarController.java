package com.tryiton.core.avatar.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.TryonAvatarTogetherNodeRequest;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.dto.response.TryonAvatarTogetherNodeResponse;
import com.tryiton.core.avatar.service.AvatarService;
import com.tryiton.core.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/avatars") // API 버전 관리를 위한 경로 설정
public class AvatarController {

    private final AvatarService avatarService;

    /**
     * 최신 아바타 정보 조회 API 사용자의 가장 최근 아바타 이미지와 착용 상품 정보를 반환합니다.
     */
    @GetMapping("/latest-info")
    public ResponseEntity<AvatarProductInfoDto> getLatestAvatarInfo(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails
    ) {
        Long currentUserId = customUserDetails.getUser().getId();

        AvatarProductInfoDto avatarInfo = avatarService.getLatestAvatarWithProducts(currentUserId);

        if (avatarInfo == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(avatarInfo);
    }

    /**
     * 아바타 에셋 생성 API 사용자의 원본 이미지를 받아 마스크 및 포즈 이미지를 생성하고, 생성된 에셋 정보(URL)를 DB에 저장한 후 반환합니다.
     *
     * @param customUserDetails   현재 인증된 사용자 정보 (Spring Security가 주입)
     * @param avatarCreateRequest 원본 이미지 URL이 담긴 요청 DTO
     * @return 생성된 아바타 에셋 정보가 담긴 응답 DTO
     */
    @PostMapping
    public ResponseEntity<AvatarCreateResponse> createAvatarAssets(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestBody AvatarCreateRequest avatarCreateRequest
    ) {
        Member member = customUserDetails.getUser();

        // 서비스 레이어의 create 메서드 호출
        AvatarCreateResponse response = avatarService.createAvatar(member, avatarCreateRequest);

        // 생성 성공 시 201 Created 상태 코드와 함께 결과 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 가상 피팅(Try-On) API 베이스 이미지, 의상, 마스크, 포즈 정보를 받아 가상 피팅을 수행하고 최종 결과 이미지의 URL을 반환합니다.
     *
     * @param customUserDetails  현재 인증된 사용자 정보
     * @param avatarTryOnRequest 가상 피팅에 필요한 모든 이미지 URL이 담긴 요청 DTO
     * @return 최종 Try-on 이미지 URL이 담긴 응답 DTO
     */
    @PostMapping("/try-on")
    public ResponseEntity<AvatarTryOnResponse> performTryOn(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestBody AvatarTryOnRequest avatarTryOnRequest
    ) {
        Member member = customUserDetails.getUser();
        // 서비스 레이어의 tryOn 메서드 호출
        AvatarTryOnResponse response = avatarService.tryOn(member, avatarTryOnRequest);

        // 성공 시 200 OK 상태 코드와 함께 결과 반환
        return ResponseEntity.ok(response);
    }

    @PostMapping("/together")
    public ResponseEntity<TryonAvatarTogetherNodeResponse> tryonTogether(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestBody TryonAvatarTogetherNodeRequest tryonAvatarTogetherNodeRequest
    ) {
        Member member = customUserDetails.getUser();
        TryonAvatarTogetherNodeResponse response = avatarService.tryonTogether(member,
            tryonAvatarTogetherNodeRequest);
        return ResponseEntity.ok(response);
    }
}
