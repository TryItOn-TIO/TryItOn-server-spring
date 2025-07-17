package com.tryiton.core.common.service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * 비동기 작업을 관리하는 서비스.
 * 각 작업에 고유 ID를 부여하고 CompletableFuture를 통해 작업 완료를 추적합니다.
 */
@Service
public class AsyncTaskService {

    private final Map<String, CompletableFuture<Object>> pendingTasks = new ConcurrentHashMap<>();

    /**
     * 새로운 비동기 작업을 등록하고 고유 ID를 반환합니다.
     * @return 생성된 작업 ID
     */
    public String registerTask() {
        String taskId = UUID.randomUUID().toString();
        pendingTasks.put(taskId, new CompletableFuture<>());
        return taskId;
    }

    /**
     * 특정 작업 ID에 해당하는 CompletableFuture를 반환합니다.
     * @param taskId 작업 ID
     * @return 해당 작업의 CompletableFuture
     */
    public CompletableFuture<Object> getFuture(String taskId) {
        return pendingTasks.get(taskId);
    }

    /**
     * 작업을 성공적으로 완료 처리합니다.
     * @param taskId 작업 ID
     * @param result 작업 결과
     */
    public void completeTask(String taskId, Object result) {
        CompletableFuture<Object> future = pendingTasks.remove(taskId);
        if (future != null) {
            future.complete(result);
        }
    }

    /**
     * 작업을 실패 처리합니다.
     * @param taskId 작업 ID
     * @param e      발생한 예외
     */
    public void failTask(String taskId, Exception e) {
        CompletableFuture<Object> future = pendingTasks.remove(taskId);
        if (future != null) {
            future.completeExceptionally(e);
        }
    }

    public Map<String, CompletableFuture<Object>> getFutureMap() {
        return Collections.unmodifiableMap(pendingTasks);
    }
}