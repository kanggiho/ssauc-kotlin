package com.example.ssauc.util;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component("dateUtils") // Thymeleaf에서 #dateUtils로 호출 가능
public class DateUtils {

    public String formatRemainingTime(LocalDateTime endAt) {
        // 현재 시간과 종료 시간의 차이를 계산 (종료 시간이 미래라고 가정)
        Duration duration = Duration.between(LocalDateTime.now(), endAt);
        if(duration.isNegative()) {
            return "종료됨";
        }
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        return days + "일 " + hours + "시간";
    }
}
