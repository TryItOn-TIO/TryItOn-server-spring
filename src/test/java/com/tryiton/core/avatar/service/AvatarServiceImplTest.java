package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.FastApiTryOnRequest;
import com.tryiton.core.avatar.dto.request.TryonAvatarTogetherNodeRequest;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.avatar.dto.response.FastApiTryOnResponse;
import com.tryiton.core.avatar.dto.response.TryonAvatarTogetherNodeResponse;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.entity.AvatarItem;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private Avatar avatar;

    @Mock
    private Product newTop, existingBottom;
    @Mock
    private Category topCategory, bottomCategory;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        avatar = Avatar.builder()
            .id(1L)
            .avatarImg("base.jpg")
            .member(Member.builder().id(1L).build())
            .build();
    }

    @Test
    @DisplayName("하의를 입은 아바타가 새 상의를 입으면, 착용 목록과 DTO가 올바르게 업데이트 되어야 한다")
    void tryOn_whenWearingBottom_shouldWearNewTopAndReturnCorrectDto() {
        // given
        Long userId = 1L;
        Long newTopId = 10L;
        String newAvatarImgUrl = "http://s3.new-avatar.com/image.jpg";
        Member member = Member.builder().id(userId).build();
        AvatarTryOnRequest request = new AvatarTryOnRequest(Long.toString(newTopId));

        // ⭐️ 'lenient()'를 사용하여 Mockito의 엄격한 검사를 완화합니다.
        lenient().when(existingBottom.isUpperGarment()).thenReturn(false);
        lenient().when(existingBottom.isLowerGarment()).thenReturn(true);
        lenient().when(existingBottom.getProductName()).thenReturn("기존 하의");
        lenient().when(existingBottom.getCategory()).thenReturn(bottomCategory);
        lenient().when(bottomCategory.getCategoryName()).thenReturn("하의");

        // 실제 Avatar 객체에 초기 상태(하의 착용)를 설정
        avatar.wearGarment(existingBottom);

        // Repository 동작 정의
        when(avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId)).thenReturn(avatar);
        when(productRepository.findById(newTopId)).thenReturn(Optional.of(newTop));

        // Mock 객체들 정보 정의
        when(newTop.isUpperGarment()).thenReturn(true);
        when(newTop.getImg2()).thenReturn("newTop.jpg");
        when(newTop.getProductName()).thenReturn("새로운 상의");
        when(newTop.getCategory()).thenReturn(topCategory);
        when(topCategory.getCategoryName()).thenReturn("상의");

        // FastAPI 응답 Mocking
        FastApiTryOnResponse mockedFastApiResponse = new FastApiTryOnResponse(newAvatarImgUrl);
        when(fastApiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/tryon")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(FastApiTryOnRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FastApiTryOnResponse.class)).thenReturn(Mono.just(mockedFastApiResponse));

        // when
        AvatarTryOnResponse response = avatarService.tryOn(member, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAvatarImgUrl()).isEqualTo(newAvatarImgUrl);
        assertThat(avatar.getItems()).hasSize(2);
        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getProducts()).extracting(AvatarTryOnResponse.ProductInfo::getProductName)
            .containsExactlyInAnyOrder("기존 하의", "새로운 상의");
    }

    @DisplayName("상의-하의 순차 피팅 성공")
    @Test
    void tryonTogether_Success_With_Sequential_Calls() {
        // --- Arrange (Given) ---
        Member member = Member.builder().id(1L).build();
        Avatar avatar = Avatar.builder()
            .id(10L)
            .member(member)
            .avatarImg("http://avatar.url/base.jpg")
            .build();

        Product top1 = createProduct(101L, "코튼 티셔츠", "상의");
        Product bottom1 = createProduct(201L, "데님 팬츠", "하의");
        Product bottom2 = createProduct(202L, "슬랙스", "하의");

        List<Long> topIds = List.of(top1.getId());
        List<Long> bottomIds = List.of(bottom1.getId(), bottom2.getId());
        TryonAvatarTogetherNodeRequest request = new TryonAvatarTogetherNodeRequest(topIds, bottomIds);

        // API 호출 순서에 따른 결과 이미지 URL 정의
        String topAppliedImgUrl = "http://result.url/top_applied.jpg";
        String finalImgUrl1 = "http://result.url/final_top1_bottom1.jpg";
        String finalImgUrl2 = "http://result.url/final_top1_bottom2.jpg";

        // API 호출 순서에 따른 Mock 응답 객체 생성
        FastApiTryOnResponse topApiResponse = new FastApiTryOnResponse(topAppliedImgUrl);
        FastApiTryOnResponse finalApiResponse1 = new FastApiTryOnResponse(finalImgUrl1);
        FastApiTryOnResponse finalApiResponse2 = new FastApiTryOnResponse(finalImgUrl2);

        // Repository 모킹
        when(avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(member.getId())).thenReturn(avatar);
        when(productRepository.findAllById(topIds)).thenReturn(List.of(top1));
        when(productRepository.findAllById(bottomIds)).thenReturn(List.of(bottom1, bottom2));

        // WebClient 모킹
        when(fastApiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/tryon")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(FastApiTryOnRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FastApiTryOnResponse.class))
            .thenReturn(Mono.just(topApiResponse))
            .thenReturn(Mono.just(finalApiResponse1))
            .thenReturn(Mono.just(finalApiResponse2));

        // --- Act (When) ---
        TryonAvatarTogetherNodeResponse response = avatarService.tryonTogether(member, request);

        // --- Assert (Then) ---
        assertNotNull(response);
        assertEquals(2, response.getCombinations().size());
        assertEquals(finalImgUrl1, response.getCombinations().get(0).getTryonImgUrl());
        assertEquals(top1.getProductName(), response.getCombinations().get(0).getTopProductName());
        assertEquals(bottom1.getProductName(), response.getCombinations().get(0).getBottomProductName());
        assertEquals(finalImgUrl2, response.getCombinations().get(1).getTryonImgUrl());
        verify(fastApiWebClient, times(3)).post();
    }

    @DisplayName("가상 피팅 실패 - 아바타 없음")
    @Test
    void tryonTogether_Fail_When_AvatarNotFound() {
        // --- Arrange (Given) ---
        Member member = Member.builder().id(1L).build();
        TryonAvatarTogetherNodeRequest request = new TryonAvatarTogetherNodeRequest(List.of(1L), List.of(2L));

        // 아바타를 찾지 못하도록 모킹
        when(avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(member.getId())).thenReturn(null);

        // --- Act & Assert ---
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            avatarService.tryonTogether(member, request);
        });

        assertEquals("가상 피팅을 진행할 아바타가 존재하지 않습니다.", exception.getMessage());
        verify(productRepository, never()).findAllById(any());
    }

    private Product createProduct(Long id, String name, String categoryName) {
        Category category = Category.builder().categoryName(categoryName).build();
        return Product.builder()
            .id(id)
            .productName(name)
            .img2("http://product.url/" + name + ".jpg")
            .category(category)
            .build();
    }
}