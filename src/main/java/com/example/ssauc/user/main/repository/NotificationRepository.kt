package com.example.ssauc.user.main.repository

import com.example.ssauc.user.main.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface NotificationRepository : JpaRepository<Notification?, Long?> {
    @Query(
        ("SELECT n FROM Notification n " +
                "WHERE n.user.userId = :userId " +
                "AND n.message = :message " +
                "AND n.type = :type " +
                "AND n.readStatus = :readStatus")
    )
    fun findNotification(
        @Param("userId") userId: Long?,
        @Param("message") message: String?,
        @Param("type") type: String?,
        @Param("readStatus") readStatus: Int
    ): Optional<Notification?>?

    fun findByUser_UserIdAndReadStatus(userId: Long?, i: Int): List<Notification?>?
}