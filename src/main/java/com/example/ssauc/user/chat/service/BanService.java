package com.example.ssauc.user.chat.service;

import com.example.ssauc.user.chat.entity.Ban;
import com.example.ssauc.user.chat.repository.BanRepository;
import com.example.ssauc.user.login.entity.Users;


import com.example.ssauc.user.login.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class BanService {

    private final BanRepository banRepository;
    private final UsersRepository usersRepository;

//    //특정사용자를 차단
//    public Ban blockUser(Users user, Users blockedUser) {
//        // 이미 차단했는지 확인
//        if (banRepository.findByUserAndBlockedUser(user, blockedUser).isPresent()) {
//            throw new RuntimeException("이미 차단한 사용자입니다.");
//        }
//
//
//        Ban ban = new Ban(user, blockedUser, LocalDateTime.now());
//        return banRepository.save(ban);
//    }
//
//
//    /**
//     * 특정 사용자가 다른 사용자를 차단했는지 여부
//     * @param user 차단한 유저
//     * @param blockedUser 차단된 유저
//     * @return true/false
//     */
//    public boolean isBlocked(Users user, Users blockedUser) {
//        return banRepository.findByUserAndBlockedUser(user, blockedUser).isPresent();
//    }


    public void banUser(Long userId, Long blockedUserId) {
        // 차단하는 사용자와 차단 당하는 사용자 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("차단하는 사용자를 찾을 수 없습니다."));
        Users blockedUser = usersRepository.findById(blockedUserId)
                .orElseThrow(() -> new IllegalArgumentException("차단 당하는 사용자를 찾을 수 없습니다."));



        // 기존 Ban 레코드 전체를 조회 (status 무관)
        Optional<Ban> optionalBan = banRepository.findByUserAndBlockedUser(user, blockedUser);

        if (optionalBan.isPresent()) {
            Ban existingBan = optionalBan.get();
            if (existingBan.getStatus() == 1) {
                throw new IllegalStateException("이미 차단된 사용자입니다.");
            } else {
                // 비활성화 되어있다면 차단 상태(1)로 업데이트
                existingBan.setStatus(1);
                existingBan.setBlockedAt(LocalDateTime.now());
                banRepository.save(existingBan);
                return;
            }
        }

        // Ban 엔티티 생성
        Ban ban = Ban.builder()
                .user(user)
                .blockedUser(blockedUser)
                .blockedAt(LocalDateTime.now())
                .status(1)  // 1: 차단 상태 (활성화)
                .build();

        // ban 테이블에 저장
        banRepository.save(ban);
    }


    public void unbanUser(Long userId, Long blockedUserId) {
        // 차단하는 사용자와 차단 당하는 사용자 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Users blockedUser = usersRepository.findById(blockedUserId)
                .orElseThrow(() -> new IllegalArgumentException("차단 당하는 사용자를 찾을 수 없습니다."));

        // 활성 차단 상태의 Ban 엔티티가 있는지 확인
        Ban ban = banRepository.findByUserAndBlockedUserAndStatus(user, blockedUser, 1)
                .orElseThrow(() -> new IllegalStateException("차단을 당하여 해당 기능이 불가능합니다."));

        // 차단 해제: status를 0으로 업데이트하거나 삭제 처리
        ban.setStatus(0);
        banRepository.save(ban);
    }




}








