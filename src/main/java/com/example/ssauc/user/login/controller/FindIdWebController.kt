package com.example.ssauc.user.login.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class FindIdWebController {
    @GetMapping("/find-id")
    fun findIdForm(model: Model?): String {
        // templates/login/find-id.html 템플릿을 렌더링
        return "login/find-id"
    }
}
