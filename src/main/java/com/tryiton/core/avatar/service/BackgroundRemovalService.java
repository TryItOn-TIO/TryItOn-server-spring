package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.request.RemoveBgRequest;
import com.tryiton.core.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackgroundRemovalService {

    private final S3Client s3Client;
    
    @Value("${remove-bg.api-key}")
    private String removeBgApiKey;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * Remove.bg API를 사용하여 배경을 제거합니다
     */
    public String removeBackground(String imageUrl, Long userId) {
        try {
            log.info("배경 제거 시작 - userId: {}, imageUrl: {}", userId, imageUrl);

            // Remove.bg API 호출
            byte[] processedImageData = callRemoveBgApi(imageUrl);
            
            // S3에 업로드
            String s3Key = generateS3Key(userId);
            String processedImageUrl = uploadToS3(processedImageData, s3Key);
            
            log.info("배경 제거 완료 - userId: {}, processedUrl: {}", userId, processedImageUrl);
            
            return processedImageUrl;
            
        } catch (Exception e) {
            log.error("배경 제거 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "배경 제거 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * Remove.bg API 호출
     */
    private byte[] callRemoveBgApi(String imageUrl) {
        WebClient webClient = WebClient.builder()
            .baseUrl("https://api.remove.bg")
            .build();

        try {
            return webClient.post()
                .uri("/v1.0/removebg")
                .header("X-Api-Key", removeBgApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                    "image_url", imageUrl,
                    "size", "auto",
                    "format", "png"
                ))
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(errorBody -> new RuntimeException("Remove.bg API 오류: " + errorBody))
                )
                .bodyToMono(byte[].class)
                .block();
                
        } catch (Exception e) {
            log.error("Remove.bg API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("Remove.bg API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * S3 키 생성
     */
    private String generateS3Key(Long userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("background-removed/%d/%s.png", userId, timestamp);
    }

    /**
     * S3에 이미지 업로드
     */
    private String uploadToS3(byte[] imageData, String s3Key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("image/png")
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageData));
            
            // Public URL 생성
            String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, s3Key);
            
            log.info("S3 업로드 완료: {}", publicUrl);
            return publicUrl;
            
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage());
        }
    }
}
