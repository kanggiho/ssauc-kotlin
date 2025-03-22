package com.example.ssauc.user.chat.controller;

import com.example.ssauc.user.chat.dto.ReportRequestDto;
import com.example.ssauc.user.chat.entity.ChatRoom;
import com.example.ssauc.user.chat.entity.Report;
import com.example.ssauc.user.chat.service.ReportService;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.product.entity.Product;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("chat/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UsersRepository usersRepository;
    private final TokenExtractor tokenExtractor;


//    @PostMapping("/submit-report")
//    public String submitReport(@ModelAttribute ReportRequestDto dto) {
//
//        // 1) DB에서 신고자 / 피신고자 Users 엔티티 조회
//        Users reporterUser = usersRepository.findById(dto.getReporterId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid reporter ID: " + dto.getReporterId()));
//        Users reportedUser = usersRepository.findById(dto.getReportedUserId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid reportedUser ID: " + dto.getReportedUserId()));
//
//        // 2) Report 엔티티 생성
//        Report report = new Report();
//        report.setReporter(reporterUser);       // ManyToOne 필드
//        report.setReportedUser(reportedUser);   // ManyToOne 필드
//        report.setReportReason(dto.getReason());
//        report.setDetails(dto.getDetails());
//
//        // (필요 시) 상태, 신고일자 등 기본값 설정
//        report.setStatus("PENDING");
//        report.setReportDate(LocalDateTime.now());
//
//        // 신고 등록(Service호출)
//        reportService.createReport(report);
//
//        // 저장 후 페이지 이동(리다이렉트)
//        return "redirect:/history/report?filter=user";
//    }

    @GetMapping
    public String report(@RequestParam("roomId") long roomId,
                         @RequestParam("reported") long reportedUserId,
                         Model model) {

        ChatRoom chatRoom = reportService.getChatRoom(roomId);
        Product product = chatRoom.getProduct();
        Users user = reportService.getUsers(reportedUserId);

        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("product", product);
        model.addAttribute("reportedUser", user);

        return "chat/report";
    }

    @ResponseBody
    @PostMapping("/result")
    public ResponseEntity<?> submitReport(@RequestBody ReportRequestDto reportRequestDto, HttpServletRequest request) {


        reportRequestDto.setReporterUserId(tokenExtractor.getUserFromToken(request).getUserId());
        boolean isReported = reportService.reportUser(reportRequestDto);

        if (isReported) {
            return ResponseEntity.ok().body("신고가 성공적으로 접수되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("신고 접수에 실패했습니다.");
        }
    }
















}
