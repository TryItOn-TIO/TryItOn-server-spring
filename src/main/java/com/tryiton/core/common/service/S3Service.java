package com.tryiton.core.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * S3에서 객체를 삭제합니다.
     * @param s3Key 삭제할 S3 객체의 키
     */
    public void deleteObject(String s3Key) {
        try {
            // 객체 존재 여부 확인
            if (!objectExists(s3Key)) {
                log.warn("삭제하려는 S3 객체가 존재하지 않습니다: {}", s3Key);
                return;
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("S3 객체 삭제 완료: {}", s3Key);
        } catch (Exception e) {
            log.error("S3 객체 삭제 실패: {}, 에러: {}", s3Key, e.getMessage());
            // 삭제 실패해도 예외를 던지지 않음 (기존 이미지 삭제 실패가 업데이트를 막지 않도록)
        }
    }

    /**
     * S3 URL에서 키를 추출합니다.
     * @param s3Url S3 URL
     * @return S3 키
     */
    public String extractS3KeyFromUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) {
            return null;
        }

        try {
            // https://bucket-name.s3.region.amazonaws.com/path/to/file.jpg
            // 또는 https://s3.region.amazonaws.com/bucket-name/path/to/file.jpg
            if (s3Url.contains(".s3.")) {
                // bucket-name.s3.region.amazonaws.com 형태
                String[] parts = s3Url.split(".com/", 2);
                if (parts.length > 1) {
                    return parts[1];
                }
            } else if (s3Url.contains("s3.")) {
                // s3.region.amazonaws.com/bucket-name 형태
                String[] parts = s3Url.split(bucketName + "/", 2);
                if (parts.length > 1) {
                    return parts[1];
                }
            }

            log.warn("S3 URL에서 키 추출 실패: {}", s3Url);
            return null;
        } catch (Exception e) {
            log.error("S3 URL 파싱 중 오류 발생: {}, 에러: {}", s3Url, e.getMessage());
            return null;
        }
    }

    /**
     * S3 객체가 존재하는지 확인합니다.
     * @param s3Key 확인할 S3 객체의 키
     * @return 존재 여부
     */
    private boolean objectExists(String s3Key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("S3 객체 존재 확인 중 오류: {}, 에러: {}", s3Key, e.getMessage());
            return false;
        }
    }
}
