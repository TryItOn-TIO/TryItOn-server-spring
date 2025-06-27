package com.tryiton.core.auth.email.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Getter
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "code", length = 6, nullable = false)
    private String code;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static EmailVerification create(String email, String code) {
        EmailVerification ev = new EmailVerification();
        ev.email = email;
        ev.code = code;
        ev.verified = false;
        ev.createdAt = LocalDateTime.now();
        return ev;
    }

    public void verify(String inputCode) {
        if (!this.code.equals(inputCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        this.verified = true;
    }

    public boolean isExpired() {
        return createdAt.isBefore(LocalDateTime.now().minusMinutes(5)); // 5분 만료
    }
}