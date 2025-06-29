package com.tryiton.core.avatar.repository;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/* 북마크 여부에 따른 조회 구분 명확히 하기! */
public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    // 옷장에 저장된 착장들 (북마크) 목록 조회 - 최근에 저장한 순으로
    List<Avatar> findAllByUserAndIsBookmarkedTrueOrderByCreatedAtDesc(Member user);

    // 최근 입혀본 착장 (1장) - 메인/카테고리 탭, 옷장
    Avatar findTopByUserIdOrderByCreatedAtDesc(Long userId);

    // 유저의 북마크 목록에서 특정 착장 하나를 클릭할 경우 해당 착장을 불러옴
    Optional<Avatar> findByAvatarIdAndUser(Long avatarId, Member user);
}
