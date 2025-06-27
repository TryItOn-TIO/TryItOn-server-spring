package com.tryiton.core.auth.email.repository;

import com.tryiton.core.auth.email.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {

}
