package com.tryiton.core.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 여러 Redis 엔드포인트 간에 공유되는 데이터에 대한 일관된 접근을 제공하는 클래스
 */
@Slf4j
@Component
public class SharedDataAccessor {
    private final RedisTemplate<String, Object> cacheRedisTemplate;
    private final ObjectMapper objectMapper;
    
    public SharedDataAccessor(
            @Qualifier("cacheRedisTemplate") RedisTemplate<String, Object> cacheRedisTemplate,
            ObjectMapper objectMapper) {
        this.cacheRedisTemplate = cacheRedisTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 공유 데이터 저장
     * @param key 데이터 키
     * @param value 저장할 값
     */
    public void saveSharedData(String key, Object value) {
        String prefixedKey = "shared:" + key;
        try {
            if (value instanceof String) {
                cacheRedisTemplate.opsForValue().set(prefixedKey, value);
            } else {
                cacheRedisTemplate.opsForValue().set(prefixedKey, objectMapper.writeValueAsString(value));
            }
            log.debug("공유 데이터 저장 성공: {}", prefixedKey);
        } catch (Exception e) {
            log.error("공유 데이터 저장 실패: {}, 오류: {}", prefixedKey, e.getMessage(), e);
            throw new RuntimeException("공유 데이터 저장 실패", e);
        }
    }
    
    /**
     * 만료 시간과 함께 공유 데이터 저장
     * @param key 데이터 키
     * @param value 저장할 값
     * @param timeout 만료 시간
     * @param unit 시간 단위
     */
    public void saveSharedData(String key, Object value, long timeout, TimeUnit unit) {
        String prefixedKey = "shared:" + key;
        try {
            if (value instanceof String) {
                cacheRedisTemplate.opsForValue().set(prefixedKey, value, timeout, unit);
            } else {
                cacheRedisTemplate.opsForValue().set(prefixedKey, objectMapper.writeValueAsString(value), timeout, unit);
            }
            log.debug("공유 데이터 저장 성공 (TTL 설정): {}, TTL: {} {}", prefixedKey, timeout, unit);
        } catch (Exception e) {
            log.error("공유 데이터 저장 실패: {}, 오류: {}", prefixedKey, e.getMessage(), e);
            throw new RuntimeException("공유 데이터 저장 실패", e);
        }
    }
    
    /**
     * 공유 데이터 조회
     * @param key 데이터 키
     * @param type 반환 타입 클래스
     * @return 저장된 값 또는 null
     */
    public <T> T getSharedData(String key, Class<T> type) {
        String prefixedKey = "shared:" + key;
        try {
            Object value = cacheRedisTemplate.opsForValue().get(prefixedKey);
            
            if (value == null) {
                log.debug("공유 데이터 없음: {}", prefixedKey);
                return null;
            }
            
            // 타입 변환 처리
            if (type.isInstance(value)) {
                return type.cast(value);
            } else {
                log.debug("타입 변환 시도: {} -> {}", value.getClass().getName(), type.getName());
                return objectMapper.convertValue(value, type);
            }
        } catch (Exception e) {
            log.error("공유 데이터 조회 실패: {}, 오류: {}", prefixedKey, e.getMessage(), e);
            throw new RuntimeException("공유 데이터 조회 실패", e);
        }
    }
    
    /**
     * 공유 데이터 삭제
     * @param key 데이터 키
     * @return 삭제 성공 여부
     */
    public boolean deleteSharedData(String key) {
        String prefixedKey = "shared:" + key;
        try {
            Boolean result = cacheRedisTemplate.delete(prefixedKey);
            log.debug("공유 데이터 삭제: {}, 결과: {}", prefixedKey, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("공유 데이터 삭제 실패: {}, 오류: {}", prefixedKey, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 공유 데이터 존재 여부 확인
     * @param key 데이터 키
     * @return 존재 여부
     */
    public boolean hasSharedData(String key) {
        String prefixedKey = "shared:" + key;
        try {
            Boolean result = cacheRedisTemplate.hasKey(prefixedKey);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("공유 데이터 존재 확인 실패: {}, 오류: {}", prefixedKey, e.getMessage(), e);
            return false;
        }
    }
}
