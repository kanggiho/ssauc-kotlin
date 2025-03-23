package com.example.ssauc.user.login.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class FirebaseConfig {
    @Value("\${firebase.credentials.path}")
    private val firebaseCredentialsPath: String? = null // 예: "classpath:firebase-service-account.json"

    @PostConstruct
    fun init() {
        try {
//             "classpath:" 접두어 제거 후, 앞에 "/"를 붙여 클래스패스 루트 기준으로 찾음
            var resourcePath = if (firebaseCredentialsPath!!.startsWith("classpath:"))
                firebaseCredentialsPath.substring("classpath:".length)
            else
                firebaseCredentialsPath
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/$resourcePath"
            }

            //            InputStream serviceAccount = new ClassPathResource(firebaseCredentialsPath.replace("classpath:", "")).getInputStream();
            val serviceAccount = javaClass.getResourceAsStream(resourcePath)
            requireNotNull(serviceAccount) { "Firebase service account file not found: $firebaseCredentialsPath" }

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }
        } catch (e: Exception) {
            throw RuntimeException("Error initializing Firebase", e)
        }
    }
}
