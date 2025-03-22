package com.example.ssauc.user.login.util;

public class PasswordValidator {

    /**
     * 예: 최소 8자, 영문/숫자/특수문자 1개 이상
     */
    public static boolean isValid(String password) {
        if (password == null) return false;
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@!#$%^&*])[A-Za-z\\d@!#$%^&*]{8,}$");
    }
}
