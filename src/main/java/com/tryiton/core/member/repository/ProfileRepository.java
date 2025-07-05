package com.tryiton.core.member.repository;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByMember(Member member);
}
