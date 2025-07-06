package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.FastApiTogetherRequest;
import com.tryiton.core.avatar.dto.request.FastApiTryOnRequest;
import com.tryiton.core.avatar.dto.request.InitialAvatarRequest;
import com.tryiton.core.avatar.dto.request.TryonAvatarTogetherNodeRequest;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.dto.response.FastApiTryOnResponse;
import com.tryiton.core.avatar.dto.response.InitialAvatarResponse;
import com.tryiton.core.avatar.dto.response.TryonAvatarTogetherNodeResponse;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.repository.AvatarItemRepository;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import java.util.ArrayList;
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

    // 공통 로직: 해당 아바타가 입은 상품명 리스트 추출
    private List<String> getProductNamesOfAvatar(Avatar avatar) {
        return avatarItemRepository.findAllByAvatar(avatar).stream()
            .map(item -> item.getProduct().getProductName())
            .collect(Collectors.toList());
    }

    /**
     * 원본 이미지를 받아 마스크, 포즈 이미지를 생성하고 DB에 저장합니다.
     */
    public AvatarCreateResponse create(Member member, AvatarCreateRequest avatarCreateRequest) {
        // 1. FastAPI 서버로 보낼 요청 DTO 생성
        String originalImgUrl = avatarCreateRequest.getTryOnImgUrl();

        // 2. 응답받은 이미지 주소들을 포함하여 Avatar 엔티티 생성
        Avatar newAvatar = Avatar.builder()
            .avatarImg(originalImgUrl)
            .build();

        // 3. 연관관계 매핑
        newAvatar.setMappingUser(member);

        // 4. DB에 저장
        Avatar savedAvatar = avatarRepository.save(newAvatar);

        // 5. 최종 결과를 클라이언트에게 보낼 응답 DTO로 변환하여 반환
        return AvatarCreateResponse.fromEntity(savedAvatar);
    }

    /**
     * FastAPI 서버에 가상 피팅을 요청하고 결과 이미지 URL을 반환하는 헬퍼 메서드 //Todo: 메서드 명 바꾸기
     * @param baseImgUrl 피팅의 기반이 될 이미지 URL
     * @param garment 피팅할 의류 상품
     * @param member 요청 사용자 정보
     * @return 생성된 이미지 URL, 실패 시 null 반환
     */
    private String performStatelessTryOn(String baseImgUrl, Product garment, Member member) {
        FastApiTryOnRequest fastApiRequest = new FastApiTryOnRequest(
            baseImgUrl,
            garment.getImg2(),
            garment.getCategory().getCategoryName(), // 마스크 URL 대신 카테고리 이름을 사용
            member.getId()
        );

        try {
            FastApiTryOnResponse response = fastApiWebClient.post()
                .uri("/tryon") // 단일 피팅 엔드포인트
                .bodyValue(fastApiRequest)
                .retrieve()
                .bodyToMono(FastApiTryOnResponse.class)
                .block(); // 비동기 작업을 동기적으로 기다립니다.

            if (response != null && response.getTryOnImgUrl() != null) {
                return response.getTryOnImgUrl();
            }
        } catch (Exception e) {
            // API 호출 실패 시 에러 로그를 남기는 것이 좋습니다. 예: log.error(...)
            return null; // 실패 시 null 반환
        }
        return null;
    }



    @Transactional(readOnly = true)
    @Override
    public TryonAvatarTogetherNodeResponse tryonTogether(Member member,
        TryonAvatarTogetherNodeRequest request) {

        Long userId = member.getId();
        // 1. 피팅의 기반이 될 사용자의 가장 최근 아바타를 조회합니다.  // todo: 리펙토링 해야함 / 피팅의 기반이 되어선 안됨 = 사용자의 가장 최신 아바타
        Avatar baseAvatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId);
        if (baseAvatar == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "가상 피팅을 진행할 아바타가 존재하지 않습니다.");
        }
        String originalAvatarImg = baseAvatar.getAvatarImg();

        // 2. 착용할 상의 및 하의 상품 목록을 조회합니다.
        List<Product> tops = productRepository.findAllById(request.getTopProductIds());
        List<Product> bottoms = productRepository.findAllById(request.getBottomProductIds());

        List<TryonAvatarTogetherNodeResponse.TryonResult> results = new ArrayList<>();

        // 3. 상의 목록을 순회합니다.
        for (Product top : tops) {
            // 3-1. 원본 아바타에 상의를 입혀 중간 결과 이미지를 생성합니다.
            String topAppliedImgUrl = performStatelessTryOn(originalAvatarImg, top, member);

            // 상의 피팅에 실패하면 다음 상의로 넘어갑니다.
            if (topAppliedImgUrl == null) {
                continue;
            }

            // 4. 하의 목록을 순회합니다.
            for (Product bottom : bottoms) {
                // 4-1. 상의가 적용된 이미지에 하의를 입혀 최종 결과 이미지를 생성합니다.
                String finalImgUrl = performStatelessTryOn(topAppliedImgUrl, bottom, member);

                // 최종 피팅에 성공한 경우에만 결과 리스트에 추가합니다.
                if (finalImgUrl != null) {
                    TryonAvatarTogetherNodeResponse.TryonResult result = TryonAvatarTogetherNodeResponse.TryonResult.builder()
                        .tryonImgUrl(finalImgUrl)
                        .topProductName(top.getProductName())
                        .topCategoryName(top.getCategory().getCategoryName())
                        .bottomProductName(bottom.getProductName())
                        .bottomCategoryName(bottom.getCategory().getCategoryName())
                        .build();
                    results.add(result);
                }
            }
        }

        return new TryonAvatarTogetherNodeResponse(results);
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

        // 4. FastAPI 서버에 보낼 요청 DTO를 구성합니다.
        FastApiTryOnRequest fastApiRequest = new FastApiTryOnRequest(
            avatar.getAvatarImg(),
            newGarment.getImg2(), // 상품의 착용샷 이미지
            newGarment.getCategory().getCategoryName(), // 마스크 URL 대신 카테고리 이름을 사용
            member.getId()
        );

        // 5. WebClient를 사용하여 FastAPI 서버에 가상 피팅을 요청합니다.
        FastApiTryOnResponse fastApiResponse = fastApiWebClient.post()
            .uri("/tryon")
            .bodyValue(fastApiRequest)
            .retrieve()
            .bodyToMono(FastApiTryOnResponse.class)
            .block();

        if (fastApiResponse == null || fastApiResponse.getTryOnImgUrl() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "FastAPI 서버로부터 유효한 응답을 받지 못했습니다.");
        }

        // 6. 최종 생성된 이미지로 아바타의 이미지를 업데이트합니다.
        avatar.update(fastApiResponse.getTryOnImgUrl());

        // 7. 현재 아바타가 입고 있는 모든 아이템 정보를 DTO 리스트로 변환합니다.
        List<AvatarTryOnResponse.ProductInfo> productInfos = avatar.getItems().stream()
            .map(item -> new AvatarTryOnResponse.ProductInfo(
                item.getProduct().getProductName(),
                item.getProduct().getCategory().getCategoryName()
            ))
            .collect(Collectors.toList());

        // 8. 최종 응답 DTO를 빌더로 생성하여 반환합니다.
        return AvatarTryOnResponse.builder()
            .avatarId(avatar.getId())
            .avatarImgUrl(fastApiResponse.getTryOnImgUrl())
            .products(productInfos)
            .build();
    }


}
