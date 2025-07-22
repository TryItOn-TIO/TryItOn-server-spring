package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.request.AvatarBaseImageUpdateRequest;
import com.tryiton.core.avatar.dto.request.AvatarImageUploadCompleteRequest;
import com.tryiton.core.avatar.dto.request.TryonAvatarTogetherNodeRequest;
import com.tryiton.core.avatar.dto.response.AvatarBaseImageUpdateResponse;
import com.tryiton.core.avatar.dto.response.AvatarImageUploadCompleteResponse;
import com.tryiton.core.avatar.dto.response.ResetAvatarResponse;
import com.tryiton.core.avatar.dto.response.TryonAvatarTogetherNodeResponse;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
public interface AvatarService {
    AvatarTryOnResponse getLatestAvatarWithProducts(Long userId);

    AvatarTryOnResponse tryOn(Member member, AvatarTryOnRequest avatarTryOnRequest);

    AvatarCreateResponse createAvatar(Member member, AvatarCreateRequest avatarCreateRequest);

    // TODO: together 기능은 현재 사용하지 않음 - 필요시 주석 해제
    // TryonAvatarTogetherNodeResponse tryonTogether(Member member, TryonAvatarTogetherNodeRequest tryonAvatarTogetherNodeRequest);

    ResetAvatarResponse resetAvatar(Member member);
    
    /**
     * 사용자의 아바타 베이스 이미지를 업데이트합니다.
     * 새로운 베이스 이미지로 마스크, 포즈 이미지를 재생성하고 기존 캐시를 무효화합니다.
     */
    AvatarBaseImageUpdateResponse updateAvatarBaseImage(Member member, AvatarBaseImageUpdateRequest request);
    
    /**
     * 프리사인드 URL로 업로드된 아바타 이미지의 후처리를 수행합니다.
     * 기존 이미지 삭제, DB 업데이트, 새 아바타 에셋 생성을 처리합니다.
     */
    AvatarImageUploadCompleteResponse processAvatarImageUploadComplete(Member member, AvatarImageUploadCompleteRequest request);
    
    /**
     * 현재 입고 있는 옷의 캐시를 삭제합니다.
     * AI가 옷을 잘못 처리해서 뭉개지거나 하는 경우 캐시를 삭제하여 다시 렌더링할 수 있도록 합니다.
     * 
     * @param member 현재 로그인한 사용자
     * @return 캐시 삭제 성공 여부와 메시지를 담은 응답 객체
     */
    boolean clearCurrentOutfitCache(Member member);
}
