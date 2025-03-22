package com.example.ssauc.user.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReportDto {
    private Long reportId;
    private String reportedUserName;
    private String profileImageUrl;
    private String reportReason;
    private LocalDateTime reportDate;
    private LocalDateTime processedAt;
    private String status;
}
