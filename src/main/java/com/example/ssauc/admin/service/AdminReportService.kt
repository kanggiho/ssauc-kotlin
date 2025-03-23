package com.example.ssauc.admin.service

import com.example.ssauc.admin.repository.AdminReportRepository
import com.example.ssauc.user.chat.entity.Report
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.mypage.event.UserWarnedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AdminReportService {
    @Autowired
    private val adminReportRepository: AdminReportRepository? = null

    @Autowired
    private val userRepository: UsersRepository? = null

    var eventPublisher: ApplicationEventPublisher? = null

    fun getReports(page: Int, sortField: String, sortDir: String): Page<Report?> {
        val sort = Sort.by(Sort.Direction.fromString(sortDir), sortField)
        return adminReportRepository!!.findAll(PageRequest.of(page, 10, sort))
    }

    fun findReportById(reportId: Long): Report? {
        return adminReportRepository!!.findById(reportId).orElse(null)
    }

    fun updateReportInfo(action: String, reportId: Long): Boolean {
        val report = adminReportRepository!!.findById(reportId).orElse(null) ?: return false

        var temp = 0

        if (action == "참작") {
            temp = 0
        } else if (action == "경고") {
            temp = 1
        } else if (action == "제명") {
            temp = 3
        }


        // report 업데이트
        val updateReport = adminReportRepository.updateReportByReportId("처리완료", LocalDateTime.now(), reportId)

        // reportedUser 업데이트
        val updateReportedUser = userRepository!!.updateUserByWarningCount(temp, report.reportedUser.userId)


        if (action == "경고") {
            eventPublisher!!.publishEvent(UserWarnedEvent(this, report.reportedUser.userId))
        }

        return updateReport == 1 && updateReportedUser == 1
    }

    fun findAllReports(): List<Report?> {
        return adminReportRepository!!.findAll()
    }
}