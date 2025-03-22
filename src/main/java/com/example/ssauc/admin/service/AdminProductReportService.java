package com.example.ssauc.admin.service;

import com.example.ssauc.admin.repository.AdminProductReportRepository;
import com.example.ssauc.user.bid.entity.ProductReport;
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
public class AdminProductReportService {

    @Autowired
    private AdminProductReportRepository adminProductReportRepository;

    @Autowired
    private UsersRepository userRepository;

    ApplicationEventPublisher eventPublisher;

    public Page<ProductReport> getReports(int page, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        return adminProductReportRepository.findAll(PageRequest.of(page, 10, sort));
    }

    public ProductReport findProductReportById(Long reportId) {
        return adminProductReportRepository.findById(reportId).orElse(null);
    }

    public boolean updateProductReportInfo(String action, long reportId) {

        ProductReport productReport = adminProductReportRepository.findById(reportId).orElse(null);

        if(productReport == null) return false;

        int temp = 0;

        if(action.equals("참작")){
            temp = 0;
        } else if (action.equals("경고")) {
            temp= 1;
        } else if (action.equals("제명")) {
            temp = 3;
        }


        // productReport 업데이트
        int updateProductReport = adminProductReportRepository.updateProductReportByReportId("처리완료", LocalDateTime.now() ,reportId);

        // reportedUser 업데이트
        int updateReportedUser = userRepository.updateUserByWarningCount(temp,productReport.getReportedUser().getUserId());


        if(action.equals("경고")){
            eventPublisher.publishEvent(new UserWarnedEvent(this, productReport.getReportedUser().getUserId()));
        }

        return updateProductReport == 1 && updateReportedUser == 1;

    }

    public List<ProductReport> findAllProductReports() {
        return adminProductReportRepository.findAll();
    }
}