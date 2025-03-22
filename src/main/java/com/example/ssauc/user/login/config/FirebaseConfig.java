package com.example.ssauc.user.login.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path}")
    private String firebaseCredentialsPath;  // 예: "classpath:firebase-service-account.json"

    @PostConstruct
    public void init() {
        try {
//             "classpath:" 접두어 제거 후, 앞에 "/"를 붙여 클래스패스 루트 기준으로 찾음
            String resourcePath = firebaseCredentialsPath.startsWith("classpath:")
                    ? firebaseCredentialsPath.substring("classpath:".length())
                    : firebaseCredentialsPath;
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }

//            InputStream serviceAccount = new ClassPathResource(firebaseCredentialsPath.replace("classpath:", "")).getInputStream();

            InputStream serviceAccount = getClass().getResourceAsStream(resourcePath);
            if (serviceAccount == null) {
                throw new IllegalArgumentException("Firebase service account file not found: " + firebaseCredentialsPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing Firebase", e);
        }
    }
}
