package com.example.ssauc.user.main.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.main.entity.Notification;
import com.example.ssauc.user.main.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationResponseService {

    @Autowired
    NotificationRepository notificationRepository;

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUser_UserIdAndReadStatus(userId, 1);
    }
}
