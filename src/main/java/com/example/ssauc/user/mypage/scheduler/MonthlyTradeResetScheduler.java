package com.example.ssauc.user.mypage.scheduler;

import com.example.ssauc.user.mypage.repository.UserActivityRepository;
import com.example.ssauc.user.mypage.entity.UserActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyTradeResetScheduler {
    // 월별 거래 횟수 초기화

    private final UserActivityRepository userActivityRepository;

    // 매월 1일 00:00에 모든 유저의 monthly_trade_count를 초기화
    @Scheduled(cron = "0 0 8 1 * ?")
    public void resetMonthlyTradeCounts() {
        List<UserActivity> activities = userActivityRepository.findAll();
        for (UserActivity activity : activities) {
            activity.setMonthlyTradeCount(0L);
            activity.setLastUpdated(LocalDateTime.now());
        }
        userActivityRepository.saveAll(activities);
    }
}
