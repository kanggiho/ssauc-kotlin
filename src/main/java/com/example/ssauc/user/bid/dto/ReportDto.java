package com.example.ssauc.user.bid.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long productId;
    private Long reporterId;
    private Long reportedUserId;
    private String reportReason;
    private String details;



}
