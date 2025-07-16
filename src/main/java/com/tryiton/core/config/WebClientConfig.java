package com.tryiton.core.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${FastApi.user-service.url}")
    private String userServiceUrl;

    @Bean
    public WebClient userServiceWebClient() {
        // 1. Netty 기반의 HttpClient를 명시적으로 생성
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 연결 타임아웃 5초
            .responseTimeout(Duration.ofSeconds(60)) // 응답 타임아웃 60초
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS)) // 읽기 타임아웃
                    .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS))); // 쓰기 타임아웃

        log.info(">>> WebClient for FastAPI is configured with URL: {}", userServiceUrl);

        // 2. 생성된 HttpClient로 WebClient를 빌드
        return WebClient.builder()
            .baseUrl(userServiceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient)) // 생성한 http client를 명시적으로 연결
            .build();
    }
}