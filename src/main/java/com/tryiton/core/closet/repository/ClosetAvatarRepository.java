package com.tryiton.core.closet.repository;

import com.tryiton.core.closet.entity.ClosetAvatar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClosetAvatarRepository extends JpaRepository<ClosetAvatar, Long> {

    // 유저가 저장한 모든 착장 조회
    List<ClosetAvatar> findByUserId(Long userId);

    // 유저가 저장한 착장 수 조회 - 착장이 10개 초과하면 저장 제한하기위해
    @Query("SELECT COUNT(ca) FROM ClosetAvatar ca WHERE ca.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // 유저가 저장한 착장과 포함된 아이템들 함께 조회
    @Query("SELECT ca FROM ClosetAvatar ca JOIN FETCH ca.items WHERE ca.user.id = :userId")
    List<ClosetAvatar> findWithItemsByUserId(@Param("userId") Long userId);

    // 유저가 착장을 하나라도 저장했는지 여부
    boolean existsByUserId(Long userId);

    // 특정 착장 ID가 해당 유저의 것인지의 여부 <- 삭제할때!
    boolean existsByIdAndUserId(Long closetAvatarId, Long userId);

    // 특정 착장 ID로 유저 소유 착장 조회
    Optional<ClosetAvatar> findByIdAndUserId(Long id, Long userId);
}
