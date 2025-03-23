package com.example.ssauc.user.main.controller

import org.springframework.security.core.Authentication

@ControllerAdvice
@lombok.RequiredArgsConstructor
class GlobalNotificationAdvice {
    private val notificationService: NotificationResponseService? = null
    private val userRepository: UsersRepository? = null
    private val tokenExtractor: TokenExtractor? = null

    @ModelAttribute
    fun addNotificationsToModel(model: org.springframework.ui.Model, request: jakarta.servlet.http.HttpServletRequest) {
        // 기본값: 빈 리스트
        model.addAttribute("notifications", listOf<Any>())

        val user: Users = tokenExtractor.getUserFromToken(request) ?: return

        // tokenExtractor가 반환한 Users 객체에서 username 꺼내기
        val username: String = user.userName

        userRepository.findByUserName(username)
            .or(java.util.function.Supplier<java.util.Optional<out Users>> { userRepository.findByEmail(username) })
            .ifPresent(java.util.function.Consumer<Users> { u: Users ->
                val unread: List<com.example.ssauc.user.main.entity.Notification> =
                    notificationService.getUnreadNotifications(u.userId)
                model.addAttribute("notifications", unread)
            })
    }
}


