package com.example.ssauc.admin.controller;

import com.example.ssauc.admin.service.AdminService;
import com.example.ssauc.admin.entity.Admin;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.POST;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator.getOtpAuthURL;

@Slf4j
@RequestMapping("/admin")
@Controller
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping
    public String index() {
        return "admin/admin";
    }

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");

        model.addAttribute("adminName", admin.getAdminName());


        return "admin/adminhome";
    }

    //관리자 로그인 처리 (POST)
    // 1. adminId, adminPw로 checkAddress (실제로는 email, password)
    // 2. 검증 실패 시 로그인 화면으로 돌아감
    // 3. 검증 성공 시 Google Authenticator 등록 여부 확인
    // 미등록 시 /admin/enroll 페이지로
    // 등록 시 /admin/two-factor 페이지로
    @PostMapping("/home")
    public String home(@RequestParam("adminId") String adminId,
                       @RequestParam("adminPw") String adminPw,
                       Model model,
                       HttpSession session) {
        // 관리자 조회 (여기서는 adminId를 email로 사용)
        Admin admin = adminService.checkAddress(adminId, adminPw);
        if (admin == null) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "admin/admin";
        }

        // Google Authenticator 미등록이면 등록 처리
        if (admin.getGoogleSecret() == null || admin.getGoogleSecret().isEmpty()) {
            admin = adminService.generateGoogleSecret(admin);
            // QR 코드 URL 생성 (Google Chart API 사용)
            String qrUrl = getQrCodeUrl("ssauc", admin.getEmail(), admin.getGoogleSecret());
            log.info("Generated QR URL: {}", qrUrl);
            session.setAttribute("tempAdmin", admin);
            model.addAttribute("qrUrl", qrUrl);
            return "admin/enroll";
        } else {
            // 이미 등록되어 있으면 2FA 페이지로 이동
            session.setAttribute("tempAdmin", admin);
            return "redirect:/admin/two-factor";
        }
    }
    //직접 otpauth URL 생성 (secret key 기반)
    //URL 형식: otpauth://totp/{issuer}:{accountName}?secret={secret}&issuer={issuer}
    private String getQrCodeUrl(String issuer, String accountName, String secret) {
        String otpAuthUrl = "otpauth://totp/" + issuer + ":" + accountName + "?secret=" + secret + "&issuer=" + issuer;

        try {
            String encodedUrl = URLEncoder.encode(otpAuthUrl, "UTF-8");
            return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedUrl;
        } catch (UnsupportedEncodingException e) {
            log.error("QR 코드 URL 인코딩 에러", e);
            return "";
        }
    }


    // QR 등록(Enroll) 완료 후 2FA 코드 검증

    @PostMapping("/verify-enroll")
    public String verifyEnroll(@RequestParam("code") int code,
                               HttpSession session,
                               Model model) {
        Admin admin = (Admin) session.getAttribute("tempAdmin");
        if (admin == null) {
            return "redirect:/admin";
        }
        boolean isValid = adminService.verifyCode(admin, code);
        if (isValid) {
            session.setAttribute("admin", admin);
            session.removeAttribute("tempAdmin");
            session.setMaxInactiveInterval(30 * 60);
            return "redirect:/admin/home";
        } else {
            model.addAttribute("error", "인증 코드가 올바르지 않습니다.");
            return "admin/enroll";
        }
    }


    //2FA 코드 입력 페이지
    @GetMapping("/two-factor")
    public String twoFactor() {
        return "admin/twoFactor";
    }


    // 2FA 코드 검증 (이미 등록된 관리자 대상)

    @PostMapping("/two-factor")
    public String verifyTwoFactor(@RequestParam("code") int code,
                                  HttpSession session,
                                  Model model) {
        Admin admin = (Admin) session.getAttribute("tempAdmin");
        if (admin == null) {
            return "redirect:/admin";
        }
        boolean isValid = adminService.verifyCode(admin, code);
        if (isValid) {
            session.setAttribute("admin", admin);
            session.removeAttribute("tempAdmin");
            session.setMaxInactiveInterval(30 * 60);
            return "redirect:/admin/home";
        } else {
            model.addAttribute("error", "인증 코드가 올바르지 않습니다.");
            return "admin/twoFactor";
        }
    }

    /**
     * 로그아웃 처리
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin";
    }
}
