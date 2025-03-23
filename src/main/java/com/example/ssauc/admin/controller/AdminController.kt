package com.example.ssauc.admin.controller

import com.example.ssauc.admin.entity.Admin
import com.example.ssauc.admin.service.AdminService
import jakarta.servlet.http.HttpSession
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

@Slf4j
@RequestMapping("/admin")
@Controller
class AdminController {
    @Autowired
    private val adminService: AdminService? = null

    @GetMapping
    fun index(): String {
        return "admin/admin"
    }

    @GetMapping("/home")
    fun home(model: Model, session: HttpSession): String {
        val admin = session.getAttribute("admin") as Admin

        model.addAttribute("adminName", admin.adminName)


        return "admin/adminhome"
    }

    //관리자 로그인 처리 (POST)
    // 1. adminId, adminPw로 checkAddress (실제로는 email, password)
    // 2. 검증 실패 시 로그인 화면으로 돌아감
    // 3. 검증 성공 시 Google Authenticator 등록 여부 확인
    // 미등록 시 /admin/enroll 페이지로
    // 등록 시 /admin/two-factor 페이지로
    @PostMapping("/home")
    fun home(
        @RequestParam("adminId") adminId: String?,
        @RequestParam("adminPw") adminPw: String?,
        model: Model,
        session: HttpSession
    ): String {
        // 관리자 조회 (여기서는 adminId를 email로 사용)
        var admin = adminService!!.checkAddress(adminId, adminPw)
        if (admin == null) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 올바르지 않습니다.")
            return "admin/admin"
        }

        // Google Authenticator 미등록이면 등록 처리
        if (admin.googleSecret == null || admin.googleSecret.isEmpty()) {
            admin = adminService.generateGoogleSecret(admin)
            // QR 코드 URL 생성 (Google Chart API 사용)
            val qrUrl = getQrCodeUrl("ssauc", admin.email, admin.googleSecret)
            AdminController.log.info("Generated QR URL: {}", qrUrl)
            session.setAttribute("tempAdmin", admin)
            model.addAttribute("qrUrl", qrUrl)
            return "admin/enroll"
        } else {
            // 이미 등록되어 있으면 2FA 페이지로 이동
            session.setAttribute("tempAdmin", admin)
            return "redirect:/admin/two-factor"
        }
    }

    //직접 otpauth URL 생성 (secret key 기반)
    //URL 형식: otpauth://totp/{issuer}:{accountName}?secret={secret}&issuer={issuer}
    private fun getQrCodeUrl(issuer: String, accountName: String, secret: String): String {
        val otpAuthUrl = "otpauth://totp/$issuer:$accountName?secret=$secret&issuer=$issuer"

        try {
            val encodedUrl = URLEncoder.encode(otpAuthUrl, "UTF-8")
            return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=$encodedUrl"
        } catch (e: UnsupportedEncodingException) {
            AdminController.log.error("QR 코드 URL 인코딩 에러", e)
            return ""
        }
    }


    // QR 등록(Enroll) 완료 후 2FA 코드 검증
    @PostMapping("/verify-enroll")
    fun verifyEnroll(
        @RequestParam("code") code: Int,
        session: HttpSession,
        model: Model
    ): String {
        val admin = session.getAttribute("tempAdmin") as Admin
            ?: return "redirect:/admin"
        val isValid = adminService!!.verifyCode(admin, code)
        if (isValid) {
            session.setAttribute("admin", admin)
            session.removeAttribute("tempAdmin")
            session.maxInactiveInterval = 30 * 60
            return "redirect:/admin/home"
        } else {
            model.addAttribute("error", "인증 코드가 올바르지 않습니다.")
            return "admin/enroll"
        }
    }


    //2FA 코드 입력 페이지
    @GetMapping("/two-factor")
    fun twoFactor(): String {
        return "admin/twoFactor"
    }


    // 2FA 코드 검증 (이미 등록된 관리자 대상)
    @PostMapping("/two-factor")
    fun verifyTwoFactor(
        @RequestParam("code") code: Int,
        session: HttpSession,
        model: Model
    ): String {
        val admin = session.getAttribute("tempAdmin") as Admin
            ?: return "redirect:/admin"
        val isValid = adminService!!.verifyCode(admin, code)
        if (isValid) {
            session.setAttribute("admin", admin)
            session.removeAttribute("tempAdmin")
            session.maxInactiveInterval = 30 * 60
            return "redirect:/admin/home"
        } else {
            model.addAttribute("error", "인증 코드가 올바르지 않습니다.")
            return "admin/twoFactor"
        }
    }

    /**
     * 로그아웃 처리
     */
    @GetMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/admin"
    }
}
