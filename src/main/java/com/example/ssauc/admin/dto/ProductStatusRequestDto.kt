package com.example.ssauc.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductStatusRequestDto {
    private Long productId;
    private String status;
}
