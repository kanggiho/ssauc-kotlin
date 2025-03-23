package com.example.ssauc.user.main.service

import com.example.ssauc.user.main.entity.Notification
import com.example.ssauc.user.main.repository.NotificationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotificationResponseService {
    @Autowired
    var notificationRepository: NotificationRepository? = null

    fun getUnreadNotifications(userId: Long?): List<Notification?>? {
        return notificationRepository!!.findByUser_UserIdAndReadStatus(userId, 1)
    }
}
