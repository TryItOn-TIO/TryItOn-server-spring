package com.tryiton.core.avatar.repository;

import com.tryiton.core.avatar.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    // 최근 착장 (1개 조회)
    Avatar findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
