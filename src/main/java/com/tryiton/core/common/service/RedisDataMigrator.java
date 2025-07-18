package com.tryiton.core.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 데이터 마이그레이션을 위한 유틸리티 클래스
 * 애플리케이션 시작 시 기존 Redis 데이터를 공유 데이터 형식으로 마이그레이션
 */
@Slf4j
@Component
@Profile("!test") // 테스트 환경에서는 실행하지 않음
public class RedisDataMigrator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SharedDataAccessor sharedDataAccessor;

    public RedisDataMigrator(
            @Qualifier("cacheRedisTemplate") RedisTemplate<String, Object> redisTemplate,
            SharedDataAccessor sharedDataAccessor) {
        this.redisTemplate = redisTemplate;
        this.sharedDataAccessor = sharedDataAccessor;
    }

    /**
     * 애플리케이션 시작 시 Redis 데이터 마이그레이션 수행
     */
    @EventListener(ApplicationReadyEvent.class)
    public void migrateRedisData() {
        log.info("Redis 데이터 마이그레이션 시작...");
        
        try {
            // 1. 상품 상세 정보 마이그레이션
            migrateProductDetailData();
            
            // 2. 추천 데이터 마이그레이션
            migrateRecommendationData();
            
            log.info("Redis 데이터 마이그레이션 완료");
        } catch (Exception e) {
            log.error("Redis 데이터 마이그레이션 중 오류 발생", e);
        }
    }
    
    /**
     * 상품 상세 정보 마이그레이션
     */
    private void migrateProductDetailData() {
        log.info("상품 상세 정보 마이그레이션 시작");
        
        // 상품 상세 정보 키 패턴
        Set<String> productDetailKeys = redisTemplate.keys("productDetail::*");
        if (productDetailKeys == null || productDetailKeys.isEmpty()) {
            log.info("마이그레이션할 상품 상세 정보가 없습니다.");
            return;
        }
        
        log.info("{}개의 상품 상세 정보 마이그레이션 중...", productDetailKeys.size());
        int successCount = 0;
        
        for (String key : productDetailKeys) {
            try {
                // 기존 키에서 상품 ID 추출
                String productId = key.replace("productDetail::", "");
                
                // 데이터 조회
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    // 새로운 형식으로 저장
                    String newKey = "product:" + productId;
                    sharedDataAccessor.saveSharedData(newKey, value, 10, TimeUnit.MINUTES);
                    successCount++;
                }
            } catch (Exception e) {
                log.warn("키 '{}' 마이그레이션 실패: {}", key, e.getMessage());
            }
        }
        
        log.info("상품 상세 정보 마이그레이션 완료: {}개 성공, {}개 실패", 
                successCount, productDetailKeys.size() - successCount);
    }
    
    /**
     * 추천 데이터 마이그레이션
     */
    private void migrateRecommendationData() {
        log.info("추천 데이터 마이그레이션 시작");
        
        // 추천 데이터 키 패턴
        Set<String> recommendKeys = redisTemplate.keys("recommend:*");
        if (recommendKeys == null || recommendKeys.isEmpty()) {
            log.info("마이그레이션할 추천 데이터가 없습니다.");
            return;
        }
        
        log.info("{}개의 추천 데이터 마이그레이션 중...", recommendKeys.size());
        int successCount = 0;
        
        for (String key : recommendKeys) {
            try {
                // 기존 키에서 추천 유형 추출
                String recommendType = key.replace("recommend:", "");
                
                // 데이터 조회
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    // 새로운 형식으로 저장
                    sharedDataAccessor.saveSharedData(recommendType, value, 30, TimeUnit.MINUTES);
                    successCount++;
                }
            } catch (Exception e) {
                log.warn("키 '{}' 마이그레이션 실패: {}", key, e.getMessage());
            }
        }
        
        log.info("추천 데이터 마이그레이션 완료: {}개 성공, {}개 실패", 
                successCount, recommendKeys.size() - successCount);
    }
}
