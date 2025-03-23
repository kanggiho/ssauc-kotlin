package com.example.ssauc.user.mypage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

@Getter
public class ReviewSubmittedEvent extends ApplicationEvent {
    // 리뷰 작성자: reputation +0.5
    // 리뷰 대상자: reputation +baseScore(-1.5 ~ 1.5)

    private final Long reviewerId;
    private final Long revieweeId;
    private final double baseScore;
    private final LocalDateTime createdAt;

    public ReviewSubmittedEvent(Object source, Long reviewerId, Long revieweeId, double baseScore, LocalDateTime createdAt) {
        super(source);
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.baseScore = baseScore;
        this.createdAt = createdAt;
    }
}
