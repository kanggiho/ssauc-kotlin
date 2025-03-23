package com.example.ssauc.user.chat.controller

import com.example.ssauc.user.chat.dto.BanRequestDto
import com.example.ssauc.user.chat.service.BanService
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
class BanController {
    private val banService: BanService? = null

    // 차단 로직 실행
    @PostMapping("/ban")
    fun banUser(@RequestBody banRequestDto: BanRequestDto): ResponseEntity<String> {
        try {
            banService!!.banUser(banRequestDto.userId, banRequestDto.blockedUserId)
            return ResponseEntity.ok("차단에 성공했습니다.")
        } catch (e: IllegalStateException) {
            // 이미 차단되어 있는 경우 (예외 메시지로 전달한 내용을 그대로 응답)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 차단되어 있습니다.")
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("차단에 실패했습니다.")
        }
    }

    @PostMapping("/unban")
    fun unbanUser(@RequestBody banRequestDto: BanRequestDto): ResponseEntity<String> {
        try {
            banService!!.unbanUser(banRequestDto.userId, banRequestDto.blockedUserId)
            return ResponseEntity.ok("차단 해제에 성공했습니다.")
        } catch (e: IllegalStateException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("차단 해제에 실패했습니다.")
        }
    }
}
