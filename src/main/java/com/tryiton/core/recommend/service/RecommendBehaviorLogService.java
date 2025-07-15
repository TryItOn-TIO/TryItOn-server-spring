package com.tryiton.core.recommend.service;

import com.tryiton.core.common.enums.RecommendAction;
import com.tryiton.core.recommend.entity.RecommendBehaviorLog;
import com.tryiton.core.recommend.repository.RecommendBehaviorLogRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RecommendBehaviorLogService {

    private final RecommendBehaviorLogRepository recommendBehaviorLogRepository;

    public RecommendBehaviorLogService(
        RecommendBehaviorLogRepository recommendBehaviorLogRepository) {
        this.recommendBehaviorLogRepository = recommendBehaviorLogRepository;
    }

    @Async
    public void logUserAction(Long userId, Long productId, RecommendAction action){
        recommendBehaviorLogRepository.save(
            RecommendBehaviorLog.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .productId(productId)
                .action(action)
                .score(action.getScore())
                .createdAt(LocalDateTime.now())
                .build()
        );
    }
}
