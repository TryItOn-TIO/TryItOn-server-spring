package com.tryiton.core.member.entity;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    public List<Avatar> avatars = new ArrayList<>();


}
