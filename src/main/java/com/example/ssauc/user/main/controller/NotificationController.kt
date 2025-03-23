package com.example.ssauc.user.main.controller

import com.example.ssauc.user.main.repository.NotificationRepository
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
class NotificationController {
    private val notificationRepository: NotificationRepository? = null

    @PatchMapping("/{notificationId}/read")
    fun markNotificationAsRead(@PathVariable notificationId: Long): ResponseEntity<String> {
        val notification = notificationRepository!!.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Notification not found") }!!

        notification.readStatus = 0
        notificationRepository.save(notification)

        return ResponseEntity.ok("readStatus updated to 0")
    }

    // 모든 알림 삭제 API
    @DeleteMapping("/clear")
    fun clearAllNotifications(): ResponseEntity<String> {
        notificationRepository!!.deleteAll()
        return ResponseEntity.ok("All notifications cleared")
    }
}