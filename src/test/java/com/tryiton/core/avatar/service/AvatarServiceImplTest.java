package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.FastApiTryOnRequest;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.dto.response.FastApiTryOnResponse;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarServiceImplTest {

    @InjectMocks
    private AvatarServiceImpl avatarService;

    @Mock
    private AvatarRepository avatarRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WebClient fastApiWebClient;
    @Mock
    private Avatar mockAvatar;

    // WebClient Mock 객체들
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    @DisplayName("상의 의류 TryOn 성공 테스트")
    void tryOn_withUpperGarment_shouldSucceed() {
        // given
        Long userId = 1L;
        Long productId = 10L;
        String newAvatarImgUrl = "http://s3.new-avatar-image.com/image.jpg";

        Member mockMember = Member.builder().id(userId).build();
        AvatarTryOnRequest request = new AvatarTryOnRequest(Long.toString(productId));
        Product mockGarment = mock(Product.class);

        // --- 1. static 메서드 호출을 제어 ---
        try (MockedStatic<Product> mockedProduct = mockStatic(Product.class)) {
            mockedProduct.when(() -> mockGarment.isUpperGarment()).thenReturn(true);

            // --- 2. 명시적으로 분리된 스터빙 (핵심 수정 부분) ---
            Category mockCategory = mock(Category.class); // 카테고리 Mock 생성
            when(mockGarment.getCategory()).thenReturn(mockCategory); // getCategory()가 mockCategory를 반환하도록 설정
            when(mockCategory.getCategoryName()).thenReturn("티셔츠"); // getCategoryName()이 "티셔츠"를 반환하도록 설정
            // --- 여기까지 수정 ---

            // Repository Mocking
            when(avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId)).thenReturn(mockAvatar);
            when(productRepository.findById(productId)).thenReturn(Optional.of(mockGarment));

            // mockAvatar의 행위 정의
            when(mockAvatar.getAvatarImg()).thenReturn("http://s3.base-avatar-image.com/base.jpg");
            when(mockAvatar.getPoseImg()).thenReturn("http://s3.pose-image.com/pose.jpg");
            when(mockAvatar.getUpperMaskImg()).thenReturn("http://s3.upper-mask.com/mask.jpg");
            when(mockAvatar.getId()).thenReturn(1L);

            // mockGarment의 다른 행위 정의
            when(mockGarment.getImg2()).thenReturn("http://s3.garment-image.com/garment.jpg");
            when(mockGarment.getProductName()).thenReturn("테스트용 티셔츠");

            // WebClient Mocking
            FastApiTryOnResponse mockedResponse = mock(FastApiTryOnResponse.class);
            when(mockedResponse.getTryOnImgUrl()).thenReturn(newAvatarImgUrl);
            when(fastApiWebClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("/tryon")).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any(FastApiTryOnRequest.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(FastApiTryOnResponse.class)).thenReturn(Mono.just(mockedResponse));

            // when
            AvatarTryOnResponse response = avatarService.tryOn(mockMember, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAvatarImgUrl()).isEqualTo(newAvatarImgUrl);
            assertThat(response.getProductName()).isEqualTo("테스트용 티셔츠");
            assertThat(response.getCategoryName()).isEqualTo("티셔츠");

            verify(mockAvatar, times(1)).update(newAvatarImgUrl);
        }
    }
}