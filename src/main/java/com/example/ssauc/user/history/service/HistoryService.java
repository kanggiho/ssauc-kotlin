package com.example.ssauc.user.history.service;

import com.example.ssauc.common.service.CommonUserService;
import com.example.ssauc.user.bid.entity.AutoBid;
import com.example.ssauc.user.bid.entity.Bid;
import com.example.ssauc.user.bid.entity.ProductReport;
import com.example.ssauc.user.bid.repository.AutoBidRepository;
import com.example.ssauc.user.bid.repository.BidRepository;
import com.example.ssauc.user.bid.repository.ProductReportRepository;
import com.example.ssauc.user.chat.entity.Ban;
import com.example.ssauc.user.chat.entity.Report;
import com.example.ssauc.user.chat.repository.ReportRepository;
import com.example.ssauc.user.history.dto.*;
import com.example.ssauc.user.chat.repository.BanRepository;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.mypage.event.OrderCompletedEvent;
import com.example.ssauc.user.mypage.event.OrderShippedEvent;
import com.example.ssauc.user.order.entity.Orders;
import com.example.ssauc.user.order.repository.OrdersRepository;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final CommonUserService commonUserService;
    private final ProductRepository productRepository;
    private final OrdersRepository ordersRepository;
    private final BanRepository banRepository;
    private final BidRepository bidRepository;
    private final AutoBidRepository autoBidRepository;
    private final ReportRepository reportRepository;
    private final ProductReportRepository productReportRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 세션에서 전달된 userId를 이용하여 DB에서 최신 사용자 정보를 조회합니다.
    public Users getCurrentUser(String email) {
        return commonUserService.getCurrentUser(email);
    }

    // ===================== 차단 관리 =====================
    // 차단 리스트
    @Transactional(readOnly = true)
    public Page<BanHistoryDto> getBanListForUser(Long userId, Pageable pageable) {
        Page<Ban> bans = banRepository.findByUserUserId(userId, pageable);
        return bans.map(ban -> new BanHistoryDto(
                ban.getBanId(),
                ban.getBlockedUser().getUserName(),
                ban.getBlockedUser().getProfileImage(),
                ban.getBlockedAt()
        ));
    }

    // 차단 해제
    @Transactional
    public void unbanUser(Long banId, Long userId) {
        // 차단 내역 소유 여부 확인 후 삭제 처리
        Ban ban = banRepository.findById(banId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 차단 내역입니다. banId=" + banId));
        if (!ban.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("차단 해제 권한이 없습니다.");
        }
        banRepository.delete(ban);
    }

    // ===================== 신고 내역 =====================
    // 상품 신고 리스트 조회
    @Transactional(readOnly = true)
    public Page<ProductReportDto> getProductReportHistoryPage(Users reporter, Pageable pageable) {
        Page<ProductReport> reports = productReportRepository.findByReporter(reporter, pageable);

        return reports.map(report -> {

            String productImageUrl = report.getProduct().getImageUrl();
            String mainImage = productImageUrl != null ? productImageUrl.split(",")[0] : null;

            return ProductReportDto.builder()
                    .reportId(report.getReportId())
                    .productId(report.getProduct().getProductId())
                    .productName(report.getProduct().getName())
                    .productImageUrl(mainImage)
                    .reportReason(report.getReportReason())
                    .reportDate(report.getReportDate())
                    .processedAt(report.getProcessedAt())
                    .status(report.getStatus())
                    .build();
        });
    }

    // 유저 신고 리스트 조회
    @Transactional(readOnly = true)
    public Page<UserReportDto> getUserReportHistoryPage(Users reporter, Pageable pageable) {
        Page<Report> reports = reportRepository.findByReporter(reporter, pageable);
        return reports.map(report -> UserReportDto.builder()
                .reportId(report.getReportId())
                .reportedUserName(report.getReportedUser().getUserName())
                .profileImageUrl(report.getReportedUser().getProfileImage())
                .reportReason(report.getReportReason())
                .reportDate(report.getReportDate())
                .processedAt(report.getProcessedAt())
                .status(report.getStatus())
                .build());
    }

    // 신고 상세 내역
    @Transactional(readOnly = true)
    public ReportDetailDto getReportDetail(String filter, Long id) {
        if ("product".equals(filter)) {
            Optional<ProductReport> productReportOpt = productReportRepository.findById(id);
            // 하나의 메서드 내에서 두 테이블(ProductReport와 Report)을 순차적으로 확인하는 구조라 if‑present로 분기 처리
            if (productReportOpt.isPresent()) {
                ProductReport pr = productReportOpt.get();
                return ReportDetailDto.builder()
                        .type("product")
                        .productId(pr.getProduct().getProductId())
                        .productName(pr.getProduct().getName())
                        .reportReason(pr.getReportReason())
                        .details(pr.getDetails())
                        .reportDate(pr.getReportDate())
                        .status(pr.getStatus())
                        .processedAt(pr.getProcessedAt())
                        .build();
            } else {
                throw new RuntimeException("상품 신고 내역이 존재하지 않습니다. id=" + id);
            }
        } else if ("user".equals(filter)) {
            Optional<Report> reportOpt = reportRepository.findById(id);
            if (reportOpt.isPresent()) {
                Report r = reportOpt.get();
                return ReportDetailDto.builder()
                        .type("user")
                        .reportedUserName(r.getReportedUser().getUserName())
                        .reportReason(r.getReportReason())
                        .details(r.getDetails())
                        .reportDate(r.getReportDate())
                        .status(r.getStatus())
                        .processedAt(r.getProcessedAt())
                        .build();
            } else {
                throw new RuntimeException("유저 신고 내역이 존재하지 않습니다. id=" + id);
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 신고 유형: " + filter);
        }
    }

    // ===================== 판매 내역 =====================
    // 판매중 리스트
    @Transactional(readOnly = true)
    public Page<SellHistoryOngoingDto> getOngoingSellHistoryPage(Users seller, Pageable pageable) {
        // 판매중 리스트: 경매 마감 시간에 10분을 더한 값이 현재 시간보다 아직 지나지 않은 상품
        // 즉, 상품의 endAt >= (현재 시간 - 10분)인 경우만 포함
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        Page<Product> productsPage = productRepository.findBySellerAndStatusAndEndAtGreaterThanEqual(seller, "판매중", cutoff, pageable);

        return productsPage.map(p -> {

            String productImageUrl = p.getImageUrl();
            String mainImage = productImageUrl != null ? productImageUrl.split(",")[0] : null;

            return SellHistoryOngoingDto.builder()
                    .productId(p.getProductId())
                    .productName(p.getName())
                    .productImageUrl(mainImage)
                    .tempPrice(p.getTempPrice())
                    .price(p.getPrice())
                    .createdAt(p.getCreatedAt())
                    .endAt(p.getEndAt())
                    .build();
        });
    }

    // 판매 마감 리스트
    @Transactional(readOnly = true)
    public Page<SellHistoryOngoingDto> getEndedSellHistoryPage(Users seller, Pageable pageable) {
        // 판매 마감 리스트: 판매중 상태에서 (경매 마감 시간 + 10분)이 이미 지난 상품
        // 즉, 상품의 endAt < (현재 시간 - 10분)인 경우
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        Page<Product> productsPage = productRepository.findBySellerAndStatusAndEndAtBefore(seller, "판매중", cutoff, pageable);
        return productsPage.map(p -> {

            String productImageUrl = p.getImageUrl();
            String mainImage = productImageUrl != null ? productImageUrl.split(",")[0] : null;

            return SellHistoryOngoingDto.builder()
                    .productId(p.getProductId())
                    .productName(p.getName())
                    .productImageUrl(mainImage)
                    .tempPrice(p.getTempPrice())
                    .price(p.getPrice())
                    .createdAt(p.getCreatedAt())
                    .endAt(p.getEndAt())
                    .build();
        });
    }

    // 판매 완료 리스트
    @Transactional(readOnly = true)
    public Page<SellHistoryCompletedDto> getCompletedSellHistoryPage(Users seller, Pageable pageable) {
        Page<Product> productsPage = productRepository.findBySellerAndStatus(seller, "판매완료", pageable);
        return productsPage.map(p -> {
            // 판매 완료된 상품의 주문 정보는 보통 하나가 존재한다고 가정합니다.
            Orders order = p.getOrders().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("판매 완료 상품에 주문 정보가 없습니다."));

            String productImageUrl = p.getImageUrl();
            String mainImage = productImageUrl != null ? productImageUrl.split(",")[0] : null;

            return SellHistoryCompletedDto.builder()
                    .orderId(order.getOrderId())
                    .productId(p.getProductId())
                    .productImageUrl(mainImage)
                    .productName(p.getName())
                    .buyerName(order.getBuyer().getUserName())
                    .profileImageUrl(order.getBuyer().getProfileImage())
                    .totalPrice(order.getTotalPrice())
                    .orderDate(order.getOrderDate())
                    .completedDate(order.getCompletedDate())
                    .build();
        });
    }

    // 판매 완료 상세 내역
    @Transactional(readOnly = true)
    public SoldDetailDto getSoldDetailByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 상품이 존재하지 않습니다."));

        Orders order = product.getOrders().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("해당 상품의 주문 정보가 없습니다."));

        return SoldDetailDto.builder()
                .productId(product.getProductId())
                .productName(product.getName())
                .startPrice(product.getStartPrice())
                .createdAt(product.getCreatedAt())
                .dealType(product.getDealType())
                .imageUrl(product.getImageUrl())
                .orderId(order.getOrderId())
                .buyerName(order.getBuyer().getUserName())
                .totalPrice(order.getTotalPrice())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .postalCode(order.getPostalCode())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryStatus(order.getDeliveryStatus())
                .orderDate(order.getOrderDate())
                .completedDate(order.getCompletedDate())
                .build();
    }

    // 운송장 번호 등록 또는 수정
    @Transactional
    public boolean updateDeliveryStatus(Long orderId, String trackingNumber) {
        Optional<Orders> optionalOrder = ordersRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Orders orders = optionalOrder.get();
            orders.setDeliveryStatus(trackingNumber);
            ordersRepository.save(orders);

            // 운송장 등록 시각
            LocalDateTime now = LocalDateTime.now();

            // 24시간 내 운송장 등록 이벤트 발행
            // 주문일과 비교하여 24시간 이내이면 이벤트 발행
            if(java.time.Duration.between(orders.getOrderDate(), now).toHours() < 24) {
                eventPublisher.publishEvent(
                        new OrderShippedEvent(this, orders.getSeller().getUserId(), orders.getOrderDate(), now)
                );
            }
            return true;
        }
        return false;
    }

    // ===================== 구매 내역 =====================
    // 입찰중 리스트 (Bid 테이블에서 구매자가 입찰한 내역 조회)
    @Transactional(readOnly = true)
    public Page<BuyBidHistoryDto> getBiddingHistoryPage(Users buyer, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        Page<Bid> bidPage = bidRepository.findLatestBidsByUser(buyer, now, pageable);

        return bidPage.map(bid -> {
            // 해당 입찰에 대해, 활성화된 AutoBid 엔티티를 조회 (없으면 null)
            AutoBid autoBid = autoBidRepository.findByUserAndProductAndActive(buyer, bid.getProduct(), true);
            Long maxBidAmount = (autoBid != null) ? autoBid.getMaxBidAmount() : null;

            String productImageUrl = bid.getProduct().getImageUrl();
            String mainImage = productImageUrl != null ? productImageUrl.split(",")[0] : null;

            return BuyBidHistoryDto.builder()
                    .bidId(bid.getBidId())
                    .productId(bid.getProduct().getProductId())
                    .productName(bid.getProduct().getName())
                    .productImageUrl(mainImage)
                    .sellerName(bid.getProduct().getSeller().getUserName())
                    .profileImageUrl(bid.getProduct().getSeller().getProfileImage())
                    .endAt(bid.getProduct().getEndAt())
                    .bidPrice(bid.getBidPrice())
                    .maxBidAmount(maxBidAmount)
                    .tempPrice(bid.getProduct().getTempPrice())
                    .build();
        });
    }

    // 구매 완료 리스트 (구매자 기준)
    @Transactional(readOnly = true)
    public Page<BuyHistoryDto> getPurchaseHistoryPage(Users buyer, Pageable pageable) {
        // Orders에서 buyer가 일치하는 주문을 조회합니다.
        Page<Orders> ordersPage = ordersRepository.findByBuyer(buyer, pageable);


        return ordersPage.map(order -> {
            String productImageUrl = order.getProduct().getImageUrl();
            String mainImage = productImageUrl != null ? productImageUrl.split(",")[0] : null;

            return BuyHistoryDto.builder()
                    .orderId(order.getOrderId())
                    .productId(order.getProduct().getProductId())
                    .productName(order.getProduct().getName())
                    .productImageUrl(mainImage)
                    .sellerName(order.getSeller().getUserName())
                    .profileImageUrl(order.getSeller().getProfileImage())
                    .totalPrice(order.getTotalPrice())
                    .orderDate(order.getOrderDate())
                    .completedDate(order.getCompletedDate())
                    .build();
        });
    }

    // 구매 내역 상세 (특정 상품의 구매 내역, 구매자 기준)
    @Transactional(readOnly = true)
    public BoughtDetailDto getBoughtDetailByProductId(Long productId, Users buyer) {
        // Product 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 상품이 존재하지 않습니다."));
        // 해당 상품의 주문 중 구매자가 일치하는 주문 찾기
        Orders order = product.getOrders().stream()
                .filter(o -> o.getBuyer().getUserId().equals(buyer.getUserId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 상품의 주문 정보가 없습니다."));
        return BoughtDetailDto.builder()
                .orderId(order.getOrderId())
                .productId(product.getProductId())
                .productName(product.getName())
                .startPrice(product.getStartPrice())
                .createdAt(product.getCreatedAt())
                .dealType(product.getDealType())
                .imageUrl(product.getImageUrl())
                .sellerName(order.getSeller().getUserName())
                .totalPrice(order.getTotalPrice())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .postalCode(order.getPostalCode())
                .deliveryAddress(order.getDeliveryAddress())
                .orderDate(order.getOrderDate())
                .completedDate(order.getCompletedDate())
                .deliveryStatus(order.getDeliveryStatus())
                .build();
    }

    // 구매 내역 상세에서 거래 완료 기능
    @Transactional
    public void completeOrder(Long orderId, Users user) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 정보가 존재하지 않습니다."));
        // 구매자 본인 여부 확인
        if (!order.getBuyer().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("주문 완료 권한이 없습니다.");
        }
        order.setOrderStatus("완료");
        order.setCompletedDate(LocalDateTime.now());
        ordersRepository.save(order);

        // 주문 완료 이벤트 발행 (reputation)
        eventPublisher.publishEvent(new OrderCompletedEvent(this, order.getBuyer().getUserId(), order.getSeller().getUserId(), order.getCompletedDate()));
    }
}
