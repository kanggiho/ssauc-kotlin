package com.example.ssauc.user.mypage.scheduler;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.mypage.entity.ReputationHistory;
import com.example.ssauc.user.mypage.repository.ReputationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InactivityPenaltyScheduler {
    // 장기 미접속 유저 reputation 감소

    private final UsersRepository usersRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;

    // 매월 1일 00:00에 실행하도록 스케줄러 설정
    @Scheduled(cron = "0 0 9 1 * ?")
    public void applyInactivityPenalty() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeMonthsAgo = now.minusMonths(3);

        // lastLogin이 3개월 이전인 사용자들 조회
        List<Users> inactiveUsers = usersRepository.findByLastLoginBefore(threeMonthsAgo);

        for (Users user : inactiveUsers) {
            // 미접속 개월 수 계산 (예: 5개월 미접속이면, penaltyMonths = 5 - 3 = 2)
            long monthsInactive = ChronoUnit.MONTHS.between(user.getLastLogin(), now);
            long penaltyMonths = Math.max(monthsInactive - 3, 0);

            // 매월 -3%씩, 최대 -36%까지
            double penaltyRate = Math.min(penaltyMonths * 0.03, 0.36);
            double penaltyAmount = user.getReputation() * penaltyRate;
            double newReputation = user.getReputation() - penaltyAmount;

            // 사용자 평판 업데이트
            user.setReputation(newReputation);
            usersRepository.save(user);

            // 평판 변경 이력 기록
            ReputationHistory history = ReputationHistory.builder()
                    .user(user)
                    .changeType("3개월 이상 미접속 패널티")
                    .changeAmount(-penaltyAmount)
                    .newScore(newReputation)
                    .createdAt(now)
                    .build();
            reputationHistoryRepository.save(history);
        }
    }
}
