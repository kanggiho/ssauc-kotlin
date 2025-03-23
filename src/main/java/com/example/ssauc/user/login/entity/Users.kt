package com.example.ssauc.user.login.entity

import com.example.ssauc.user.bid.entity.AutoBid
import com.example.ssauc.user.bid.entity.Bid
import com.example.ssauc.user.cash.entity.Charge
import com.example.ssauc.user.cash.entity.Withdraw
import com.example.ssauc.user.chat.entity.Ban
import com.example.ssauc.user.chat.entity.Report
import com.example.ssauc.user.contact.entity.Board
import com.example.ssauc.user.main.entity.Notification
import com.example.ssauc.user.main.entity.ProductLike
import com.example.ssauc.user.main.entity.RecentlyViewed
import com.example.ssauc.user.mypage.entity.ReputationHistory
import com.example.ssauc.user.mypage.entity.UserActivity
import com.example.ssauc.user.order.entity.Orders
import com.example.ssauc.user.pay.entity.Payment
import com.example.ssauc.user.pay.entity.Review
import com.example.ssauc.user.product.entity.Product
import jakarta.persistence.*
import lombok.*
import java.time.LocalDateTime

@Entity
@Builder
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Users(
    @field:Column(nullable = false, length = 50) public var userName: String, @field:Column(
        nullable = false,
        length = 255
    ) public var password: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public val userId: Long? = null

    @Column(nullable = false, length = 100, unique = true)
    public var email: String? = null

    @Column(length = 15, unique = true)
    public var phone: String? = null

    // profileImage: 기본값 적용 (DDL 기본값과 @PrePersist로 세팅)
    @Column(
        length = 500,
        columnDefinition = "varchar(500) default 'https://ssg-be-s3-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png'"
    )
    public var profileImage: String? = null

    @Column(length = 300)
    public var location: String? = null // 지역 정보

    // status: 기본값 active
    @Column(columnDefinition = "varchar(50) default 'active'")
    public var status: String? = null

    // reputation: 기본값 50.0
    @Column(columnDefinition = "double default 50.0")
    public var reputation: Double? = null

    public val warningCount = 0

    public var createdAt: LocalDateTime?
    public val updatedAt: LocalDateTime? = null
    public var lastLogin: LocalDateTime? = null

    // cash: 기본값 0
    @Column(columnDefinition = "bigint default 0")
    public var cash: Long? = null

    // review.comment 요약 (초기값 null)
    @Column(name = "review_summary", columnDefinition = "TEXT")
    public var reviewSummary: String? = null

    // 신규 생성 시 기본값 설정 (엔티티가 persist 되기 전에 실행)
    @PrePersist
    fun prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now()
        }
        if (this.profileImage == null) {
            this.profileImage = "https://ssg-be-s3-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png"
        }
        if (this.status == null) {
            this.status = "ACTIVE"
        }
        if (this.reputation == null) {
            this.reputation = 30.0
        }
        if (this.cash == null) {
            this.cash = 0L
        }
        if (this.lastLogin == null) {
            this.lastLogin = LocalDateTime.now()
        }
    }

    // 마지막 로그인 시간 업데이트 메서드 (로그인 성공 시 서비스에서 호출)
    fun updateLastLogin() {
        this.lastLogin = LocalDateTime.now()
    }

    // 연관 관계 설정
    @OneToMany(mappedBy = "seller", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val products: List<Product>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val bids: List<Bid>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val autoBids: List<AutoBid>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val recentlyViewedProducts: List<RecentlyViewed>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val likedProducts: List<ProductLike>? = null

    @OneToMany(mappedBy = "buyer", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val purchasedOrders: List<Orders>? = null

    @OneToMany(mappedBy = "seller", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val soldOrders: List<Orders>? = null

    @OneToMany(mappedBy = "reviewer", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val reviewsGiven: List<Review>? = null

    @OneToMany(mappedBy = "reviewee", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val reviewsReceived: List<Review>? = null

    @OneToMany(mappedBy = "payer", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val payments: List<Payment>? = null

    @OneToMany(mappedBy = "reporter", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val reportsByUser: List<Report>? = null

    @OneToMany(mappedBy = "reportedUser", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val reportsAgainstUser: List<Report>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val notifications: List<Notification>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val boards: List<Board>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val bansAsUser: List<Ban>? = null

    @OneToMany(mappedBy = "blockedUser", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val bansAsBlockedUser: List<Ban>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val withdraws: List<Withdraw>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val charges: List<Charge>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val userActivities: List<UserActivity>? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    public val reputationHistories: List<ReputationHistory>? = null // 채팅기능 구현
    //    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<ChatParticipant> chatParticipants;
    //
    //    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<ChatMessage> sentMessages;


    // 생성자 (username, password 만 받는 생성자)
    init {
        this.createdAt = LocalDateTime.now()
    }
}
