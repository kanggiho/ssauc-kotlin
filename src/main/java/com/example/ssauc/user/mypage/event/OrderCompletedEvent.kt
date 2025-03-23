package com.example.ssauc.user.mypage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

@Getter
public class OrderCompletedEvent extends ApplicationEvent {
    // 거래(판매 및 구매) 완료 이벤트: reputation +1.0
    // 한 달에 거래 3회 이상 이벤트: reputation +3.0

    private final Long buyerId;
    private final Long sellerId;
    private final LocalDateTime completedAt;

    public OrderCompletedEvent(Object source, Long buyerId, Long sellerId, LocalDateTime completedAt) {
        super(source);
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.completedAt = completedAt;
    }
}
