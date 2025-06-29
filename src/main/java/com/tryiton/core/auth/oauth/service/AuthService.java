package com.tryiton.core.auth.oauth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.tryiton.core.auth.oauth.entity.OauthCredentials;
import com.tryiton.core.common.enums.Gender;
import jakarta.annotation.PostConstruct;
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
import com.tryiton.core.member.dto.SigninResponseDto;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.member.repository.MemberRepository;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    private static final long ONE_HOUR = 60 * 60 * 1000L; // JWT 토큰 만료 1시간으로 설정

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final String clientId;

    public AuthService(MemberRepository memberRepository, JwtUtil jwtUtil, 
                      @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId) {
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
        this.clientId = clientId;
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
                throw new BusinessException("유효하지 않은 ID 토큰입니다.");
            }
            Payload payload = idToken.getPayload();

            // Payload로부터 사용자 정보 추출
            return convertPayloadTo(payload);

        } catch (GeneralSecurityException e) {
            throw new BusinessException("Google 토큰 검증 실패: " + e.getMessage());
        } catch (IOException e) {
            throw new BusinessException("Google 토큰 검증 실패: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("Google 토큰 검증 실패: " + e.getMessage());
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
    public SigninResponseDto loginWithGoogle(GoogleSigninRequestDto dto) {
        GoogleInfoDto googleInfo = authenticate(dto.getIdToken());
        String email = googleInfo.getEmail();

        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("가입되지 않은 회원입니다. 회원가입이 필요합니다."));

        String jwt = jwtUtil.createJwt(email, member.getRole().name(), ONE_HOUR); // 1시간

        return new SigninResponseDto(member.getUsername(), member.getEmail(), jwt);
    }

    // 회원가입 요청을 처리하고 JWT 토큰 반환
    @Transactional
    public GoogleSignupResponseDto signupWithGoogle(GoogleSignupRequestDto dto) {
        try {
            GoogleInfoDto googleInfo = authenticate(dto.getIdToken());
            String email = googleInfo.getEmail();
            
            // 이메일 중복 체크
            memberRepository.findByEmail(email).ifPresent(m -> {
                throw new BusinessException("이미 가입된 회원입니다.");
            });

            Member member = Member.builder()
                .email(email)
                .username(dto.getUsername())
                .birthDate(dto.getBirthDate())
                .gender(Gender.valueOf(dto.getGender()))
                .phoneNum(dto.getPhoneNum())
                .provider(AuthProvider.GOOGLE)
                .role(UserRole.USER)
                .build();

            OauthCredentials oauthCredentials = OauthCredentials.builder()
                .providerUserId(googleInfo.getSub()) // Google에서 제공하는 고유 식별자
                .expiresAt(LocalDateTime.now().plusHours(1)) // 1시간
                .scope("openid profile email")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .member(member)
                .build();

            Profile profile = Profile.builder()
                .preferredStyle(Style.valueOf(dto.getPreferredStyle()))
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .shoeSize(dto.getShoeSize())
                .profileImageUrl(googleInfo.getPictureUrl())
                .member(member)
                .build();

            member.setOauthCredentials(oauthCredentials);
            member.setProfile(profile);

            Member saved = memberRepository.save(member);

            String jwt = jwtUtil.createJwt(email, member.getRole().name(), ONE_HOUR); // 1시간
            return GoogleSignupResponseDto.from(saved, jwt);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}