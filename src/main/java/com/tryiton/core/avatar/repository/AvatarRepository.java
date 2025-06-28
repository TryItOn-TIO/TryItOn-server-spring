package com.tryiton.core.avatar.repository;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    // 유저의 모든 아바타 (북마크된 착장) - 최근에 저장한 순으로
    List<Avatar> findAllByUserOrderByCreatedAtDesc(Member user);

    // 최근 착장 (1개 조회) - 옷장 왼쪽 영역에서도 사용
    Avatar findTopByUserIdOrderByCreatedAtDesc(Long userId);

    // 유저의 북마크 목록에서 특정 착장 하나를 클릭할 경우 해당 착장을 불러옴
    Optional<Avatar> findByAvatarIdAndUser(Long avatarId, Member user);
}
