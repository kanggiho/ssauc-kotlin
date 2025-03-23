package com.example.ssauc.user.pay.repository

import com.example.ssauc.user.pay.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment?, Long?>