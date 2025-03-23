package com.example.ssauc.user.login.entity;

import com.example.ssauc.user.bid.entity.AutoBid;
import com.example.ssauc.user.bid.entity.Bid;
import com.example.ssauc.user.chat.entity.Ban;
import com.example.ssauc.user.chat.entity.Report;
import com.example.ssauc.user.contact.entity.Board;
import com.example.ssauc.user.main.entity.Notification;
import com.example.ssauc.user.main.entity.ProductLike;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.cash.entity.Charge;
import com.example.ssauc.user.mypage.entity.ReputationHistory;
import com.example.ssauc.user.mypage.entity.UserActivity;
import com.example.ssauc.user.cash.entity.Withdraw;
import com.example.ssauc.user.order.entity.Orders;
import com.example.ssauc.user.pay.entity.Payment;
import com.example.ssauc.user.pay.entity.Review;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String userName;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 15, unique = true)
    private String phone;

    // profileImage: 기본값 적용 (DDL 기본값과 @PrePersist로 세팅)
    @Column(length = 500, columnDefinition = "varchar(500) default 'https://ssg-be-s3-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png'")
    private String profileImage;

    @Column(length = 300)
    private String location; // 지역 정보

    // status: 기본값 active
    @Column(columnDefinition = "varchar(50) default 'active'")
    private String status;

    // reputation: 기본값 50.0
    @Column(columnDefinition = "double default 50.0")
    private Double reputation;

    private int warningCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    // cash: 기본값 0
    @Column(columnDefinition = "bigint default 0")
    private Long cash;

    // review.comment 요약 (초기값 null)
    @Column(name = "review_summary", columnDefinition = "TEXT")
    private String reviewSummary;

    // 생성자 (username, password 만 받는 생성자)
    public Users(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    // 신규 생성 시 기본값 설정 (엔티티가 persist 되기 전에 실행)
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.profileImage == null) {
            this.profileImage = "https://ssg-be-s3-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png";
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.reputation == null) {
            this.reputation = 30.0;
        }
        if (this.cash == null) {
            this.cash = 0L;
        }
        if (this.lastLogin == null) {
            this.lastLogin = LocalDateTime.now();
        }
    }

    // 마지막 로그인 시간 업데이트 메서드 (로그인 성공 시 서비스에서 호출)
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    // 연관 관계 설정

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AutoBid> autoBids;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecentlyViewed> recentlyViewedProducts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductLike> likedProducts;

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> purchasedOrders;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> soldOrders;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewsGiven;

    @OneToMany(mappedBy = "reviewee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewsReceived;

    @OneToMany(mappedBy = "payer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reportsByUser;

    @OneToMany(mappedBy = "reportedUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reportsAgainstUser;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ban> bansAsUser;

    @OneToMany(mappedBy = "blockedUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ban> bansAsBlockedUser;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Withdraw> withdraws;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Charge> charges;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserActivity> userActivities;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReputationHistory> reputationHistories;





    // 채팅기능 구현

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatParticipant> chatParticipants;
//
//    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatMessage> sentMessages;


}
