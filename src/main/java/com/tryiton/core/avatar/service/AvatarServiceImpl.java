package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.FastApiTryOnRequest;
import com.tryiton.core.avatar.dto.request.InitialAvatarRequest;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.dto.response.FastApiTryOnResponse;
import com.tryiton.core.avatar.dto.response.InitialAvatarResponse;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.repository.AvatarItemRepository;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarServiceImpl implements AvatarService {

    private final AvatarRepository avatarRepository;
    private final AvatarItemRepository avatarItemRepository;
    private final MemberRepository memberRepository;
    private final WebClient fastApiWebClient;
    private final ProductRepository productRepository;

    // 가장 최근 착장한 아바타 이미지 + 착용 상품명 리스트
    @Override
    public AvatarProductInfoDto getLatestAvatarWithProducts(Long userId) {
        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId);

        // 아바타가 없을 때는?
        if (avatar == null) {
            return null;
        }

        List<String> productNames = getProductNamesOfAvatar(avatar);
        return new AvatarProductInfoDto(avatar.getAvatarImg(), productNames);
    }

    // 북마크된 아바타들 최대 10개 + 각 착장 상품 정보 포함
    @Override
    public List<AvatarProductInfoDto> getBookmarkedAvatarsWithProducts(Long userId) {
        Member user = memberRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다."));

        return avatarRepository.findAllByMemberAndIsBookmarkedTrueOrderByCreatedAtDesc(user).stream()
            .limit(10)
            .map(avatar -> new AvatarProductInfoDto(
                avatar.getAvatarImg(),
                getProductNamesOfAvatar(avatar)
            ))
            .collect(Collectors.toList());
    }

    // 공통 로직: 해당 아바타가 입은 상품명 리스트 추출
    private List<String> getProductNamesOfAvatar(Avatar avatar) {
        return avatarItemRepository.findAllByAvatar(avatar).stream()
            .map(item -> item.getProduct().getProductName())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateBookmark(Long avatarId, Long userId, boolean bookmark) {
        Member user = memberRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다."));

        Avatar avatar = avatarRepository.findByIdAndMember(avatarId, user)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "해당 아바타가 존재하지 않습니다."));

        avatar.setBookmarked(bookmark);
    }

    /**
     * 원본 이미지를 받아 마스크, 포즈 이미지를 생성하고 DB에 저장합니다.
     */
    public AvatarCreateResponse create(Member member, AvatarCreateRequest avatarCreateRequest) {
        // 1. FastAPI 서버로 보낼 요청 DTO 생성
        String originalImgUrl = avatarCreateRequest.getTryOnImgUrl();
        InitialAvatarRequest fastApiRequest = new InitialAvatarRequest(member.getId(), originalImgUrl);

        // 2. WebClient를 사용하여 FastAPI 서버의 /generate 엔드포인트에 POST 요청
        InitialAvatarResponse fastApiResponse = fastApiWebClient.post()
            .uri("/generate")
            .bodyValue(fastApiRequest)
            .retrieve()
            .bodyToMono(InitialAvatarResponse.class)
            .block(); // 비동기 결과를 동기적으로 기다림 (실제 프로덕션에서는 비동기 체인 고려)

        // 3. FastAPI 응답 검증
        if (fastApiResponse == null || fastApiResponse.getPoseImgUrl() == null || fastApiResponse.getUpperMaskImgUrl() == null || fastApiResponse.getLowerMaskImgUrl() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "FastAPI 서버로부터 유효한 이미지 주소를 받지 못했습니다.");
        }

        // 4. 응답받은 이미지 주소들을 포함하여 Avatar 엔티티 생성
        Avatar newAvatar = Avatar.builder()
            .avatarImg(originalImgUrl)
            .poseImg(fastApiResponse.getPoseImgUrl())
            .upperMaskImg(fastApiResponse.getUpperMaskImgUrl())
            .lowerMaskImg(fastApiResponse.getLowerMaskImgUrl())
            .build();

        // 5. 연관관계 매핑
        newAvatar.setMappingUser(member);

        // 6. DB에 저장
        Avatar savedAvatar = avatarRepository.save(newAvatar);

        // 7. 최종 결과를 클라이언트에게 보낼 응답 DTO로 변환하여 반환
        return AvatarCreateResponse.fromEntity(savedAvatar);
    }

    @Transactional
    @Override
    public AvatarTryOnResponse tryOn(Member member, AvatarTryOnRequest avatarTryOnRequest) {
        Long userId = member.getId();
        // 1. 사용자의 가장 최근 아바타를 조회합니다.
        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId);
        if (avatar == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "가상 피팅을 진행할 아바타가 존재하지 않습니다.");
        }

        // 2. 착용할 상품(의류)을 조회합니다.
        Product newGarment = productRepository.findById(Long.parseLong(avatarTryOnRequest.getProductId()))
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + avatarTryOnRequest.getProductId()));

        // 3. Avatar 엔티티의 비즈니스 로직을 호출하여 옷을 입힙니다.
        //    (내부적으로 상/하의 중복 착용을 처리합니다)
        avatar.wearGarment(newGarment);

        // 4. 의류 종류에 맞는 마스크 URL을 결정합니다.
        String maskUrl = newGarment.isUpperGarment() ? avatar.getUpperMaskImg() : avatar.getLowerMaskImg();

        // 5. FastAPI 서버에 보낼 요청 DTO를 구성합니다.
        FastApiTryOnRequest fastApiRequest = new FastApiTryOnRequest(
            avatar.getAvatarImg(),
            newGarment.getImg2(), // 상품의 착용샷 이미지
            maskUrl,
            avatar.getPoseImg(),
            member.getId()
        );

        // 6. WebClient를 사용하여 FastAPI 서버에 가상 피팅을 요청합니다.
        FastApiTryOnResponse fastApiResponse = fastApiWebClient.post()
            .uri("/tryon")
            .bodyValue(fastApiRequest)
            .retrieve()
            .bodyToMono(FastApiTryOnResponse.class)
            .block();

        if (fastApiResponse == null || fastApiResponse.getTryOnImgUrl() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "FastAPI 서버로부터 유효한 응답을 받지 못했습니다.");
        }

        // 7. 최종 생성된 이미지로 아바타의 이미지를 업데이트합니다.
        avatar.update(fastApiResponse.getTryOnImgUrl());

        // 8. 현재 아바타가 입고 있는 모든 아이템 정보를 DTO 리스트로 변환합니다.
        List<AvatarTryOnResponse.ProductInfo> productInfos = avatar.getItems().stream()
            .map(item -> new AvatarTryOnResponse.ProductInfo(
                item.getProduct().getProductName(),
                item.getProduct().getCategory().getCategoryName()
            ))
            .collect(Collectors.toList());

        // 9. 최종 응답 DTO를 빌더로 생성하여 반환합니다.
        return AvatarTryOnResponse.builder()
            .avatarId(avatar.getId())
            .avatarImgUrl(fastApiResponse.getTryOnImgUrl())
            .products(productInfos)
            .build();
    }
}
