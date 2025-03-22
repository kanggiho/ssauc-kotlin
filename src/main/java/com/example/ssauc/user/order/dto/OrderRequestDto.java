package com.example.ssauc.user.order.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderRequestDto {
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private int totalPayment;
    private String postalCode;
    private String deliveryAddress;
    private String selectedOption;
}
