package com.tryiton.core.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password; // Google 로그인은 null 허용

    // 추가 정보
    private String nickname;
    private String preferredStyle;
    private Integer height;  // cm
    private Integer weight;  // kg
    private Integer age;

    // Role
    private UserRole role;

    // google or email
    private AuthProvider provider;

    // google 로그인 유저의 고유 ID
    private String providerId;
}
