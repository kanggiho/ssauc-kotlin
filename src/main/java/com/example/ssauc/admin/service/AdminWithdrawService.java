package com.example.ssauc.admin.service;

import com.example.ssauc.admin.repository.AdminWithdrawRepository;
import com.example.ssauc.user.cash.entity.Withdraw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminWithdrawService {

    @Autowired
    private AdminWithdrawRepository adminWithdrawRepository;

    public Page<Withdraw> getWithdraws(int page, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, 10, sort);
        return adminWithdrawRepository.findAll(pageable);
    }

    public Page<Withdraw> searchWithdraws(String keyword, int page, String sortField, String sortDir) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.fromString(sortDir), sortField));
        return adminWithdrawRepository.findByUser_UserNameContainingIgnoreCaseOrBankContainingIgnoreCaseOrAccountContainingIgnoreCase(keyword, keyword, keyword, pageable);
    }

    public List<Withdraw> findAllWithdraws() {
        return adminWithdrawRepository.findAll();
    }

    public void processWithdraw(Long withdrawId) {
        Withdraw withdraw = adminWithdrawRepository.findById(withdrawId)
                .orElseThrow(() -> new RuntimeException("Withdraw record not found"));
        withdraw.setWithdrawAt(LocalDateTime.now());
        adminWithdrawRepository.save(withdraw);
    }
}
