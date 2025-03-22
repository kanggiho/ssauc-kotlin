package com.example.ssauc.user.main.repository;

import com.example.ssauc.user.main.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n " +
            "WHERE n.user.userId = :userId " +
            "AND n.message = :message " +
            "AND n.type = :type " +
            "AND n.readStatus = :readStatus")
    Optional<Notification> findNotification(@Param("userId") Long userId,
                                            @Param("message") String message,
                                            @Param("type") String type,
                                            @Param("readStatus") int readStatus);

    List<Notification> findByUser_UserIdAndReadStatus(Long userId, int i);
}