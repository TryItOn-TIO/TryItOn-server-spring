package com.tryiton.core.avatar.repository;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    // 가장 최근 입혀본 착장 (1장)
    Avatar findTopByMemberIdOrderByCreatedAtDesc(Long userId);

    // 유저의 특정 착장 하나를 클릭했을 때 해당 아바타를 불러옴
    Optional<Avatar> findByIdAndMember(Long id, Member user);
}