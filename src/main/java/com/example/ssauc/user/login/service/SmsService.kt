package com.example.ssauc.user.login.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import org.springframework.stereotype.Service

@Service
class SmsService {
    fun verifyFirebaseToken(firebaseToken: String?, phone: String): Boolean {
        try {
            val token = FirebaseAuth.getInstance().verifyIdToken(firebaseToken)
            val verifiedPhone = token.claims["phone_number"] as String?
            return phone == verifiedPhone
        } catch (e: FirebaseAuthException) {
            e.printStackTrace()
            return false
        }
    }
}
