package com.example.ssauc.user.pay.repository;

import com.example.ssauc.user.pay.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 로그인한 사용자가 review의 reviewee인 경우 → 수신된 리뷰
    Page<Review> findByReviewee_UserId(Long userId, Pageable pageable);

    // 로그인한 사용자가 review의 reviewer인 경우 → 작성한 리뷰
    Page<Review> findByReviewer_UserId(Long userId, Pageable pageable);

    // 특정 유저의 최근 5개 리뷰를 가져오는 메소드
    List<Review> findTop5ByReviewee_UserIdOrderByCreatedAtDesc(Long userId);

    // 특정 유저의 최근 10개 리뷰를 가져오는 메소드 (10개 미만이면 그 개수만큼)
    List<Review> findTop10ByReviewee_UserIdOrderByCreatedAtDesc(Long userId);

    // 특정 유저의 리뷰 개수 조회
    Long countByReviewee_UserId(Long userId);
}