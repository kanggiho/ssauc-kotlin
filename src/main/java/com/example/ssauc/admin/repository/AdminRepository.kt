package com.example.ssauc.admin.repository

import com.example.ssauc.admin.entity.Admin
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AdminRepository : JpaRepository<Admin?, Long?> {
    fun findByEmail(email: String?): Optional<Admin?>?

    fun findByEmailAndPassword(email: String?, password: String?): Optional<Admin?>?
}