package com.tryiton.core.avatar.controller;

import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.service.AvatarService;
import com.tryiton.core.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * 아바타 에셋 생성 API
     * 사용자의 원본 이미지를 받아 마스크 및 포즈 이미지를 생성하고,
     * 생성된 에셋 정보(URL)를 DB에 저장한 후 반환합니다.
     *
     * @param member 현재 인증된 사용자 정보 (Spring Security가 주입)
     * @param avatarCreateRequest 원본 이미지 URL이 담긴 요청 DTO
     * @return 생성된 아바타 에셋 정보가 담긴 응답 DTO
     */
    @PostMapping
    public ResponseEntity<AvatarCreateResponse> createAvatarAssets(
        @AuthenticationPrincipal Member member, // 실제 Spring Security의 UserDetails 구현체로 변경 필요
        @RequestBody AvatarCreateRequest avatarCreateRequest
    ) {
        // 서비스 레이어의 create 메서드 호출
        AvatarCreateResponse response = avatarService.create(member, avatarCreateRequest);

        // 생성 성공 시 201 Created 상태 코드와 함께 결과 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 가상 피팅(Try-On) API
     * 베이스 이미지, 의상, 마스크, 포즈 정보를 받아 가상 피팅을 수행하고
     * 최종 결과 이미지의 URL을 반환합니다.
     *
     * @param member 현재 인증된 사용자 정보
     * @param avatarTryOnRequest 가상 피팅에 필요한 모든 이미지 URL이 담긴 요청 DTO
     * @return 최종 Try-on 이미지 URL이 담긴 응답 DTO
     */
    @PostMapping("/try-on")
    public ResponseEntity<AvatarTryOnResponse> performTryOn(
        @AuthenticationPrincipal Member member,
        @RequestBody AvatarTryOnRequest avatarTryOnRequest
    ) {
        // 서비스 레이어의 tryOn 메서드 호출
        AvatarTryOnResponse response = avatarService.tryOn(member, avatarTryOnRequest);

        // 성공 시 200 OK 상태 코드와 함께 결과 반환
        return ResponseEntity.ok(response);
    }
}
