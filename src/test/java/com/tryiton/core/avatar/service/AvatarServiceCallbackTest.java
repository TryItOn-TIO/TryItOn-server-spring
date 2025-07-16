package com.tryiton.core.avatar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.CallbackRequest;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.InitialAvatarResponse;
import com.tryiton.core.common.service.AsyncTaskService;
import com.tryiton.core.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles; // 1. @ActiveProfiles 임포트
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test") // 2. 테스트 프로필 활성화 어노테이션 추가
@SpringBootTest
@AutoConfigureMockMvc
class AvatarServiceCallbackTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("아바타 생성 요청 시, 성공 콜백을 받으면 AvatarCreateResponse를 정상 반환한다")
    void createAvatar_ShouldReturnResponse_WhenCallbackIsSuccessful() throws Exception {
        // given
        Member testMember = Member.builder().id(1L).build();
        // AvatarCreateRequest의 생성자에 맞게 수정
        AvatarCreateRequest request = new AvatarCreateRequest(testMember.getId().toString(), "http://example.com/image.jpg");

        // when
        CompletableFuture<AvatarCreateResponse> serviceFuture = CompletableFuture.supplyAsync(() ->
            avatarService.createAvatar(testMember, request)
        );

        Thread.sleep(500);

        // then
        InitialAvatarResponse mockResult = new InitialAvatarResponse();
        // InitialAvatarResponse에 Setter가 없으므로 Reflection을 사용하거나 DTO에 Setter를 추가해야 합니다.
        // 테스트의 편의를 위해 리플렉션 대신 DTO에 Setter를 추가했다고 가정합니다.
        mockResult.setTryOnImgUrl("http://example.com/image.jpg");
        mockResult.setPoseImgUrl("http://s3.../pose.png");
        mockResult.setUpperMaskImgUrl("http://s3.../upper.png");
        mockResult.setLowerMaskImgUrl("http://s3.../lower.png");


        CallbackRequest callbackRequest = new CallbackRequest();
        String taskId = asyncTaskService.getFutureMap().keySet().stream().findFirst().orElseThrow();
        callbackRequest.setTaskId(taskId);
        callbackRequest.setStatus("SUCCESS");
        callbackRequest.setResult(objectMapper.convertValue(mockResult, JsonNode.class));

        mockMvc.perform(post("/api/callbacks/vton")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackRequest)))
            .andExpect(status().isOk());

        AvatarCreateResponse finalResponse = serviceFuture.get(5, TimeUnit.SECONDS);

        assertThat(finalResponse).isNotNull();
        assertThat(finalResponse.getTryOnImgUrl()).isEqualTo(request.getTryOnImgUrl());
    }
}