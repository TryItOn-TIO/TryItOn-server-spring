package com.tryiton.core.closet.repository;

import com.tryiton.core.closet.entity.ClosetAvatarItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClosetAvatarItemRepository extends JpaRepository<ClosetAvatarItem, Long> {

    // 특정 착장에 포함된 모든 아이템 조회
    List<ClosetAvatarItem> findByClosetAvatarId(Long closetAvatarId);

    // 특정 착장 내에서 상의 등을 포함한 아이템 조회
    @Query("""
        SELECT cai FROM ClosetAvatarItem cai
        JOIN FETCH cai.product p
        JOIN FETCH p.category c
        WHERE cai.closetAvatar.id = :closetAvatarId
        AND c.parentCategory.id = :parentCategoryId
        """)
    List<ClosetAvatarItem> findItemsByClosetAvatarIdAndParentCategory(
        @Param("closetAvatarId") Long closetAvatarId,
        @Param("parentCategoryId") Long parentCategoryId
    );
}
