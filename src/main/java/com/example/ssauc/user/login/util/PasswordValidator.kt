package com.example.ssauc.user.login.util

object PasswordValidator {
    /**
     * 예: 최소 8자, 영문/숫자/특수문자 1개 이상
     */
    @JvmStatic
    fun isValid(password: String?): Boolean {
        if (password == null) return false
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@!#$%^&*])[A-Za-z\\d@!#$%^&*]{8,}$".toRegex())
    }
}
