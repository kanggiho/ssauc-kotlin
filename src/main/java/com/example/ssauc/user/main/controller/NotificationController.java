package com.example.ssauc.user.main.controller;

import com.example.ssauc.user.main.entity.Notification;
import com.example.ssauc.user.main.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.setReadStatus(0);
        notificationRepository.save(notification);

        return ResponseEntity.ok("readStatus updated to 0");
    }

    // 모든 알림 삭제 API
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllNotifications() {
        notificationRepository.deleteAll();
        return ResponseEntity.ok("All notifications cleared");
    }
}