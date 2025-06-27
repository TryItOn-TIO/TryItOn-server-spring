package com.tryiton.core.auth.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAuthenticationCode(String email, String code) throws MessagingException {
        String htmlContent = """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
          <meta charset="UTF-8" />
          <title>TryItOn 이메일 인증</title>
          <style>
            body {
              margin: 0;
              padding: 0;
              background-color: #f2f3f8;
              font-family: 'Segoe UI', 'Pretendard', sans-serif;
            }
            .email-wrapper {
              max-width: 600px;
              margin: 40px auto;
              background: #ffffff;
              border-radius: 12px;
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
              overflow: hidden;
            }
            .email-header {
              background-color: #1976d2;
              color: white;
              padding: 20px 0;
              text-align: center;
            }
            .email-body {
              padding: 30px 40px;
              text-align: center;
            }
            .email-body h2 {
              margin-bottom: 16px;
              font-size: 20px;
              color: #333;
            }
            .email-body p {
              font-size: 16px;
              color: #555;
              margin: 8px 0;
            }
            .code-box {
              display: inline-block;
              background-color: #f0f7ff;
              color: #1976d2;
              font-size: 32px;
              font-weight: bold;
              padding: 14px 24px;
              margin: 24px 0;
              border-radius: 8px;
              letter-spacing: 6px;
            }
            .email-footer {
              font-size: 13px;
              color: #aaa;
              text-align: center;
              padding: 20px;
            }
          </style>
        </head>
        <body>
          <div class="email-wrapper">
            <div class="email-header">
              <h1>TryItOn</h1>
            </div>
            <div class="email-body">
              <h2>이메일 인증 코드</h2>
              <p>아래 인증 코드를 5분 내에 입력해 주세요.</p>
              <div class="code-box">""" + code + "</div>" + """
              <p>감사합니다 :)<br>TryItOn 팀 드림</p>
            </div>
            <div class="email-footer">
              ⓒ TryItOn. All rights reserved.
            </div>
          </div>
        </body>
        </html>
        """;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[TryItOn] 이메일 인증 코드");
        helper.setFrom("tryiton486@gmail.com");
        helper.setText(htmlContent, true); // true = HTML 모드

        mailSender.send(message);
    }
}