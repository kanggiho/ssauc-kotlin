package com.example.ssauc.admin.service

import com.example.ssauc.admin.repository.AdminProductReportRepository
import com.example.ssauc.user.bid.entity.ProductReport
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
class AdminProductReportService {
    @Autowired
    private val adminProductReportRepository: AdminProductReportRepository? = null

    @Autowired
    private val userRepository: UsersRepository? = null

    var eventPublisher: ApplicationEventPublisher? = null

    fun getReports(page: Int, sortField: String, sortDir: String): Page<ProductReport?> {
        val sort = Sort.by(Sort.Direction.fromString(sortDir), sortField)
        return adminProductReportRepository!!.findAll(PageRequest.of(page, 10, sort))
    }

    fun findProductReportById(reportId: Long): ProductReport? {
        return adminProductReportRepository!!.findById(reportId).orElse(null)
    }

    fun updateProductReportInfo(action: String, reportId: Long): Boolean {
        val productReport = adminProductReportRepository!!.findById(reportId).orElse(null) ?: return false

        var temp = 0

        if (action == "참작") {
            temp = 0
        } else if (action == "경고") {
            temp = 1
        } else if (action == "제명") {
            temp = 3
        }


        // productReport 업데이트
        val updateProductReport =
            adminProductReportRepository.updateProductReportByReportId("처리완료", LocalDateTime.now(), reportId)

        // reportedUser 업데이트
        val updateReportedUser = userRepository!!.updateUserByWarningCount(temp, productReport.reportedUser!!.userId)


        if (action == "경고") {
            eventPublisher!!.publishEvent(UserWarnedEvent(this, productReport.reportedUser.userId))
        }

        return updateProductReport == 1 && updateReportedUser == 1
    }

    fun findAllProductReports(): List<ProductReport?> {
        return adminProductReportRepository!!.findAll()
    }
}