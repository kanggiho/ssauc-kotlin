package com.example.ssauc.user.mypage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

@Getter
public class OrderShippedEvent extends ApplicationEvent {
    // 운송장 정보 등록 이벤트(주문 후 24시간 이내): reputation +1.0

    private final Long sellerId;
    private final LocalDateTime orderDate;
    private final LocalDateTime shippedDate;

    public OrderShippedEvent(Object source, Long sellerId, LocalDateTime orderDate, LocalDateTime shippedDate) {
        super(source);
        this.sellerId = sellerId;
        this.orderDate = orderDate;
        this.shippedDate = shippedDate;
    }
}
