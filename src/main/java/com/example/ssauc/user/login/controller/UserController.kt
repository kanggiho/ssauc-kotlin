package com.example.ssauc.user.login.controller;


import com.example.ssauc.user.login.dto.UserRegistrationDTO;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 회원가입/중복검사 REST API
 * signup.js에서 fetch()로 호출
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UsersRepository userRepository;

    /**
     * (1) 회원가입 처리
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO dto) {
        String result = userService.register(dto);
        if ("회원가입 성공".equals(result)) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * (2) 이메일 중복 확인
     */

    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam("email") String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("유효한 이메일 주소를 입력하세요.");
        }
        Optional<Users> existingUserOpt = userRepository.findByEmail(email);
        if(existingUserOpt.isPresent()){
            Users user = existingUserOpt.get();
            if("blocked".equalsIgnoreCase(user.getStatus())){
                return ResponseEntity.badRequest().body("해당 이메일은 가입이 불가합니다.");
            } else if("inactive".equalsIgnoreCase(user.getStatus())){
                return ResponseEntity.ok("사용 가능한 이메일입니다.");
            } else {
                return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
            }
        }
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    /**
     * (3) 닉네임 중복 확인
     */
    @GetMapping("/check-username")
    public ResponseEntity<String> checkUsername(
            @RequestParam("username") String username,
            @RequestParam(value = "email", required = false) String email) {
        if (username == null || username.trim().length() < 2) {
            return ResponseEntity.badRequest().body("닉네임은 최소 2글자 이상이어야 합니다.");
        }
        Optional<Users> userOpt = userRepository.findByUserName(username);
        if(userOpt.isPresent()){
            Users user = userOpt.get();
            // 만약 추가 파라미터로 전달된 이메일이 DB에 있는 사용자와 동일하다면 재가입 대상이므로 사용 가능
            if(email != null && email.equalsIgnoreCase(user.getEmail())){
                return ResponseEntity.ok("사용 가능한 닉네임입니다.");
            }
            // inactive 상태라면 재가입 가능 (이메일과는 다를 수 있으나, 보통 재가입 시에는 이메일로 구분하므로 우선 available 처리)
            else if("inactive".equalsIgnoreCase(user.getStatus())){
                return ResponseEntity.ok("사용 가능한 닉네임입니다.");
            } else {
                return ResponseEntity.badRequest().body("이미 사용 중인 닉네임입니다.");
            }
        }
        return ResponseEntity.ok("사용 가능한 닉네임입니다.");
    }

    @GetMapping("/check-phone")
    public ResponseEntity<String> checkPhone(
            @RequestParam("phone") String phone,
            @RequestParam(value = "email", required = false) String email) {
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("유효한 휴대폰 번호를 입력하세요.");
        }
        Optional<Users> userOpt = userRepository.findByPhone(phone);
        if(userOpt.isPresent()){
            Users user = userOpt.get();
            if(email != null && email.equalsIgnoreCase(user.getEmail())){
                return ResponseEntity.ok("사용 가능한 휴대폰 번호입니다.");
            } else if("inactive".equalsIgnoreCase(user.getStatus())){
                return ResponseEntity.ok("사용 가능한 휴대폰 번호입니다.");
            } else {
                return ResponseEntity.badRequest().body("이미 사용 중인 휴대폰 번호입니다.");
            }
        }
        return ResponseEntity.ok("사용 가능한 휴대폰 번호입니다.");
    }
}