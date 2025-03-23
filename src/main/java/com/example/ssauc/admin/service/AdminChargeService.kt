package com.example.ssauc.admin.service

import com.example.ssauc.admin.repository.AdminChargeRepository
import com.example.ssauc.user.cash.entity.Charge
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class AdminChargeService {
    @Autowired
    private val adminChargeRepository: AdminChargeRepository? = null

    fun getCharges(page: Int, sortField: String, sortDir: String): Page<Charge?> {
        val sort = Sort.by(Sort.Direction.fromString(sortDir), sortField)
        return adminChargeRepository!!.findAll(PageRequest.of(page, 10, sort))
    }

    fun searchCharges(keyword: String?, page: Int, sortField: String, sortDir: String): Page<Charge?>? {
        val pageable: Pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.fromString(sortDir), sortField))
        return adminChargeRepository!!.findByUser_UserNameContainingIgnoreCaseOrChargeTypeContainingIgnoreCase(
            keyword,
            keyword,
            pageable
        )
    }

    fun findAllCharges(): List<Charge?> {
        return adminChargeRepository!!.findAll()
    }
}
