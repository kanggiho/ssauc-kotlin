package com.example.ssauc.user.login.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import org.springframework.stereotype.Service

@Service
class FirebaseService {
    @Throws(FirebaseAuthException::class)
    fun verifyIdToken(idToken: String?): FirebaseToken {
        return FirebaseAuth.getInstance().verifyIdToken(idToken)
    }
}
