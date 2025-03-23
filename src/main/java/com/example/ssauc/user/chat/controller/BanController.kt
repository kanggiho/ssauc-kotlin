package com.example.ssauc.user.chat.controller;

import com.example.ssauc.user.chat.dto.BanRequestDto;
import com.example.ssauc.user.chat.service.BanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class BanController {

    private final BanService banService;

    // 차단 로직 실행
    @PostMapping("/ban")
    public ResponseEntity<String> banUser(@RequestBody BanRequestDto banRequestDto) {
        try {
            banService.banUser(banRequestDto.getUserId(), banRequestDto.getBlockedUserId());
            return ResponseEntity.ok("차단에 성공했습니다.");
        } catch (IllegalStateException e) {
            // 이미 차단되어 있는 경우 (예외 메시지로 전달한 내용을 그대로 응답)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 차단되어 있습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("차단에 실패했습니다.");
        }
    }

    @PostMapping("/unban")
    public ResponseEntity<String> unbanUser(@RequestBody BanRequestDto banRequestDto) {
        try {
            banService.unbanUser(banRequestDto.getUserId(), banRequestDto.getBlockedUserId());
            return ResponseEntity.ok("차단 해제에 성공했습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("차단 해제에 실패했습니다.");
        }
    }

}
