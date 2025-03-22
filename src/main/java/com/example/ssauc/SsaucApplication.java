package com.example.ssauc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = "com.example.ssauc")
@EnableJpaRepositories(basePackages = "com.example.ssauc")
@EnableScheduling
public class SsaucApplication implements CommandLineRunner {

    @Autowired
    private Environment environment;
    // Environment 객체를 통해 설정값을 읽음

    public static void main(String[] args) {
        SpringApplication.run(SsaucApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 서버 포트 7777
        String port = environment.getProperty("server.port", "8080"); // 기본값 8080
        // 애플리케이션 시작 후 콘솔에 링크 출력
        System.out.println("========================================");
        System.out.println("Application is running at:");
        System.out.println("http://localhost:" + port);
        System.out.println("========================================");
    }
}
