package com.tryiton.core.auth.oauth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.tryiton.core.auth.email.util.Validator;
import com.tryiton.core.auth.oauth.dto.GoogleSigninResponseDto;
import com.tryiton.core.auth.oauth.entity.OauthCredentials;
import com.tryiton.core.common.enums.Gender;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import com.tryiton.core.auth.jwt.JwtUtil;
import com.tryiton.core.auth.oauth.dto.GoogleInfoDto;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.Style;
import com.tryiton.core.common.enums.UserRole;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.auth.oauth.dto.GoogleSigninRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupResponseDto;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.member.repository.MemberRepository;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

@Service
@Slf4j
public class AuthService {

    private static final long ONE_HOUR = 60 * 60 * 1000L; // JWT 토큰 만료 1시간으로 설정

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final String clientId;
    private final S3Client s3Client; // S3Template 대신 S3Client 주입
    private final String bucketName;

    public AuthService(MemberRepository memberRepository, JwtUtil jwtUtil,
        @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
        S3Client s3Client, // 생성자 파라미터 변경
        @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
        this.clientId = clientId;
        this.s3Client = s3Client; // 주입받은 S3Client 할당
        this.bucketName = bucketName;
    }

    // client id 검증
    @PostConstruct
    public void validateClientId() {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalStateException("Google Client ID가 설정되지 않았습니다.");
        }
    }

    // OAuth 토큰을 검증하여 사용자 정보 반환
    public GoogleInfoDto authenticate(String token) {
        return extractUserInfoFrom(token);
    }

    // 토큰에서 Google 사용자 정보 추출
    private GoogleInfoDto extractUserInfoFrom(String token) {
        try {
            validateClientId();

            GoogleIdTokenVerifier verifier = createGoogleIdTokenVerifier();
            // 토큰 검증
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "유효하지 않은 ID 토큰입니다.");
            }
            Payload payload = idToken.getPayload();

            // Payload로부터 사용자 정보 추출
            return convertPayloadTo(payload);

        } catch (GeneralSecurityException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Google 토큰 검증 실패: " + e.getMessage());
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Google 토큰 검증 실패: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Google 토큰 검증 실패: " + e.getMessage());
        }
    }

    // Payload를 GoogleInfoDto로 변환
    private GoogleInfoDto convertPayloadTo(Payload payload) {
        String email = payload.getEmail();
        String pictureUrl = payload.containsKey("picture") ? (String) payload.get("picture") : null;
        String sub = payload.getSubject();

        return new GoogleInfoDto(email, pictureUrl, sub);
    }

    // Google ID 토큰 검증기 생성
    private GoogleIdTokenVerifier createGoogleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(), new JacksonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build();
    }

    // 로그인 요청을 처리하고 JWT 토큰 반환
    public GoogleSigninResponseDto loginWithGoogle(GoogleSigninRequestDto dto) {
        GoogleInfoDto googleInfo = authenticate(dto.getIdToken());
        String email = googleInfo.getEmail();

        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "가입되지 않은 회원입니다. 회원가입이 필요합니다."));

        String jwt = jwtUtil.createJwt(email, member.getRole().name(), ONE_HOUR); // 1시간

        return (GoogleSigninResponseDto) GoogleSigninResponseDto.builder()
            .username(member.getUsername())
            .email(member.getEmail())
            .accessToken(jwt)
            .build();
    }


    // 회원가입 요청을 처리하고 JWT 토큰 반환
    @Transactional
    public GoogleSignupResponseDto signupWithGoogle(GoogleSignupRequestDto dto) {
        try {
            GoogleInfoDto googleInfo = authenticate(dto.getIdToken());
            String email = googleInfo.getEmail();

            // 이메일 중복 체크
            memberRepository.findByEmail(email).ifPresent(m -> {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 가입된 회원입니다.");
            });

            // 휴대폰 번호 유효성 검사
            Validator.validatePhone(dto.getPhoneNum());

            // 회원가입 처리 (엔티티 생성)
            Member member = Member.builder()
                .email(email)
                .username(dto.getUsername())
                .birthDate(dto.getBirthDate())
                .gender(Gender.valueOf(dto.getGender()))
                .phoneNum(dto.getPhoneNum())
                .provider(AuthProvider.GOOGLE)
                .role(UserRole.USER)
                .build();

            // ★ 1. Member를 먼저 저장하여 ID를 할당받습니다.
            Member savedMember = memberRepository.save(member);
            Long userId = savedMember.getId();

            // ★ 2. S3 이미지 처리 로직을 먼저 호출하여 새로운 URL을 확보합니다.
            String newProfileImageUrl = handleProfileImage(googleInfo.getPictureUrl(), userId);

            // ★ 3. OauthCredentials와 Profile 엔티티를 생성합니다.
            OauthCredentials oauthCredentials = OauthCredentials.builder()
                .providerUserId(googleInfo.getSub())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .scope("openid profile email")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .member(savedMember)
                .build();

            Profile profile = Profile.builder()
                .preferredStyle(Style.valueOf(dto.getPreferredStyle()))
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .shoeSize(dto.getShoeSize())
                .profileImageUrl(newProfileImageUrl) // ★ 4. 새로 받은 URL을 사용합니다.
                .userBaseImageUrl(dto.getUserBaseImageUrl())
                .avatarBaseImageUrl(dto.getAvatarBaseImageUrl())
                .member(savedMember)
                .build();

            // ★ 5. Member에 연관관계를 설정합니다.
            savedMember.setOauthCredentials(oauthCredentials);
            savedMember.setProfile(profile);

            // ★ 6. Profile과 OauthCredentials 정보가 포함된 Member를 다시 저장하여 업데이트를 완료합니다.
            memberRepository.save(savedMember);

            String jwt = jwtUtil.createJwt(email, savedMember.getRole().name(), ONE_HOUR);
            return GoogleSignupResponseDto.from(savedMember, jwt);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류가 발생했습니다.", e);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * S3에 있는 프로필 이미지를 새 경로로 옮기고, 원본을 삭제한 뒤, 새 URL을 반환합니다.
     * @param originalImageUrl 원본 이미지 URL
     * @param userId 사용자 ID
     * @return 새로 생성된 이미지의 영구 URL
     */
    private String handleProfileImage(String originalImageUrl, Long userId) {
        if (originalImageUrl == null || originalImageUrl.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(originalImageUrl);
            String originalKey = uri.getPath().substring(1);

            String fileName = originalKey.substring(originalKey.lastIndexOf('/') + 1);
            String newKey = "users/" + userId + "/" + fileName;

            // 1. CopyObjectRequest 생성 및 실행
            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(originalKey)
                .destinationBucket(bucketName)
                .destinationKey(newKey)
                .build();
            s3Client.copyObject(copyReq);

            // 2. DeleteObjectRequest 생성 및 실행
            DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(originalKey)
                .build();
            s3Client.deleteObject(deleteReq);

            // 3. 새로 생성된 객체의 URL 가져오기
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(newKey)
                .build();
            URL newUrl = s3Client.utilities().getUrl(getUrlRequest);

            return newUrl.toString().split("\\?")[0]; // 쿼리 파라미터 제외한 순수 URL 반환

        } catch (Exception e) {
            log.error("S3 프로필 이미지 처리 중 오류 발생. userId: {}", userId, e);
            return originalImageUrl;
        }
    }
}