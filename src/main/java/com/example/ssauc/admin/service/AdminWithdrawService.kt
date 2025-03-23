package com.example.ssauc.admin.service

import com.example.ssauc.admin.repository.AdminWithdrawRepository
import com.example.ssauc.user.cash.entity.Withdraw
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AdminWithdrawService {
    @Autowired
    private val adminWithdrawRepository: AdminWithdrawRepository? = null

    fun getWithdraws(page: Int, sortField: String, sortDir: String): Page<Withdraw?> {
        val sort = Sort.by(Sort.Direction.fromString(sortDir), sortField)
        val pageable: Pageable = PageRequest.of(page, 10, sort)
        return adminWithdrawRepository!!.findAll(pageable)
    }

    fun searchWithdraws(keyword: String?, page: Int, sortField: String, sortDir: String): Page<Withdraw?>? {
        val pageable: Pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.fromString(sortDir), sortField))
        return adminWithdrawRepository!!.findByUser_UserNameContainingIgnoreCaseOrBankContainingIgnoreCaseOrAccountContainingIgnoreCase(
            keyword,
            keyword,
            keyword,
            pageable
        )
    }

    fun findAllWithdraws(): List<Withdraw?> {
        return adminWithdrawRepository!!.findAll()
    }

    fun processWithdraw(withdrawId: Long) {
        val withdraw = adminWithdrawRepository!!.findById(withdrawId)
            .orElseThrow { RuntimeException("Withdraw record not found") }!!
        withdraw.withdrawAt = LocalDateTime.now()
        adminWithdrawRepository.save(withdraw)
    }
}
