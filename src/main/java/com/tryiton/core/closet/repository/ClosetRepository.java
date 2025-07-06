package com.tryiton.core.closet.repository;

import com.tryiton.core.closet.entity.Closet;
import com.tryiton.core.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetRepository extends JpaRepository<Closet, Long> {
    Optional<Closet> findByMemberAndAvatarId(Member member, Long avatarId);

    List<Closet> findAllByMemberOrderByCreatedAtDesc(Member member);
}
