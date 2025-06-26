package com.tryiton.core.member.entity;

import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "user_name")
    private String username;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender")
    private String gender;

    @Column(name = "password")
    private String password; // Google 로그인은 null 허용

    @Column(name = "password_expired", nullable = false)
    @Builder.Default
    private Boolean passwordExpired = false;

    @Column(name = "phone_num")
    private String phoneNum;

    @Column(name = "banned", nullable = false)
    @Builder.Default
    private Boolean banned = false;

    @Column(name = "withdraw", nullable = false)
    @Builder.Default
    private Boolean withdraw = false;

    // profile entity (회원 정보)
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Profile profile;

    // Role
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;
    
    // google or email
    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private AuthProvider provider;

    // google 로그인 유저의 고유 ID
    @Column(name = "provider_id")
    private String providerId;
}
