package com.tryiton.core.avatar.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.avatar.dto.request.AvatarBaseImageUpdateRequest;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.AvatarImageUploadCompleteRequest;
import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.FastApiTryOnRequest;
import com.tryiton.core.avatar.dto.response.AvatarBaseImageUpdateResponse;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.AvatarImageUploadCompleteResponse;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.dto.response.CeleryTaskResultDto;
import com.tryiton.core.avatar.dto.response.FastApiTryOnResponse;
import com.tryiton.core.avatar.dto.response.InitialAvatarResponse;
import com.tryiton.core.avatar.dto.response.ResetAvatarResponse;
import com.tryiton.core.avatar.dto.response.TaskResponse;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.entity.AvatarItem;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.common.enums.RecommendAction;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.common.service.S3Service;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.recommend.service.RecommendBehaviorLogService;
import java.time.Duration;
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
    private final WebClient fastApiWebClient;
    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    private final RecommendBehaviorLogService recommendBehaviorLogService;
    private final com.tryiton.core.member.repository.ProfileRepository profileRepository;

    @Value("${server.url}")
    private String springServerUrl;

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
        String cacheKey = String.format("cache/tryon/%d/%s-%d.png", userId, garmentType, product.getId());
        log.info("캐시 키 생성 - userId: {}, productId: {}, garmentType: {}, 키: {}",
            userId, product.getId(), garmentType, cacheKey);
        return cacheKey;
    }

    /**
     * 캐시 키 생성 (조합용 - 상의/하의)
     */
    private String generateCombinationCacheKey(Long userId, Long topId, Long bottomId) {
        StringBuilder keyBuilder = new StringBuilder();
        
        // 베이스 아바타인 경우 base 폴더에 저장
        Profile profile = profileRepository.findById(userId).orElse(null);
        boolean isBaseAvatar = profile != null && 
                               profile.getUserBaseImageUrl() != null && 
                               profile.getUserBaseImageUrl().contains("base/default_avatar");
        
        if (isBaseAvatar) {
            keyBuilder.append("cache/tryon/base/");
        } else {
            keyBuilder.append("cache/tryon/").append(userId).append("/");
        }

        if (topId != null && bottomId != null) {
            keyBuilder.append("top-").append(topId).append("_bottom-").append(bottomId);
        } else if (topId != null) {
            keyBuilder.append("top-").append(topId);
        } else if (bottomId != null) {
            keyBuilder.append("bottom-").append(bottomId);
        } else {
            keyBuilder.append("base");
        }

        keyBuilder.append(".png");

        String cacheKey = keyBuilder.toString();
        log.info("조합 캐시 키 생성 - userId: {}, topId: {}, bottomId: {}, 베이스아바타: {}, 키: {}",
            userId, topId, bottomId, isBaseAvatar, cacheKey);
        return cacheKey;
    }

    /**
     * S3에 캐시 이미지가 존재하는지 확인
     */
    private boolean existsInS3(String key) {
        try {
            log.info("S3 캐시 확인 시작 - 키: {}", key);

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            s3Client.headObject(headObjectRequest);
            log.info("S3 캐시 확인 성공 - 키: {}", key);
            return true;
        } catch (NoSuchKeyException e) {
            log.info("S3 캐시 미스 - 키: {}", key);
            return false;
        } catch (Exception e) {
            log.warn("S3 객체 존재 확인 중 오류 발생: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * S3 Public URL 생성
     */
    private String buildS3PublicUrl(String key) {
        String url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        log.info("S3 Public URL 생성 - 키: {}, URL: {}", key, url);
        return url;
    }

    // 가장 최근 착장한 아바타 이미지 + 착용 상품명 리스트
    @Override
    @Transactional(readOnly = true)
    public AvatarTryOnResponse getLatestAvatarWithProducts(Long userId) {
        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId);

        // 아바타가 없을 때는 빈 응답을 반환하여 프론트엔드 오류 방지
        if (avatar == null) {
            return new AvatarTryOnResponse(null, null, java.util.Collections.emptyList());
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

    @Override
    @Transactional
    public AvatarCreateResponse createAvatar(Member member, AvatarCreateRequest avatarCreateRequest) {
        log.info("아바타 생성 요청 시작 - userId: {}", member.getId());
        
        // 기본 아바타 URL인지 확인
        boolean isDefaultAvatar = avatarCreateRequest.getTryOnImgUrl().contains("base/default_avatar");
        
        // 기본 아바타인 경우 FastAPI 호출 없이 기본 에셋 사용
        if (isDefaultAvatar) {
            log.info("기본 아바타 사용 - userId: {}", member.getId());
            
            // 기본 에셋 파일을 사용자 폴더로 복사
            copyDefaultAvatarAssets(member.getId());
            
            // 아바타 DB 저장
            Avatar newAvatar = Avatar.builder()
                .member(member)
                .avatarImg(avatarCreateRequest.getTryOnImgUrl())
                .build();
                
            Avatar savedAvatar = avatarRepository.save(newAvatar);
            
            // 프로필에 베이스 이미지 URL 업데이트
            Profile profile = member.getProfile();
            if (profile != null) {
                String oldUserBaseImageUrl = profile.getUserBaseImageUrl();
                String oldAvatarBaseImageUrl = profile.getAvatarBaseImageUrl();
                
                profile.setUserBaseImageUrl(avatarCreateRequest.getTryOnImgUrl());
                profile.setAvatarBaseImageUrl(avatarCreateRequest.getTryOnImgUrl());
                
                // 프로필 변경사항 명시적으로 저장
                profileRepository.save(profile);
                
                log.info("프로필 이미지 업데이트 및 저장 완료: userBaseImageUrl: {} -> {}, avatarBaseImageUrl: {} -> {}", 
                        oldUserBaseImageUrl, avatarCreateRequest.getTryOnImgUrl(),
                        oldAvatarBaseImageUrl, avatarCreateRequest.getTryOnImgUrl());
            }
            
            log.info("기본 아바타 생성 및 DB 저장 완료 - userId: {}, 마스크/포즈 URL 설정됨", member.getId());
            return AvatarCreateResponse.fromEntity(savedAvatar);
        }

        // 1. Python API에 작업 요청 보내고 Celery Task ID 받기
        String celeryTaskId = requestTaskToFastApi("/generate", avatarCreateRequest);

        // 2. 작업 완료될 때까지 폴링하며 대기
        CeleryTaskResultDto taskResult = pollForResult(celeryTaskId);

        // 3. 받은 결과로 후속 처리
        try {
            InitialAvatarResponse fastApiResponse = objectMapper.treeToValue(taskResult.getResult(), InitialAvatarResponse.class);

            if (fastApiResponse == null || fastApiResponse.getPoseImgUrl() == null) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "FastAPI로부터 유효한 응답을 받지 못했습니다.");
            }

            // 아바타 DB 저장
            Avatar newAvatar = Avatar.builder()
                .member(member)
                .avatarImg(avatarCreateRequest.getTryOnImgUrl())
                .build();
            Avatar savedAvatar = avatarRepository.save(newAvatar);

            // 프로필에 베이스 이미지 URL 업데이트
            Profile profile = member.getProfile();
            if (profile != null) {
                String oldUserBaseImageUrl = profile.getUserBaseImageUrl();
                String oldAvatarBaseImageUrl = profile.getAvatarBaseImageUrl();
                
                profile.setUserBaseImageUrl(avatarCreateRequest.getTryOnImgUrl());
                profile.setAvatarBaseImageUrl(avatarCreateRequest.getTryOnImgUrl());
                
                // 프로필 변경사항 명시적으로 저장
                profileRepository.save(profile);
                
                log.info("프로필 이미지 업데이트 및 저장 완료: userBaseImageUrl: {} -> {}, avatarBaseImageUrl: {} -> {}", 
                        oldUserBaseImageUrl, avatarCreateRequest.getTryOnImgUrl(),
                        oldAvatarBaseImageUrl, avatarCreateRequest.getTryOnImgUrl());
            }

            log.info("아바타 생성 및 DB 저장 완료 - userId: {}", member.getId());
            return AvatarCreateResponse.fromEntity(savedAvatar);

        } catch (Exception e) {
            log.error("아바타 생성 결과 처리 중 오류 발생", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "아바타 생성에 실패했습니다: " + e.getMessage());
        }
    }


    /**
     * 가상 피팅을 비동기적으로 수행하고 결과를 반환합니다.
     */
    @Override
    @Transactional
    public AvatarTryOnResponse tryOn(Member member, AvatarTryOnRequest avatarTryOnRequest) {
        Long userId = member.getId();
        Long productId = Long.parseLong(avatarTryOnRequest.getProductId());

        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId);
        if (avatar == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "가상 피팅을 진행할 아바타가 존재하지 않습니다.");
        }

        Product newGarment = productRepository.findByIdWithCategory(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + avatarTryOnRequest.getProductId()));

        // 현재 착용 중인 상의와 하의 확인 (더 명확하게 로깅)
        Long currentTopId = null;
        Long currentBottomId = null;
        Product currentTop = null;
        Product currentBottom = null;
        
        for (AvatarItem item : avatar.getItems()) {
            Product product = item.getProduct();
            if (product.isUpperGarment()) {
                currentTopId = product.getId();
                currentTop = product;
                log.info("현재 착용 중인 상의: ID={}, 이름={}", currentTopId, product.getProductName());
            } else if (product.isLowerGarment()) {
                currentBottomId = product.getId();
                currentBottom = product;
                log.info("현재 착용 중인 하의: ID={}, 이름={}", currentBottomId, product.getProductName());
            }
        }

        boolean isNewGarmentTop = newGarment.isUpperGarment();
        log.info("새로 입히는 의류: ID={}, 이름={}, 타입={}", 
             newGarment.getId(), newGarment.getProductName(), 
             isNewGarmentTop ? "상의" : "하의");
        
        // 캐시 키 생성
        String cacheKey;
        if (isNewGarmentTop) {
            // 상의인 경우: 현재 하의 ID 유지
            cacheKey = generateCombinationCacheKey(userId, productId, currentBottomId);
        } else {
            // 하의인 경우: 현재 상의 ID 유지
            cacheKey = generateCombinationCacheKey(userId, currentTopId, productId);
        }
        
        // 캐시 확인 - S3에 해당 키로 파일이 존재하는지 확인
        boolean cacheExists = existsInS3(cacheKey);
        
        // 캐시 로깅 추가
        log.info("캐시 확인 - 키: {}, 존재여부: {}", cacheKey, cacheExists);

        String finalImageUrl;

        if (cacheExists) {
            // 캐시 HIT
            finalImageUrl = buildS3PublicUrl(cacheKey) + "?t=" + System.currentTimeMillis();
            log.info("캐시 히트! 기존 이미지 사용: {}", finalImageUrl);
        } else {
            // 캐시 MISS: FastAPI에 비동기 작업 요청 및 폴링
            log.info("캐시 미스! FastAPI 호출 시작");

            // 베이스 이미지 선택 로직
            String baseImageUrl;
            
            // 항상 프로필에서 최신 베이스 이미지 URL을 가져옴 (DB에서 최신 상태 조회)
            Profile freshProfile = profileRepository.findById(member.getId()).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "사용자 프로필을 찾을 수 없습니다.")
            );
            
            // 최신 userBaseImageUrl 사용
            String latestBaseImageUrl = freshProfile.getUserBaseImageUrl();
            log.info("최신 베이스 이미지 URL 조회: {}", latestBaseImageUrl);
            
            // 중요: 상의/하의 교체 시 이미지 선택 로직 수정
            if (isNewGarmentTop) {
                // 상의를 입히는 경우
                if (currentBottomId != null) {
                    // 이미 하의를 입고 있는 경우, 현재 아바타 이미지 사용
                    baseImageUrl = avatar.getAvatarImg();
                    log.info("하의가 입혀진 현재 아바타 이미지를 베이스로 사용: {}", baseImageUrl);
                } else {
                    // 하의를 입고 있지 않은 경우, 원본 이미지 사용
                    baseImageUrl = latestBaseImageUrl;
                    log.info("원본 베이스 이미지 사용: {}", baseImageUrl);
                }
            } else {
                // 하의를 입히는 경우
                if (currentTopId != null) {
                    // 이미 상의를 입고 있는 경우, 현재 아바타 이미지 사용
                    baseImageUrl = avatar.getAvatarImg();
                    log.info("상의가 입혀진 현재 아바타 이미지를 베이스로 사용: {}", baseImageUrl);
                } else {
                    // 상의를 입고 있지 않은 경우, 원본 이미지 사용
                    baseImageUrl = latestBaseImageUrl;
                    log.info("원본 베이스 이미지 사용: {}", baseImageUrl);
                }
            }

            FastApiTryOnRequest fastApiRequest = new FastApiTryOnRequest(
                baseImageUrl, // 베이스 이미지 URL
                newGarment.getImg1(), 
                buildS3Url(avatar.getMaskUrl(newGarment)),
                buildS3Url(avatar.getPoseUrl()), 
                member.getId(), 
                newGarment.getId(),
                determineGarmentType(newGarment),
                cacheKey,  // 캐시 키 추가
                null, 
                null // taskId, callbackUrl은 이제 사용 안함
            );
            
            // 캐시 키 정보도 함께 로깅
            log.info("FastAPI 요청 생성 - baseImageUrl: {}, garmentImgUrl: {}, cacheKey: {}", 
                    baseImageUrl, newGarment.getImg1(), cacheKey);

            // 1. Python API에 작업 요청 보내고 Celery Task ID 받기
            String celeryTaskId = requestTaskToFastApi("/tryon", fastApiRequest);

            // 2. 작업 완료될 때까지 폴링하며 대기
            CeleryTaskResultDto taskResult = pollForResult(celeryTaskId);

            // 3. 받은 결과에서 최종 이미지 URL 추출
            try {
                FastApiTryOnResponse fastApiResponse = objectMapper.treeToValue(taskResult.getResult(), FastApiTryOnResponse.class);
                if (fastApiResponse == null || fastApiResponse.getTryOnImgUrl() == null) {
                    log.error("FastAPI 응답에서 tryOnImgUrl을 찾을 수 없습니다. 응답: {}", taskResult.getResult().toString());
                    throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "가상 피팅 결과 처리 중 오류가 발생했습니다.");
                }
                finalImageUrl = fastApiResponse.getTryOnImgUrl() + "?t=" + System.currentTimeMillis();
                log.info("폴링 성공. 최종 이미지 URL: {}", finalImageUrl);
            } catch (Exception e) {
                log.error("Try-on 결과 처리 중 오류 발생", e);
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "가상 피팅 결과 처리 중 오류가 발생했습니다.");
            }
        }

        // 공통 로직: 아바타 상태 업데이트 및 응답 생성
        // 중요: 기존 아이템 유지 로직 수정
        if (isNewGarmentTop) {
            // 상의를 교체하는 경우: 기존 상의만 제거하고 하의는 유지
            avatar.getItems().removeIf(item -> item.getProduct().isUpperGarment());
        } else {
            // 하의를 교체하는 경우: 기존 하의만 제거하고 상의는 유지
            avatar.getItems().removeIf(item -> !item.getProduct().isUpperGarment());
        }
        
        // 새 의류 추가
        avatar.getItems().add(new AvatarItem(avatar, newGarment));
        avatar.update(finalImageUrl);
        
        // 현재 착용 중인 아이템 로깅 (디버깅용)
        log.info("아바타 업데이트 후 착용 아이템 수: {}", avatar.getItems().size());
        for (AvatarItem item : avatar.getItems()) {
            log.info("착용 아이템: ID={}, 이름={}, 타입={}", 
                    item.getProduct().getId(), 
                    item.getProduct().getProductName(),
                    item.getProduct().isUpperGarment() ? "상의" : "하의");
        }
        
        recommendBehaviorLogService.logUserAction(userId, newGarment.getId(), RecommendAction.TRYON);

        List<AvatarTryOnResponse.ProductInfo> productInfos = avatar.getItems().stream()
            .map(item -> new AvatarTryOnResponse.ProductInfo(
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getProduct().getCategory().getCategoryName()
            ))
            .collect(Collectors.toList());

        log.info("아바타 응답 생성 완료 - 최종 이미지 URL: {}, 착용 아이템 수: {}", finalImageUrl, productInfos.size());
        return new AvatarTryOnResponse(avatar.getId(), finalImageUrl, productInfos);
    }

    /**
     * Python FastAPI 서버에 작업을 요청하고 Celery Task ID를 받아옵니다.
     */
    private String requestTaskToFastApi(String uri, Object requestBody) {
        try {
            TaskResponse response = fastApiWebClient.post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(TaskResponse.class)
                .block(Duration.ofSeconds(10)); // API 서버의 응답은 즉시 오므로 짧은 타임아웃

            if (response == null || response.getTask_id() == null) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "FastAPI로부터 작업 ID를 받지 못했습니다.");
            }
            log.info("FastAPI 작업 요청 성공. Celery Task ID: {}", response.getTask_id());
            return response.getTask_id();
        } catch (Exception e) {
            log.error("FastAPI {} 요청 실패", uri, e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "작업 요청에 실패했습니다.");
        }
    }

    /**
     * 작업이 완료될 때까지 Python FastAPI 서버의 결과 확인 API를 폴링합니다.
     */
    private CeleryTaskResultDto pollForResult(String celeryTaskId) {
        long startTime = System.currentTimeMillis();
        long timeout = 60 * 1000; // 최대 60초 대기

        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                CeleryTaskResultDto result = fastApiWebClient.get()
                    .uri("/result/{celery_task_id}", celeryTaskId)
                    .retrieve()
                    .bodyToMono(CeleryTaskResultDto.class)
                    .block(Duration.ofSeconds(5));

                if (result != null) {
                    if ("SUCCESS".equals(result.getStatus())) {
                        log.info("작업 성공 확인. Celery Task ID: {}", celeryTaskId);
                        return result;
                    } else if ("FAILURE".equals(result.getStatus())) {
                        log.error("작업 실패 확인. Celery Task ID: {}, 결과: {}", celeryTaskId, result.getResult());
                        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "작업 처리 중 오류가 발생했습니다.");
                    }
                    // "PENDING" 상태이면 계속 폴링
                }

                // 2초 대기 후 다시 시도
                Thread.sleep(2000);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "작업 대기 중 인터럽트 발생");
            } catch (Exception e) {
                log.error("결과 폴링 중 오류 발생. Celery Task ID: {}", celeryTaskId, e);
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "결과 확인 중 오류가 발생했습니다.");
            }
        }
        throw new BusinessException(HttpStatus.REQUEST_TIMEOUT, "작업 처리 시간을 초과했습니다.");
    }

    @Transactional
    @Override
    public ResetAvatarResponse resetAvatar(Member member) {
        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(member.getId());
        String baseImageUrl = member.getProfile().getUserBaseImageUrl();
        avatar.resetAvatar(baseImageUrl);
        
        // 아바타 이미지 URL을 포함한 응답 반환
        return ResetAvatarResponse.of(baseImageUrl);
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
            String oldAvatarBaseImageUrl = profile.getAvatarBaseImageUrl();
            profile.setUserBaseImageUrl(request.getNewBaseImageUrl());
            profile.setAvatarBaseImageUrl(request.getNewBaseImageUrl());
            
            // 프로필 변경사항 명시적으로 저장
            profileRepository.save(profile);
            
            log.info("프로필 이미지 업데이트 및 저장 완료: userBaseImageUrl: {} -> {}, avatarBaseImageUrl: {} -> {}", 
                    oldBaseImageUrl, request.getNewBaseImageUrl(),
                    oldAvatarBaseImageUrl, request.getNewBaseImageUrl());

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
        return String.format("combination/%d/%d-%d.png", userId, topId, bottomId);
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
            
            // base 폴더는 삭제하지 않음 (공유 리소스이므로)
            log.info("사용자 캐시 무효화 완료 - userId: {} (base 폴더는 유지됨)", userId);

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
            log.info("아바타 이미지 업로드 완료 처리 시작 - userId: {}, newImageUrl: {}, 설정하지 않음: {}",
                member.getId(), request.getNewAvatarImageUrl(), request.isSkipAvatarSetup());

            // "설정하지 않음" 옵션이 아닌 경우에만 URL 검증
            if (!request.isSkipAvatarSetup() && (request.getNewAvatarImageUrl() == null || request.getNewAvatarImageUrl().trim().isEmpty())) {
                log.error("새 아바타 이미지 URL이 비어있습니다 - userId: {}", member.getId());
                return AvatarImageUploadCompleteResponse.failure("새 아바타 이미지 URL이 제공되지 않았습니다.");
            }

            // 1. 프로필의 베이스 이미지 URL 업데이트
            Profile profile = member.getProfile();
            if (profile == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "사용자 프로필을 찾을 수 없습니다.");
            }

            String oldBaseImageUrl = profile.getUserBaseImageUrl();
            String oldAvatarBaseImageUrl = profile.getAvatarBaseImageUrl();
            String newAvatarImageUrl;
            
            // "설정하지 않음" 선택 시 base 아바타 사용
            if (request.isSkipAvatarSetup()) {
                // 기본 아바타 URL 설정 (S3에 저장된 기본 아바타 이미지 URL)
                newAvatarImageUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/base/default_avatar.png";
                log.info("기본 아바타 사용 - URL: {}", newAvatarImageUrl);
                
                // 기본 마스크 파일들을 사용자 폴더로 복사
                copyDefaultAvatarAssets(member.getId());
            } else {
                // 사용자가 업로드한 이미지 사용
                newAvatarImageUrl = request.getNewAvatarImageUrl();
            }
            
            // userBaseImageUrl과 avatarBaseImageUrl 모두 업데이트
            profile.setUserBaseImageUrl(newAvatarImageUrl);
            profile.setAvatarBaseImageUrl(newAvatarImageUrl);
            
            // 프로필 변경사항 명시적으로 저장
            profileRepository.save(profile);
            
            log.info("프로필 이미지 업데이트 및 저장 완료: userBaseImageUrl: {} -> {}, avatarBaseImageUrl: {} -> {}", 
                    oldBaseImageUrl, newAvatarImageUrl,
                    oldAvatarBaseImageUrl, newAvatarImageUrl);

            // 2. 아바타 생성
            AvatarCreateRequest avatarCreateRequest = new AvatarCreateRequest(
                member.getId().toString(),
                newAvatarImageUrl
            );

            AvatarCreateResponse avatarCreateResponse = createAvatar(member, avatarCreateRequest);

            // 3. 기존 캐시 무효화
            invalidateUserCache(member.getId());
            
            // 4. 상태 확인 - 최종 확인을 위해 DB에서 다시 조회
            Profile updatedProfile = profileRepository.findById(member.getId()).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "사용자 프로필을 찾을 수 없습니다.")
            );
            
            log.info("아바타 업로드 완료 후 최종 상태 확인 - userBaseImageUrl: {}, avatarBaseImageUrl: {}", 
                    updatedProfile.getUserBaseImageUrl(), updatedProfile.getAvatarBaseImageUrl());

            log.info("아바타 이미지 업로드 완료 처리 완료 - userId: {}", member.getId());

            return AvatarImageUploadCompleteResponse.success(
                newAvatarImageUrl,
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
    
    /**
     * 기본 아바타 에셋(마스크, 포즈 이미지)을 사용자 폴더로 복사합니다.
     * 회원가입 시 아바타 설정을 건너뛰는 경우 호출됩니다.
     */
    private void copyDefaultAvatarAssets(Long userId) {
        try {
            log.info("기본 아바타 에셋 복사 시작 - userId: {}", userId);
            
            // 기본 에셋 파일 목록
            String[] assetFiles = {
                "pose.png",
                "upper_mask.png",
                "lower_mask.png"
            };
            
            // 각 파일을 복사
            for (String assetFile : assetFiles) {
                // 소스 키 (기본 아바타 에셋)
                String sourceKey = "users/base/" + assetFile;
                
                // 대상 키 (사용자별 폴더)
                String destinationKey = "users/" + userId + "/" + assetFile;
                
                // S3 복사 요청
                software.amazon.awssdk.services.s3.model.CopyObjectRequest copyRequest = 
                    software.amazon.awssdk.services.s3.model.CopyObjectRequest.builder()
                        .sourceBucket(bucketName)
                        .sourceKey(sourceKey)
                        .destinationBucket(bucketName)
                        .destinationKey(destinationKey)
                        .build();
                
                s3Client.copyObject(copyRequest);
                log.info("에셋 파일 복사 완료: {} -> {}", sourceKey, destinationKey);
            }
            
            log.info("기본 아바타 에셋 복사 완료 - userId: {}", userId);
        } catch (Exception e) {
            log.error("기본 아바타 에셋 복사 실패 - userId: {}, error: {}", userId, e.getMessage());
            // 복사 실패해도 진행은 계속함 (중요하지 않은 오류로 처리)
        }
    }
}
