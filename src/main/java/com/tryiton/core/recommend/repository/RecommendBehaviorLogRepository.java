package com.tryiton.core.recommend.repository;

import com.tryiton.core.recommend.entity.RecommendBehaviorLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendBehaviorLogRepository extends JpaRepository<RecommendBehaviorLog, String> {

    List<RecommendBehaviorLog> findByUserId(Long userId);
    List<RecommendBehaviorLog> findByProductId(Long productId);
    List<RecommendBehaviorLog> findByUserIdAndAction(Long userId, String action);
}
