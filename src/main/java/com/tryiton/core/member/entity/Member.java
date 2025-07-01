package com.tryiton.core.member.entity;

import com.tryiton.core.auth.oauth.entity.OauthCredentials;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.Gender;
import com.tryiton.core.common.enums.Style;
import com.tryiton.core.common.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
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

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "password")
    private String password; // Google 로그인은 null 허용

    @Column(name = "password_expired", nullable = false)
    @Builder.Default
    private Boolean passwordExpired = false;

    @Column(name = "phone_num", nullable = false, length = 18)
    private String phoneNum;

    @Column(name = "banned", nullable = false)
    @Builder.Default
    private Boolean banned = false;

    @Column(name = "withdraw", nullable = false)
    @Builder.Default
    private Boolean withdraw = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    // 페이지 네이션 고려
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Avatar> avatars = new ArrayList<>();

    // profile entity (회원 정보)
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Profile profile;

    // OAuth 정보는 선택적 1:1 관계
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, optional = true, fetch = FetchType.LAZY)
    private OauthCredentials oauthCredentials;

}
