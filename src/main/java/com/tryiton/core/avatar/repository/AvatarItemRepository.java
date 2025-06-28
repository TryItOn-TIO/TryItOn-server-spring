package com.tryiton.core.avatar.repository;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.entity.AvatarItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// 한 아바타에 여러 상품이 연결되어있고, 이 정보를 저장한 테이블 - AvatarItem
public interface AvatarItemRepository extends JpaRepository<AvatarItem, Long> {

    // 특정 아바타가 착장하고 있는 모든 상품을 조회
    List<AvatarItem> findAllByAvatar(Avatar avatar);

    // 특정 아바타 ID로 착장 상품 조회
    List<AvatarItem> findAllByAvatar_AvatarId(Long avatarId);
}
