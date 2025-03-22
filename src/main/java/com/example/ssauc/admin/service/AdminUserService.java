package com.example.ssauc.admin.service;

import com.example.ssauc.admin.repository.AdminUserRepository;
import com.example.ssauc.user.login.entity.Users;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {

    @Autowired
    private AdminUserRepository adminUsersRepository;


    public Page<Users> getUsers(int page, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        return adminUsersRepository.findAll(PageRequest.of(page, 10, sort));
    }

    public Page<Users> searchUsersByName(String keyword, int page, String sortField, String sortDir) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.fromString(sortDir), sortField));
        return adminUsersRepository.findByUserNameContainingIgnoreCase(keyword, pageable);
    }


    public Users findUsersById(Long userId) {
        return adminUsersRepository.findById(userId).orElse(null);
    }

    @Transactional
    public void changeUsersStatus(Long userId, String status) {
        Users user = adminUsersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("잘못된 상태 값입니다.");
        }

        user.setStatus(status);
        if (status.equals("ACTIVE")){
            user.setWarningCount(0);
        }
        adminUsersRepository.save(user);  // 상태 변경 후 저장
    }

    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || "BLOCKED".equals(status);
    }


    public List<Users> findAllUsers() {
        return adminUsersRepository.findAll();
    }

    // 경고 횟수 3 이상인 유저 상태 BLOCKED로 변경
    @Transactional
    public int blockUsersWithHighWarningCount() {
        List<Users> usersToBlock = adminUsersRepository.findByWarningCountGreaterThanEqual(3);

        for (Users user : usersToBlock) {
            user.setStatus("BLOCKED");
        }

        return usersToBlock.size();
    }

}