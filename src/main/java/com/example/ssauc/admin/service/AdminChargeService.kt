package com.example.ssauc.admin.service;

import com.example.ssauc.admin.repository.AdminChargeRepository;
import com.example.ssauc.user.cash.entity.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminChargeService {

    @Autowired
    private AdminChargeRepository adminChargeRepository;

    public Page<Charge> getCharges(int page, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        return adminChargeRepository.findAll(PageRequest.of(page, 10, sort));
    }

    public Page<Charge> searchCharges(String keyword, int page, String sortField, String sortDir) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.fromString(sortDir), sortField));
        return adminChargeRepository.findByUser_UserNameContainingIgnoreCaseOrChargeTypeContainingIgnoreCase(keyword, keyword, pageable);
    }

    public List<Charge> findAllCharges() {
        return adminChargeRepository.findAll();
    }
}
