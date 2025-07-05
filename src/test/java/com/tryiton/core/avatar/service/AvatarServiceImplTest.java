package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.request.FastApiTogetherRequest;
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

//    @Test
//    @DisplayName("하의를 입은 아바타가 새 상의를 입으면, 착용 목록과 DTO가 올바르게 업데이트 되어야 한다")
//    void tryOn_whenWearingBottom_shouldWearNewTopAndReturnCorrectDto() {
//        // given
//        Long userId = 1L;
//        Long newTopId = 10L;
//        String newAvatarImgUrl = "http://s3.new-avatar.com/image.jpg";
//        Member member = Member.builder().id(userId).build();
//        AvatarTryOnRequest request = new AvatarTryOnRequest(Long.toString(newTopId));
//
//        // Repository 동작 정의
//        when(avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(userId)).thenReturn(mockAvatar);
//        when(productRepository.findById(newTopId)).thenReturn(Optional.of(newTop));
//
//        // Mock 객체들 정보 정의
//        when(newTop.isUpperGarment()).thenReturn(true);
//        when(newTop.getImg2()).thenReturn("newTop.jpg");
//        when(newTop.getProductName()).thenReturn("새로운 상의");
//        when(newTop.getCategory()).thenReturn(topCategory);
//        when(topCategory.getCategoryName()).thenReturn("상의");
//
//        when(existingBottom.getProductName()).thenReturn("기존 하의");
//        when(existingBottom.getCategory()).thenReturn(bottomCategory);
//        when(bottomCategory.getCategoryName()).thenReturn("하의");
//
//        // wearGarment가 호출된 '이후'의 최종 상태를 정의
//        when(mockAvatar.getItems()).thenReturn(List.of(new AvatarItem(mockAvatar, existingBottom), new AvatarItem(mockAvatar, newTop)));
//        when(mockAvatar.getAvatarImg()).thenReturn("base.jpg");
//
//        // FastAPI 응답 Mocking
//        FastApiTryOnResponse mockedFastApiResponse = mock(FastApiTryOnResponse.class);
//        when(mockedFastApiResponse.getTryOnImgUrl()).thenReturn(newAvatarImgUrl);
//        when(fastApiWebClient.post()).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.uri("/tryon")).thenReturn(requestBodySpec);
//        when(requestBodySpec.bodyValue(any(FastApiTryOnRequest.class))).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(FastApiTryOnResponse.class)).thenReturn(Mono.just(mockedFastApiResponse));
//
//        // when
//        AvatarTryOnResponse response = avatarService.tryOn(member, request);
//
//        // then
//        verify(mockAvatar).wearGarment(newTop);
//        verify(mockAvatar).update(newAvatarImgUrl);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getAvatarImgUrl()).isEqualTo(newAvatarImgUrl);
//        assertThat(response.getProducts()).hasSize(2);
//        assertThat(response.getProducts()).extracting(AvatarTryOnResponse.ProductInfo::getProductName)
//            .containsExactlyInAnyOrder("기존 하의", "새로운 상의");
//    }

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

        // WebClient 모킹: 호출 순서에 따라 다른 값을 반환하도록 설정
        when(fastApiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/tryon")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(FastApiTryOnRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // bodyToMono가 호출될 때마다 순서대로 다른 Mono 객체를 반환
        when(responseSpec.bodyToMono(FastApiTryOnResponse.class))
            .thenReturn(Mono.just(topApiResponse))      // 1. 첫 번째 호출(상의) 결과
            .thenReturn(Mono.just(finalApiResponse1))   // 2. 두 번째 호출(하의1) 결과
            .thenReturn(Mono.just(finalApiResponse2));  // 3. 세 번째 호출(하의2) 결과

        // --- Act (When) ---
        TryonAvatarTogetherNodeResponse response = avatarService.tryonTogether(member, request);

        // --- Assert (Then) ---
        assertNotNull(response);
        // 1개 상의 * 2개 하의 = 총 2개 조합 결과
        assertEquals(2, response.getCombinations().size());

        // 결과 검증
        assertEquals(finalImgUrl1, response.getCombinations().get(0).getTryonImgUrl());
        assertEquals(top1.getProductName(), response.getCombinations().get(0).getTopProductName());
        assertEquals(bottom1.getProductName(), response.getCombinations().get(0).getBottomProductName());
        assertEquals(finalImgUrl2, response.getCombinations().get(1).getTryonImgUrl());

        // 총 API 호출 횟수 검증 (상의 1번 + 하의 2번 = 3번)
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
        verify(productRepository, never()).findAllById(any()); // 아바타가 없으면 상품 조회는 일어나지 않아야 함
    }

    // 테스트 데이터 생성을 위한 헬퍼 메서드
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