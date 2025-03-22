package com.example.ssauc.admin.service;

import com.example.ssauc.admin.repository.AdminReportRepository;
import com.example.ssauc.user.chat.entity.Report;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.mypage.event.UserWarnedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminReportService {

    @Autowired
    private AdminReportRepository adminReportRepository;

    @Autowired
    private UsersRepository userRepository;

    ApplicationEventPublisher eventPublisher;

    public Page<Report> getReports(int page, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        return adminReportRepository.findAll(PageRequest.of(page, 10, sort));
    }

    public Report findReportById(Long reportId) {
        return adminReportRepository.findById(reportId).orElse(null);
    }

    public boolean updateReportInfo(String action, long reportId) {

        Report report = adminReportRepository.findById(reportId).orElse(null);

        if(report == null) return false;

        int temp = 0;

        if(action.equals("참작")){
            temp = 0;
        } else if (action.equals("경고")) {
            temp= 1;
        } else if (action.equals("제명")) {
            temp = 3;
        }


        // report 업데이트
        int updateReport = adminReportRepository.updateReportByReportId("처리완료", LocalDateTime.now() ,reportId);

        // reportedUser 업데이트
        int updateReportedUser = userRepository.updateUserByWarningCount(temp,report.getReportedUser().getUserId());


        if(action.equals("경고")){
            eventPublisher.publishEvent(new UserWarnedEvent(this, report.getReportedUser().getUserId()));
        }

        return updateReport == 1 && updateReportedUser == 1;

    }

    public List<Report> findAllReports() {
        return adminReportRepository.findAll();
    }
}