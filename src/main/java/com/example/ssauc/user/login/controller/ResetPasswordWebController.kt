package com.example.ssauc.user.login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResetPasswordWebController {

    @GetMapping("/reset-password")
    public String resetPasswordForm(Model model) {
        // "login/reset-password.html" 템플릿을 렌더링
        return "login/reset-password";
    }
}
