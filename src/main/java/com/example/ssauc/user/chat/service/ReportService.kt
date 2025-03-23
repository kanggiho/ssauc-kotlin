package com.example.ssauc.user.chat.service

import com.example.ssauc.user.chat.dto.ReportRequestDto
import com.example.ssauc.user.chat.entity.ChatRoom
import com.example.ssauc.user.chat.entity.Report
import com.example.ssauc.user.chat.repository.ChatRoomRepository
import com.example.ssauc.user.chat.repository.ReportRepository
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
@RequiredArgsConstructor
class ReportService {
    private val reportRepository: ReportRepository? = null

    private val chatRoomRepository: ChatRoomRepository? = null

    private val usersRepository: UsersRepository? = null


    fun reportUser(reportRequestDto: ReportRequestDto): Boolean {
        try {
            val report = Report()
            report.setReporter(usersRepository!!.findById(reportRequestDto.getReporterUserId()).orElse(null))
            report.setReportedUser(usersRepository!!.findById(reportRequestDto.getReportedUserId()).orElse(null))
            report.setReportReason(reportRequestDto.getReportReason())
            report.setDetails(reportRequestDto.getDetails())
            report.setStatus("처리대기")
            report.setReportDate(LocalDateTime.now())
            reportRepository!!.save(report)
            return true
        } catch (e: Exception) {
            return false
        }
    }


    fun getChatRoom(chatRoomId: Long): ChatRoom? {
        return chatRoomRepository!!.findById(chatRoomId).orElse(null)
    }

    fun getUsers(userId: Long): Users? {
        return usersRepository!!.findById(userId).orElse(null)
    }
}
