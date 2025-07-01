package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.FastApiTryOnRequest;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.dto.response.FastApiTryOnResponse;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.entity.AvatarItem;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
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
    @Mock
    private Product newTop, existingBottom, newBottom;
    @Mock
    private Category topCategory, bottomCategory;

    // WebClient Mocking을 위한 객체들
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    @DisplayName("하의를 입은 아바타가 새 상의를 입으면, 착용 목록과 DTO가 올바르게 업데이트 되어야 한다")
    void tryOn_whenWearingBottom_shouldWearNewTopAndReturnCorrectDto() {
        // given
        Long userId = 1L;
        Long newTopId = 10L;
        String newAvatarImgUrl = "http://s3.new-avatar.com/image.jpg";
        Member member = Member.builder().id(userId).build();
        AvatarTryOnRequest request = new AvatarTryOnRequest(Long.toString(newTopId));

        // Repository 동작 정의
        when(avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId)).thenReturn(mockAvatar);
        when(productRepository.findById(newTopId)).thenReturn(Optional.of(newTop));

        // Mock 객체들 정보 정의
        when(newTop.isUpperGarment()).thenReturn(true);
        when(newTop.getImg2()).thenReturn("newTop.jpg");
        when(newTop.getProductName()).thenReturn("새로운 상의");
        when(newTop.getCategory()).thenReturn(topCategory);
        when(topCategory.getCategoryName()).thenReturn("상의");

        when(existingBottom.getProductName()).thenReturn("기존 하의");
        when(existingBottom.getCategory()).thenReturn(bottomCategory);
        when(bottomCategory.getCategoryName()).thenReturn("하의");

        // wearGarment가 호출된 '이후'의 최종 상태를 정의
        when(mockAvatar.getItems()).thenReturn(List.of(new AvatarItem(mockAvatar, existingBottom), new AvatarItem(mockAvatar, newTop)));
        when(mockAvatar.getAvatarImg()).thenReturn("base.jpg");
        when(mockAvatar.getPoseImg()).thenReturn("pose.jpg");
        when(mockAvatar.getUpperMaskImg()).thenReturn("upper.jpg");

        // FastAPI 응답 Mocking
        FastApiTryOnResponse mockedFastApiResponse = mock(FastApiTryOnResponse.class);
        when(mockedFastApiResponse.getTryOnImgUrl()).thenReturn(newAvatarImgUrl);
        when(fastApiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/tryon")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(FastApiTryOnRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FastApiTryOnResponse.class)).thenReturn(Mono.just(mockedFastApiResponse));

        // when
        AvatarTryOnResponse response = avatarService.tryOn(member, request);

        // then
        verify(mockAvatar).wearGarment(newTop);
        verify(mockAvatar).update(newAvatarImgUrl);

        assertThat(response).isNotNull();
        assertThat(response.getAvatarImgUrl()).isEqualTo(newAvatarImgUrl);
        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getProducts()).extracting(AvatarTryOnResponse.ProductInfo::getProductName)
            .containsExactlyInAnyOrder("기존 하의", "새로운 상의");
    }
}