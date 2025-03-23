package com.example.ssauc.admin.service

import com.example.ssauc.admin.repository.AdminUserRepository
import com.example.ssauc.user.login.entity.Users
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class AdminUserService {
    @Autowired
    private val adminUsersRepository: AdminUserRepository? = null


    fun getUsers(page: Int, sortField: String, sortDir: String): Page<Users?> {
        val sort = Sort.by(Sort.Direction.fromString(sortDir), sortField)
        return adminUsersRepository!!.findAll(PageRequest.of(page, 10, sort))
    }

    fun searchUsersByName(keyword: String?, page: Int, sortField: String, sortDir: String): Page<Users?>? {
        val pageable: Pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.fromString(sortDir), sortField))
        return adminUsersRepository!!.findByUserNameContainingIgnoreCase(keyword, pageable)
    }


    fun findUsersById(userId: Long): Users? {
        return adminUsersRepository!!.findById(userId).orElse(null)
    }

    @Transactional
    fun changeUsersStatus(userId: Long, status: String) {
        val user = adminUsersRepository!!.findById(userId)
            .orElseThrow { EntityNotFoundException("상품을 찾을 수 없습니다.") }!!

        require(isValidStatus(status)) { "잘못된 상태 값입니다." }

        user.status = status
        if (status == "ACTIVE") {
            user.setWarningCount(0)
        }
        adminUsersRepository.save(user) // 상태 변경 후 저장
    }

    private fun isValidStatus(status: String): Boolean {
        return "ACTIVE" == status || "BLOCKED" == status
    }


    fun findAllUsers(): List<Users?> {
        return adminUsersRepository!!.findAll()
    }

    // 경고 횟수 3 이상인 유저 상태 BLOCKED로 변경
    @Transactional
    fun blockUsersWithHighWarningCount(): Int {
        val usersToBlock = adminUsersRepository!!.findByWarningCountGreaterThanEqual(3)

        for (user in usersToBlock!!) {
            user.status = "BLOCKED"
        }

        return usersToBlock.size
    }
}