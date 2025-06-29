package com.tryiton.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // application.yml에 정의된 다른 서비스의 주소
    @Value("${FastApi.user-service.url}")
    private String userServiceUrl;

    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
            .baseUrl(userServiceUrl) // 요청의 기본 URL 설정
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // 기본 헤더 설정
            .build();
    }

}
