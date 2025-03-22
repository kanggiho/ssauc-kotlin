package com.example.ssauc.user.mypage.service;

import com.example.ssauc.common.service.CommonUserService;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.mypage.dto.*;
import com.example.ssauc.user.mypage.event.ReviewSubmittedEvent;
import com.example.ssauc.user.mypage.repository.ReputationHistoryRepository;
import com.example.ssauc.user.order.entity.Orders;
import com.example.ssauc.user.order.repository.OrdersRepository;
import com.example.ssauc.user.pay.entity.Review;
import com.example.ssauc.user.pay.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

    private final CommonUserService commonUserService;
    private final ReviewRepository reviewRepository;
    private final OrdersRepository ordersRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UsersRepository usersRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;

    // JWT 기반 DB에서 최신 사용자 정보를 조회
    @Override
    public Users getCurrentUser(String email) {
        return commonUserService.getCurrentUser(email);
    }

    // ===================== 회원 정보 =====================
    @Override
    public Users getUserInfo(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // userName으로 사용자 정보를 조회하여 DTO로 변환
    @Override
    public ResponseUserInfoDto getUserInfoJson(String userName) {
        Users user = usersRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

        String formattedCreatedAt = user.getCreatedAt().format(formatter);
        String formattedLastLogin = user.getLastLogin().format(formatter2);

        return ResponseUserInfoDto.builder()
                .userName(user.getUserName())
                .profileImage(user.getProfileImage())
                .reputation(user.getReputation())
                .location(user.getLocation())
                .createdAt(formattedCreatedAt)
                .lastLogin(formattedLastLogin)
                .reviewSummary(user.getReviewSummary())
                .build();
    }

    @Override
    public List<ReputationGraphDto> getReputationHistory(Users user) {
        // 각 유저의 전체 평판 기록을 조회
        return reputationHistoryRepository.findByUser(user)
                .stream()
                .map(rh -> ReputationGraphDto.builder()
                        .createdAt(rh.getCreatedAt())
                        .newScore(rh.getNewScore())
                        .build())
                .sorted(Comparator.comparing(ReputationGraphDto::getCreatedAt)) // 그래프 그릴 때 시간 순서대로 점 찍기 위해 필요
                .collect(Collectors.toList());
    }

    // ===================== 리뷰 관리 =====================
    // 받은 리뷰 목록 조회
    @Override
    public Page<EvaluationReviewDto> getReceivedReviews(Users user, Pageable pageable) {
        return reviewRepository.findByReviewee_UserId(user.getUserId(), pageable)
                .map(review -> convertToDto(review, user.getUserId()));
    }

    // 작성한 리뷰 목록 조회
    @Override
    public Page<EvaluationReviewDto> getWrittenReviews(Users user, Pageable pageable) {
        return reviewRepository.findByReviewer_UserId(user.getUserId(), pageable)
                .map(review -> convertToDto(review, user.getUserId()));
    }

    // DTO 변환 메서드 (코드 중복 제거)
    private EvaluationReviewDto convertToDto(Review review, Long currentUserId) {
        return EvaluationReviewDto.builder()
                .reviewId(review.getReviewId())
                .orderId(review.getOrder().getOrderId())
                .reviewer(review.getReviewer().getUserName())
                .reviewee(review.getReviewee().getUserName())
                .profileImageUrl1(review.getReviewer().getProfileImage())
                .profileImageUrl2(review.getReviewee().getProfileImage())
                .productId(review.getOrder().getProduct().getProductId())
                .productName(review.getOrder().getProduct().getName())
                .productImageUrl(review.getOrder().getProduct().getImageUrl().split(",")[0])
                .createdAt(review.getCreatedAt())
                .transactionType(getTransactionType(review, currentUserId))
                .build();
    }

    // 거래 타입 (판매 / 구매) 구분
    private String getTransactionType(Review review, Long currentUserId) {
        Users buyer = review.getOrder().getBuyer();
        return buyer.getUserId().equals(currentUserId) ? "구매" : "판매";
    }

    // 아직 리뷰를 작성하지 않은 주문 목록 조회
    @Override
    public Page<EvaluationPendingDto> getPendingReviews(Users user, Pageable pageable) {
        // 로그인한 사용자가 아직 리뷰를 작성하지 않은 주문 조회
        Page<Orders> orders = ordersRepository.findPendingReviewOrders(user.getUserId(), pageable);
        return orders.map(order -> {
            String reviewTarget = "";
            String profileImage = "";
            String transactionType = "";

            // 주문에서 본인이 buyer면 대상은 seller, 본인이 seller면 대상은 buyer
            if (order.getBuyer().getUserId().equals(user.getUserId())) {
                reviewTarget = order.getSeller().getUserName();
                profileImage = order.getSeller().getProfileImage();
                transactionType = "구매";
            } else if (order.getSeller().getUserId().equals(user.getUserId())) {
                reviewTarget = order.getBuyer().getUserName();
                profileImage = order.getBuyer().getProfileImage();
                transactionType = "판매";
            }

            String productImageUrl = order.getProduct().getImageUrl();
            String mainImage = productImageUrl != null ? productImageUrl.split(",")[0] : null;
            return EvaluationPendingDto.builder()
                    .orderId(order.getOrderId())
                    .reviewTarget(reviewTarget)
                    .productId(order.getProduct().getProductId())
                    .productName(order.getProduct().getName())
                    .profileImageUrl(profileImage)
                    .productImageUrl(mainImage)
                    .orderDate(order.getOrderDate())
                    .transactionType(transactionType)
                    .build();
        });
    }

    // 리뷰 제출 처리(baseScore를 서버에서 재계산)
    @Override
    public void submitEvaluation(EvaluationDto evaluationDto, Users currentUser) {
        Orders order = ordersRepository.findById(evaluationDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("해당 주문 정보가 없습니다."));

        Users reviewee = order.getBuyer().getUserId().equals(currentUser.getUserId()) ?
                order.getSeller() : order.getBuyer();

        // 옵션 값 변환: "positive"이면 true, "negative"이면 false
        Boolean option1 = "positive".equalsIgnoreCase(evaluationDto.getQ1());
        Boolean option2 = "positive".equalsIgnoreCase(evaluationDto.getQ2());
        Boolean option3 = "positive".equalsIgnoreCase(evaluationDto.getQ3());

        // 기본 0.0점에 각 옵션에 대해 positive는 +0.5, negative는 -0.5
        double baseScore = (option1 ? 0.5 : -0.5)
                + (option2 ? 0.5 : -0.5)
                + (option3 ? 0.5 : -0.5);

        // 상세 후기는 300자 제한 (필요 시 자르기)
        String comment = evaluationDto.getReviewContent();
        if (comment != null && comment.length() > 300) {
            comment = comment.substring(0, 300);
        }

        Review review = Review.builder()
                .reviewer(currentUser)
                .reviewee(reviewee)
                .order(order)
                .option1(option1)
                .option2(option2)
                .option3(option3)
                .baseScore(baseScore)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);

        // 리뷰 제출 이벤트 발행
        eventPublisher.publishEvent(
                new ReviewSubmittedEvent(this, currentUser.getUserId(), reviewee.getUserId(), baseScore, review.getCreatedAt())
        );


    }

    // 리뷰 페이지에 필요한 정보 조회
    @Override
    public EvaluationDto getEvaluationData(Long orderId, Users currentUser) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문 정보를 찾을 수 없습니다."));

        String transactionType;
        String otherUserName;
        // 현재 사용자가 buyer이면 거래는 '구매'이며, 리뷰 대상은 seller
        if (order.getBuyer().getUserId().equals(currentUser.getUserId())) {
            transactionType = "구매";
            otherUserName = order.getSeller().getUserName();
        }
        // 현재 사용자가 seller이면 거래는 '판매'이며, 리뷰 대상은 buyer
        else if (order.getSeller().getUserId().equals(currentUser.getUserId())) {
            transactionType = "판매";
            otherUserName = order.getBuyer().getUserName();
        } else {
            throw new RuntimeException("리뷰 작성 대상 정보가 올바르지 않습니다.");
        }

        // EvaluationDto에 주문/상품 식별자와 거래 유형을 세팅하고, 화면 표시용 상품명과 상대방 이름도 추가
        EvaluationDto evaluationDto = EvaluationDto.builder()
                .orderId(order.getOrderId())
                .productId(order.getProduct().getProductId())
                .transactionType(transactionType)
                .build();
        evaluationDto.setProductName(order.getProduct().getName());
        evaluationDto.setOtherUserName(otherUserName);
        return evaluationDto;
    }

    // 리뷰 상세 정보 조회
    @Override
    public EvaluatedDto getReviewById(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("해당 리뷰를 찾을 수 없습니다."));

        return EvaluatedDto.builder()
                .reviewId(review.getReviewId())
                .orderId(review.getOrder().getOrderId())
                .reviewerName(review.getReviewer().getUserName())
                .revieweeName(review.getReviewee().getUserName())
                .profileImageUrl(review.getReviewee().getProfileImage())
                .productId(review.getOrder().getProduct().getProductId())
                .productName(review.getOrder().getProduct().getName())
                .productImageUrl(review.getOrder().getProduct().getImageUrl().split(",")[0])
                .createdAt(review.getCreatedAt())
                .transactionType(getTransactionType(review, currentUserId))
                .option1(review.getOption1())
                .option2(review.getOption2())
                .option3(review.getOption3())
                .comment(review.getComment())
                .build();
    }

}



