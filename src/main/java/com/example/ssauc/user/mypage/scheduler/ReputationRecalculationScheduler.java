package com.example.ssauc.user.mypage.scheduler;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.mypage.entity.ReputationHistory;
import com.example.ssauc.user.mypage.repository.ReputationHistoryRepository;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class ReputationRecalculationScheduler {

    private final UsersRepository usersRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;

    // 하루 1회
    @Scheduled(cron = "0 0 4 * * ?")
    public void recalculateReputations() {
        LocalDateTime now = LocalDateTime.now();

        // 모든 사용자 조회
        List<Users> users = usersRepository.findAll();

        for (Users user : users) {
            // 원본 평판 기록이 없다면 스킵
            if (!reputationHistoryRepository.existsByUser(user)) {
                continue;
            }

            // "가중치 적용" 패턴이 포함되지 않은 평판 변화 내역 조회
            List<ReputationHistory> histories =
                    reputationHistoryRepository.findByUserAndChangeTypeNotLike(user, "%가중치 적용%");

            for (ReputationHistory history : histories) {
                long days = ChronoUnit.DAYS.between(history.getCreatedAt(), now);

                // 3개월, 6개월, 12개월 조건에 맞춰 "단계별 추가 차감" 적용
                if (days >= 90 && days < 91) {
                    applyPartialReduction(user, history, new BigDecimal("0.1"), "3개월 가중치 적용", now);
                } else if (days >= 180 && days < 181) {
                    applyPartialReduction(user, history, new BigDecimal("0.2"), "6개월 가중치 적용", now);
                } else if (days >= 365 && days < 366) {
                    applyPartialReduction(user, history, new BigDecimal("0.2"), "12개월 가중치 적용", now);
                }
            }
        }
    }

    // 기간 별 차감 정도 계산
    // @param fraction: 원본 점수 대비 차감 비율 (예: 0.1, 0.2)
    private void applyPartialReduction(Users user, ReputationHistory history,
                                       BigDecimal fraction, String changeType, LocalDateTime now) {
        // 원본 점수를 BigDecimal로 변환 후 소숫점 1자리 반올림
        BigDecimal original = BigDecimal.valueOf(history.getChangeAmount())
                .setScale(1, RoundingMode.HALF_UP);

        // 차감할 양: 원본 * fraction, 소숫점 1자리 반올림
        BigDecimal reductionBD = original.multiply(fraction)
                .setScale(1, RoundingMode.HALF_UP);

        // 현재 평판을 BigDecimal로 변환 후 소숫점 1자리 반올림
        BigDecimal currentReputation = BigDecimal.valueOf(user.getReputation())
                .setScale(1, RoundingMode.HALF_UP);

        // 새 평판 = 현재 평판 - 차감량, 음수면 0으로 처리하고 반올림
        BigDecimal newReputation = currentReputation.subtract(reductionBD)
                .max(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);

        // 사용자 평판 업데이트 (DB 필드가 double인 경우 doubleValue() 사용)
        user.setReputation(newReputation.doubleValue());
        usersRepository.save(user);


        // 적용된 가중치 대상 history_id를 changeType 앞에 추가
        String updatedChangeType = history.getHistoryId() + " - " + changeType;

        // 감소량이 있을 경우 기록
        if (reductionBD.compareTo(BigDecimal.ZERO) > 0) {
            ReputationHistory partialRecord = ReputationHistory.builder()
                    .user(user)
                    .changeType(updatedChangeType) // 원본과 구분하기 위해 고정
                    .changeAmount(-reductionBD.doubleValue())
                    .newScore(newReputation.doubleValue())
                    .createdAt(now)
                    .build();
            reputationHistoryRepository.save(partialRecord);
        }
    }
}
