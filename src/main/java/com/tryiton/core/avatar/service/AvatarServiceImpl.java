package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.request.AvatarBaseImageUpdateRequest;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.AvatarImageUploadCompleteRequest;
import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.FastApiTryOnRequest;
import com.tryiton.core.avatar.dto.request.InitialAvatarRequest;
import com.tryiton.core.avatar.dto.request.TryonAvatarTogetherNodeRequest;
import com.tryiton.core.avatar.dto.response.AvatarBaseImageUpdateResponse;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.AvatarImageUploadCompleteResponse;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.dto.response.FastApiTryOnResponse;
import com.tryiton.core.avatar.dto.response.InitialAvatarResponse;
import com.tryiton.core.avatar.dto.response.ResetAvatarResponse;
import com.tryiton.core.avatar.dto.response.TryonAvatarTogetherNodeResponse;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.repository.AvatarItemRepository;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.common.enums.RecommendAction;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.common.service.S3Service;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.recommend.service.RecommendBehaviorLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarServiceImpl implements AvatarService {

    private final AvatarRepository avatarRepository;
    private final AvatarItemRepository avatarItemRepository;
    private final MemberRepository memberRepository;
    private final WebClient fastApiWebClient;
    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final S3Service s3Service;

    private final RecommendBehaviorLogService recommendBehaviorLogService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    private String buildS3Url(String key) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    /**
     * 캐시 키 생성 (단일 상품용)
     */
    private String generateSingleItemCacheKey(Long userId, Product product) {
        String garmentType = determineGarmentType(product);
        return String.format("cache/tryon/%d/%s-%d.jpg", userId, garmentType, product.getId());
    }

    /**
     * 캐시 키 생성 (조합용 - 상의/하의)
     */
    private String generateCombinationCacheKey(Long userId, Long topId, Long bottomId) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("cache/tryon/").append(userId).append("/");
        
        if (topId != null && bottomId != null) {
            keyBuilder.append("top-").append(topId).append("_bottom-").append(bottomId);
        } else if (topId != null) {
            keyBuilder.append("top-").append(topId);
        } else if (bottomId != null) {
            keyBuilder.append("bottom-").append(bottomId);
        } else {
            keyBuilder.append("base");
        }
        
        keyBuilder.append(".jpg");
        return keyBuilder.toString();
    }

    /**
     * S3에 캐시 이미지가 존재하는지 확인
     */
    private boolean existsInS3(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.warn("S3 객체 존재 확인 중 오류 발생: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * S3 Public URL 생성
     */
    private String buildS3PublicUrl(String key) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    // 가장 최근 착장한 아바타 이미지 + 착용 상품명 리스트
    @Override
    @Transactional(readOnly = true)
    public AvatarTryOnResponse getLatestAvatarWithProducts(Long userId) {
        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId);

        // 아바타가 없을 때는?
        if (avatar == null) {
            return null;
        }

        List<AvatarTryOnResponse.ProductInfo> productInfos = avatar.getItems().stream()
            .map(item -> new AvatarTryOnResponse.ProductInfo(
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getProduct().getCategory().getCategoryName()
            ))
            .collect(Collectors.toList());

        return AvatarTryOnResponse.builder()
            .avatarId(avatar.getId())
            .avatarImgUrl(avatar.getAvatarImg())
            .products(productInfos)
            .build();
    }

    /**
     * 원본 이미지를 받아 마스크, 포즈 이미지를 생성하고 DB에 저장합니다.
     */
    @Transactional
    public AvatarCreateResponse createAvatar(Member member, AvatarCreateRequest avatarCreateRequest) {
        // 1. FastAPI 서버로 보낼 요청 DTO 생성
        String originalImgUrl = avatarCreateRequest.getTryOnImgUrl();
        log.info("FastAPI 요청 준비 - userId: {}, originalImgUrl: {}", member.getId(), originalImgUrl);
        
        InitialAvatarRequest fastApiRequest = new InitialAvatarRequest(member.getId(), originalImgUrl);
        log.info("FastAPI 요청 데이터: {}", fastApiRequest);

        // 2. WebClient를 사용하여 FastAPI 서버의 /generate 엔드포인트에 POST 요청
        InitialAvatarResponse fastApiResponse = fastApiWebClient.post()
            .uri("/generate")
            .bodyValue(fastApiRequest)
            .retrieve()
            .bodyToMono(InitialAvatarResponse.class)
            .block(); // 비동기 결과를 동기적으로 기다림 (실제 프로덕션에서는 비동기 체인 고려)

        // 3. FastAPI 응답 검증
        if (fastApiResponse == null || fastApiResponse.getPoseImgUrl() == null || fastApiResponse.getUpperMaskImgUrl() == null || fastApiResponse.getLowerMaskImgUrl() == null) {
            throw new RuntimeException("FastAPI 서버로부터 유효한 이미지 주소를 받지 못했습니다.");
        }

        // 4. 응답받은 이미지 주소들을 포함하여 Avatar 엔티티 생성
        Avatar newAvatar = Avatar.builder()
            .member(member)
            .avatarImg(originalImgUrl)
            .build();

        // 6. DB에 저장
        Avatar savedAvatar = avatarRepository.save(newAvatar);

        // 7. 최종 결과를 클라이언트에게 보낼 응답 DTO로 변환하여 반환
        return AvatarCreateResponse.fromEntity(savedAvatar);
    }

    /**
     * FastAPI 서버에 가상 피팅을 요청하고 결과 이미지 URL을 반환하는 헬퍼 메서드
     * 캐싱 기능이 추가되어 동일한 조합은 S3에서 바로 반환합니다.
     *
     * @param baseImgUrl 피팅의 기반이 될 이미지 URL
     * @param maskUrl    마스크 이미지 URL
     * @param poseUrl    포즈 이미지 URL
     * @param garment    피팅할 의류 상품
     * @param member     요청 사용자 정보
     * @return 생성된 이미지 URL, 실패 시 null 반환
     */
    private String performStatelessTryOn(String baseImgUrl, String maskUrl, String poseUrl, Product garment, Member member) {
        // 캐시 키 생성 (baseImgUrl과 garment 조합으로)
        String cacheKey = generateStatelessCacheKey(member.getId(), garment.getId(), baseImgUrl);

        // S3에 캐시된 이미지가 있는지 확인
        if (existsInS3(cacheKey)) {
            String cachedUrl = buildS3PublicUrl(cacheKey);
            log.info("Stateless 캐시 히트 - userId={}, garmentId={}, url={}",
                    member.getId(), garment.getId(), cachedUrl);
            return cachedUrl;
        }

        // 캐시 미스 - AI 서버에 요청
        log.info("Stateless 캐시 미스 - AI 서버 요청: userId={}, garmentId={}",
                member.getId(), garment.getId());

        String garmentType = determineGarmentType(garment);
        FastApiTryOnRequest fastApiRequest = new FastApiTryOnRequest(
            baseImgUrl,
            garment.getImg2(),
            maskUrl,
            poseUrl,
            member.getId(),
            garment.getId(),
            garmentType
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
            log.error("AI 서버 호출 실패: userId={}, garmentId={}, error={}",
                    member.getId(), garment.getId(), e.getMessage());
            return null; // 실패 시 null 반환
        }
        return null;
    }

    // TODO: together 기능은 현재 사용하지 않음 - 필요시 주석 해제
    /*
    @Transactional(readOnly = true)
    @Override
    public TryonAvatarTogetherNodeResponse tryonTogether(Member member,
        TryonAvatarTogetherNodeRequest request) {

        Profile profile = member.getProfile();
        Avatar baseAvatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(member.getId());

        // 2. 착용할 상품 목록을 조회합니다.
        List<Product> products = productRepository.findAllById(request.getProductIds());

        // 3. 상품을 상의와 하의로 분류합니다.
        List<Product> tops = products.stream()
            .filter(Product::isUpperGarment)
            .toList();

        List<Product> bottoms = products.stream()
            .filter(Product::isLowerGarment)
            .toList();

        List<TryonAvatarTogetherNodeResponse.TryonResult> results = new ArrayList<>();

        // 4. 상의 목록을 순회합니다.
        for (Product top : tops) {
            // 5. 하의 목록을 순회합니다.
            for (Product bottom : bottoms) {
                // 캐싱 키 생성
                String cacheKey = generateCombinationCacheKey(member.getId(), top.getId(), bottom.getId());

                String finalImgUrl = null;

                // S3에 캐시된 이미지가 있는지 확인
                if (existsInS3(cacheKey)) {
                    finalImgUrl = buildS3PublicUrl(cacheKey);
                    log.info("캐시 히트 - 조합 이미지 사용: userId={}, topId={}, bottomId={}, url={}",
                            member.getId(), top.getId(), bottom.getId(), finalImgUrl);
                } else {
                    // 캐시 미스 - 기존 로직으로 새로 생성 (파이썬이 S3에 업로드)
                    log.info("캐시 미스 - 새 조합 이미지 생성: userId={}, topId={}, bottomId={}",
                            member.getId(), top.getId(), bottom.getId());

                    String baseUrl = profile.getUserBaseImageUrl();
                    // 원본 아바타에 상의를 입혀 중간 결과 이미지를 생성합니다.
                    String topAppliedImgUrl = performStatelessTryOn(baseUrl,
                        buildS3Url(baseAvatar.getMaskUrl(top)), buildS3Url(baseAvatar.getPoseUrl()), top, member);

                    // 상의 피팅에 실패하면 다음 조합으로 넘어갑니다.
                    if (topAppliedImgUrl == null) {
                        continue;
                    }

                    // 상의가 적용된 이미지에 하의를 입혀 최종 결과 이미지를 생성합니다.
                    finalImgUrl = performStatelessTryOn(topAppliedImgUrl, buildS3Url(baseAvatar.getMaskUrl(bottom)),
                        buildS3Url(baseAvatar.getPoseUrl()), bottom, member);
                }

                // 최종 피팅에 성공한 경우에만 결과 리스트에 추가합니다.
                if (finalImgUrl != null) {
                    TryonAvatarTogetherNodeResponse.TryonResult result = TryonAvatarTogetherNodeResponse.TryonResult.builder()
                        .tryonImgUrl(finalImgUrl)
                        .topProductId(top.getId())
                        .topProductName(top.getProductName())
                        .topCategoryName(top.getCategory().getCategoryName())
                        .bottomProductId(bottom.getId())
                        .bottomProductName(bottom.getProductName())
                        .bottomCategoryName(bottom.getCategory().getCategoryName())
                        .build();
                    results.add(result);
                }
            }
        }

        return new TryonAvatarTogetherNodeResponse(results);
    }
    */

    @Transactional
    @Override
    public AvatarTryOnResponse tryOn(Member member, AvatarTryOnRequest avatarTryOnRequest) {
        Long userId = member.getId();
        Long productId = Long.parseLong(avatarTryOnRequest.getProductId());

        // 1. 사용자의 가장 최근 아바타를 조회합니다.
        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId);
        if (avatar == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "가상 피팅을 진행할 아바타가 존재하지 않습니다.");
        }

        // 2. 착용할 상품(의류)을 조회합니다.
        Product newGarment = productRepository.findByIdWithCategory(productId)
            .orElseThrow(() -> new IllegalArgumentException(
                "상품을 찾을 수 없습니다. ID: " + avatarTryOnRequest.getProductId()));

        // 3. 캐시 키 생성
        String cacheKey = generateSingleItemCacheKey(userId, newGarment);
        String finalImageUrl = null;

        // 4. S3에 캐시된 이미지가 있는지 확인
        if (existsInS3(cacheKey)) {
            // Cache Hit - 캐시된 이미지 사용
            finalImageUrl = buildS3PublicUrl(cacheKey);
            log.info("캐시 히트 - 기존 이미지 사용: userId={}, productId={}, url={}", 
                    userId, productId, finalImageUrl);
        } else {
            // Cache Miss - Python AI 서버에 새로운 이미지 생성 요청
            log.info("캐시 미스 - AI 서버에 새 이미지 생성 요청: userId={}, productId={}", 
                    userId, productId);

            // Avatar 엔티티의 비즈니스 로직을 호출하여 옷을 입힙니다.
            avatar.wearGarment(newGarment);

            // FastAPI 서버에 보낼 요청 DTO를 구성합니다.
            String garmentType = determineGarmentType(newGarment);
            FastApiTryOnRequest fastApiRequest = new FastApiTryOnRequest(
                member.getProfile().getUserBaseImageUrl(), // 원본 베이스 이미지 사용
                newGarment.getImg1(), // 상품의 착용샷 이미지
                buildS3Url(avatar.getMaskUrl(newGarment)),
                buildS3Url(avatar.getPoseUrl()),
                member.getId(),
                newGarment.getId(),
                garmentType
            );

            // WebClient를 사용하여 FastAPI 서버에 가상 피팅을 요청합니다.
            FastApiTryOnResponse fastApiResponse = fastApiWebClient.post()
                .uri("/tryon")
                .bodyValue(fastApiRequest)
                .retrieve()
                .bodyToMono(FastApiTryOnResponse.class)
                .block();

            if (fastApiResponse == null || fastApiResponse.getTryOnImgUrl() == null) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FastAPI 서버로부터 유효한 응답을 받지 못했습니다.");
            }

            finalImageUrl = fastApiResponse.getTryOnImgUrl();
        }

        // 5. 최종 생성된 이미지로 아바타의 이미지를 업데이트합니다.
        avatar.update(finalImageUrl);

        // 6. 현재 아바타가 입고 있는 모든 아이템 정보를 DTO 리스트로 변환합니다.
        List<AvatarTryOnResponse.ProductInfo> productInfos = avatar.getItems().stream()
            .map(item -> new AvatarTryOnResponse.ProductInfo(
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getProduct().getCategory().getCategoryName()
            ))
            .collect(Collectors.toList());

        // 유저 행동 로그 비동기 기록
        recommendBehaviorLogService.logUserAction(userId, newGarment.getId(), RecommendAction.TRYON);

        // 7. 최종 응답 DTO를 빌더로 생성하여 반환합니다.
        return AvatarTryOnResponse.builder()
            .avatarId(avatar.getId())
            .avatarImgUrl(finalImageUrl)
            .products(productInfos)
            .build();
    }

    @Transactional
    @Override
    public ResetAvatarResponse resetAvatar(Member member) {
        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(member.getId());
        avatar.resetAvatar(member.getProfile().getUserBaseImageUrl());
        return new ResetAvatarResponse();
    }

    @Transactional
    @Override
    public AvatarBaseImageUpdateResponse updateAvatarBaseImage(Member member, AvatarBaseImageUpdateRequest request) {
        try {
            log.info("아바타 베이스 이미지 업데이트 시작 - userId: {}, newImageUrl: {}",
                    member.getId(), request.getNewBaseImageUrl());

            // 1. 사용자 프로필 조회
            Profile profile = member.getProfile();
            if (profile == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "사용자 프로필을 찾을 수 없습니다.");
            }

            String oldBaseImageUrl = profile.getUserBaseImageUrl();
            log.info("기존 베이스 이미지 URL: {}", oldBaseImageUrl);

            // 2. 기존 베이스 이미지 삭제 (S3에서)
            if (oldBaseImageUrl != null && !oldBaseImageUrl.isEmpty()) {
                deleteOldAvatarImage(oldBaseImageUrl);
            }

            // 3. 기존 아바타 관련 이미지들 삭제 (마스크, 포즈 등)
            deleteOldAvatarAssets(member.getId());

            // 4. 프로필의 베이스 이미지 URL 업데이트
            profile.setUserBaseImageUrl(request.getNewBaseImageUrl());
            log.info("프로필 베이스 이미지 업데이트: {} -> {}", oldBaseImageUrl, request.getNewBaseImageUrl());

            // 5. 새로운 베이스 이미지로 아바타 에셋 생성 (마스크, 포즈 이미지)
            AvatarCreateRequest avatarCreateRequest = new AvatarCreateRequest(
                member.getId().toString(),
                request.getNewBaseImageUrl()
            );

            AvatarCreateResponse avatarCreateResponse = createAvatar(member, avatarCreateRequest);

            // 6. 기존 캐시 무효화 (선택사항)
            invalidateUserCache(member.getId());

            log.info("아바타 베이스 이미지 업데이트 완료 - userId: {}", member.getId());

            return AvatarBaseImageUpdateResponse.success(
                request.getNewBaseImageUrl(),
                avatarCreateResponse.getTryOnImgUrl()
            );

        } catch (BusinessException e) {
            log.error("아바타 베이스 이미지 업데이트 실패 - userId: {}, error: {}",
                    member.getId(), e.getMessage());
            return AvatarBaseImageUpdateResponse.failure(e.getMessage());
        } catch (Exception e) {
            log.error("아바타 베이스 이미지 업데이트 중 예상치 못한 오류 - userId: {}, error: {}",
                    member.getId(), e.getMessage());
            return AvatarBaseImageUpdateResponse.failure("아바타 베이스 이미지 업데이트 중 오류가 발생했습니다.");
        }
    }

    /**
     * 기존 아바타 베이스 이미지를 S3에서 삭제합니다.
     */
    private void deleteOldAvatarImage(String oldAvatarUrl) {
        try {
            String s3Key = s3Service.extractS3KeyFromUrl(oldAvatarUrl);
            if (s3Key != null) {
                s3Service.deleteObject(s3Key);
                log.info("기존 아바타 베이스 이미지 삭제 완료: {}", s3Key);
            } else {
                log.warn("S3 키 추출 실패, 삭제 건너뜀: {}", oldAvatarUrl);
            }
        } catch (Exception e) {
            log.warn("기존 아바타 베이스 이미지 삭제 실패: {}, 에러: {}", oldAvatarUrl, e.getMessage());
            // 삭제 실패해도 업데이트는 계속 진행
        }
    }

    /**
     * 기존 아바타 관련 에셋들(마스크, 포즈 등)을 S3에서 삭제합니다.
     */
    private void deleteOldAvatarAssets(Long userId) {
        try {
            // 사용자별 아바타 에셋 경로 패턴: users/{userId}/
            String baseKey = "users/" + userId + "/";

            // 일반적인 아바타 에셋 파일들 삭제
            String[] assetFiles = {
                "pose.png",
                "upper_mask.png",
                "lower_mask.png"
            };

            for (String assetFile : assetFiles) {
                String assetKey = baseKey + assetFile;
                s3Service.deleteObject(assetKey);
            }

            log.info("기존 아바타 에셋 삭제 완료 - userId: {}", userId);
        } catch (Exception e) {
            log.warn("기존 아바타 에셋 삭제 중 오류 - userId: {}, 에러: {}", userId, e.getMessage());
            // 삭제 실패해도 업데이트는 계속 진행
        }
    }

    /**
     * 조합 캐시 키 생성 (현재 사용하지 않음)
     */
    /*
    private String generateCombinationCacheKey(Long userId, Long topId, Long bottomId) {
        return String.format("combination/%d/%d-%d.jpg", userId, topId, bottomId);
    }
    */

    /**
     * Stateless 피팅 캐시 키 생성 (중간 단계 이미지용)
     */
    private String generateStatelessCacheKey(Long userId, Long garmentId, String baseImgUrl) {
        // baseImgUrl에서 해시값을 생성하여 캐시 키에 포함
        int baseImgHash = baseImgUrl.hashCode();
        return String.format("stateless/%d/%d-%d.jpg", userId, garmentId, Math.abs(baseImgHash));
    }

    /**
     * 상품의 garmentType을 안전하게 판단
     */
    private String determineGarmentType(Product product) {
        if (product.isUpperGarment()) {
            return "top";
        } else if (product.isLowerGarment()) {
            return "bottom";
        } else {
            // 상의도 하의도 아닌 경우 (액세서리 등)
            log.warn("상품 ID {}는 상의도 하의도 아닙니다. 카테고리: {}",
                    product.getId(),
                    product.getCategory() != null ? product.getCategory().getCategoryName() : "null");
            return "unknown"; // 또는 기본값 설정
        }
    }

    /**
     * 사용자의 모든 캐시를 무효화합니다.
     * 베이스 이미지가 변경되면 기존 캐시된 이미지들이 더 이상 유효하지 않기 때문입니다.
     */
    private void invalidateUserCache(Long userId) {
        try {
            log.info("사용자 캐시 무효화 시작 - userId: {}", userId);

            // S3에서 해당 사용자의 캐시 파일들을 삭제
            String cachePrefix = "cache/tryon/" + userId + "/";
            deleteS3ObjectsWithPrefix(cachePrefix);

            log.info("사용자 캐시 무효화 완료 - userId: {}", userId);

        } catch (Exception e) {
            log.warn("캐시 무효화 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage());
            // 캐시 무효화 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }

    /**
     * S3에서 특정 prefix로 시작하는 객체들을 삭제합니다.
     */
    private void deleteS3ObjectsWithPrefix(String prefix) {
        try {
            // S3에서 prefix로 시작하는 객체 목록 조회
            var listRequest = software.amazon.awssdk.services.s3.model.ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            var listResponse = s3Client.listObjectsV2(listRequest);

            // 각 객체 삭제
            for (var s3Object : listResponse.contents()) {
                var deleteRequest = software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Object.key())
                        .build();

                s3Client.deleteObject(deleteRequest);
                log.debug("캐시 파일 삭제: {}", s3Object.key());
            }

            log.info("prefix '{}' 캐시 파일 {} 개 삭제 완료", prefix, listResponse.contents().size());

        } catch (Exception e) {
            log.error("S3 객체 삭제 실패 - prefix: {}, error: {}", prefix, e.getMessage());
        }
    }

    @Transactional
    @Override
    public AvatarImageUploadCompleteResponse processAvatarImageUploadComplete(Member member, AvatarImageUploadCompleteRequest request) {
        try {
            log.info("아바타 이미지 업로드 완료 처리 시작 - userId: {}, newImageUrl: {}",
                    member.getId(), request.getNewAvatarImageUrl());

            // 요청 데이터 검증
            if (request.getNewAvatarImageUrl() == null || request.getNewAvatarImageUrl().trim().isEmpty()) {
                log.error("새 아바타 이미지 URL이 비어있습니다 - userId: {}", member.getId());
                return AvatarImageUploadCompleteResponse.failure("새 아바타 이미지 URL이 제공되지 않았습니다.");
            }

            // 1. 프로필의 베이스 이미지 URL 업데이트 (회원가입과 동일)
            Profile profile = member.getProfile();
            if (profile == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "사용자 프로필을 찾을 수 없습니다.");
            }
            
            String oldBaseImageUrl = profile.getUserBaseImageUrl();
            profile.setUserBaseImageUrl(request.getNewAvatarImageUrl());
            log.info("프로필 베이스 이미지 업데이트: {} -> {}", oldBaseImageUrl, request.getNewAvatarImageUrl());

            // 2. 아바타 생성 (회원가입과 동일한 방식)
            AvatarCreateRequest avatarCreateRequest = new AvatarCreateRequest(
                member.getId().toString(),
                request.getNewAvatarImageUrl()
            );
            
            AvatarCreateResponse avatarCreateResponse = createAvatar(member, avatarCreateRequest);

            // 3. 기존 캐시 무효화
            invalidateUserCache(member.getId());

            log.info("아바타 이미지 업로드 완료 처리 완료 - userId: {}", member.getId());

            return AvatarImageUploadCompleteResponse.success(
                request.getNewAvatarImageUrl(),
                avatarCreateResponse.getTryOnImgUrl()
            );

        } catch (BusinessException e) {
            log.error("아바타 이미지 업로드 완료 처리 실패 - userId: {}, error: {}",
                    member.getId(), e.getMessage());
            return AvatarImageUploadCompleteResponse.failure(e.getMessage());
        } catch (Exception e) {
            log.error("아바타 이미지 업로드 완료 처리 중 예상치 못한 오류 - userId: {}, error: {}",
                    member.getId(), e.getMessage());
            return AvatarImageUploadCompleteResponse.failure("아바타 이미지 업로드 완료 처리 중 오류가 발생했습니다.");
        }
    }

}