package com.example.ssauc.user.chat.service;

import com.example.ssauc.user.chat.dto.ReportRequestDto;
import com.example.ssauc.user.chat.entity.ChatRoom;
import com.example.ssauc.user.chat.entity.Report;
import com.example.ssauc.user.chat.repository.ChatRoomRepository;
import com.example.ssauc.user.chat.repository.ReportRepository;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.login.service.UserService;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final UsersRepository usersRepository;


    public boolean reportUser(ReportRequestDto reportRequestDto) {
        try {
            Report report = new Report();
            report.setReporter(usersRepository.findById(reportRequestDto.getReporterUserId()).orElse(null));
            report.setReportedUser(usersRepository.findById(reportRequestDto.getReportedUserId()).orElse(null));
            report.setReportReason(reportRequestDto.getReportReason());
            report.setDetails(reportRequestDto.getDetails());
            report.setStatus("처리대기");
            report.setReportDate(LocalDateTime.now());
            reportRepository.save(report);
            return true;
        } catch (Exception e) {
            return false;
        }
    }




    public ChatRoom getChatRoom(long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElse(null);
    }
    public Users getUsers(long userId) {
        return usersRepository.findById(userId).orElse(null);
    }

}
