package com.example.ssauc.user.main.controller;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.main.entity.Notification;
import com.example.ssauc.user.main.service.NotificationResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalNotificationAdvice {

    private final NotificationResponseService notificationService;
    private final UsersRepository userRepository;
    private final TokenExtractor tokenExtractor;

    @ModelAttribute
    public void addNotificationsToModel(Model model, HttpServletRequest request) {
        // 기본값: 빈 리스트
        model.addAttribute("notifications", List.of());

        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return;
        }

        // tokenExtractor가 반환한 Users 객체에서 username 꺼내기
        String username = user.getUserName();

        userRepository.findByUserName(username)
                .or(() -> userRepository.findByEmail(username))
                .ifPresent(u -> {
                    List<Notification> unread = notificationService.getUnreadNotifications(u.getUserId());
                    model.addAttribute("notifications", unread);
                });
    }
}


