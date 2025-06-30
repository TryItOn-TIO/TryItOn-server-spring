package com.tryiton.core.member.controller;

import io.awspring.cloud.s3.S3Template;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URL;

@RestController
public class FileUploadController {
    @Autowired
    private S3Template s3Template;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @GetMapping("/api/files")
    public ResponseEntity<String> generatePresignedUrl(@RequestParam String fileName) {

        S3Presigner presigner = S3Presigner.builder().region(Region.AP_NORTHEAST_2).build();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
//                .contentType("image/jpeg")  // 파일 타입 지정(나중에 추가하기)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest));

        URL url = presignedRequest.url();

        presigner.close();

        return ResponseEntity.ok(url.toString());
    }
}
