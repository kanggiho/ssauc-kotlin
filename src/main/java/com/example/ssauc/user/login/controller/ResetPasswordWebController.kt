package com.example.ssauc.user.login.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ResetPasswordWebController {
    @GetMapping("/reset-password")
    fun resetPasswordForm(model: Model?): String {
        // "login/reset-password.html" 템플릿을 렌더링
        return "login/reset-password"
    }
}
