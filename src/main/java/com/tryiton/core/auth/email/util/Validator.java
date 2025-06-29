package com.tryiton.core.auth.email.util;

import java.util.regex.Pattern;

public class Validator {

    private static final String PASSWORD_REGEX =
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/~`|\\\\]).{8,}$";

    private static final String EMAIL_REGEX =
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    private static final String NICKNAME_REGEX =
        "^[가-힣a-zA-Z0-9]{2,16}$"; // 한글, 영문, 숫자 2~16자

    private static final String PHONE_REGEX =
        "^01[016789]-?\\d{3,4}-?\\d{4}$"; // 대한민국 휴대전화 형식

    public static void validatePassword(String password) {
        if (password == null || !Pattern.matches(PASSWORD_REGEX, password)) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || !Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("유효한 이메일 형식이 아닙니다.");
        }
    }

    public static void validateNickname(String nickname) {
        if (nickname == null || !Pattern.matches(NICKNAME_REGEX, nickname)) {
            throw new IllegalArgumentException("닉네임은 2~16자의 한글, 영문, 숫자만 사용할 수 있습니다.");
        }
    }

    public static void validatePhone(String phoneNum) {
        if (phoneNum == null || !Pattern.matches(PHONE_REGEX, phoneNum)) {
            throw new IllegalArgumentException("유효한 휴대전화 번호 형식이 아닙니다.");
        }
    }
}
