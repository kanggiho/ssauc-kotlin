package com.example.ssauc.user.chat.controller

import com.example.ssauc.user.chat.dto.ReportRequestDto
import com.example.ssauc.user.chat.service.ReportService
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.login.util.TokenExtractor
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("chat/report")
@RequiredArgsConstructor
class ReportController {
    private val reportService: ReportService? = null
    private val usersRepository: UsersRepository? = null
    private val tokenExtractor: TokenExtractor? = null


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
    fun report(
        @RequestParam("roomId") roomId: Long,
        @RequestParam("reported") reportedUserId: Long,
        model: Model
    ): String {
        val chatRoom = reportService!!.getChatRoom(roomId)
        val product = chatRoom.product
        val user = reportService.getUsers(reportedUserId)

        model.addAttribute("chatRoom", chatRoom)
        model.addAttribute("product", product)
        model.addAttribute("reportedUser", user)

        return "chat/report"
    }

    @ResponseBody
    @PostMapping("/result")
    fun submitReport(@RequestBody reportRequestDto: ReportRequestDto, request: HttpServletRequest): ResponseEntity<*> {
        reportRequestDto.reporterUserId = tokenExtractor!!.getUserFromToken(request).userId
        val isReported = reportService!!.reportUser(reportRequestDto)

        return if (isReported) {
            ResponseEntity.ok().body("신고가 성공적으로 접수되었습니다.")
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("신고 접수에 실패했습니다.")
        }
    }
}
