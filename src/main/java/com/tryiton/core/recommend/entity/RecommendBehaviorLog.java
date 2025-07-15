package com.tryiton.core.recommend.entity;

import com.tryiton.core.common.enums.RecommendAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recommend_behavior_log")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendBehaviorLog {

    @Id
    @Column(name = "recommend_behavior_log_id", length = 255)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private RecommendAction action;

    @Column(name = "score", nullable = false)
    private float score;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;
}