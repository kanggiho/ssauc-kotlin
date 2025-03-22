package com.example.ssauc.user.bid.service;

import com.example.ssauc.user.bid.dto.*;
import com.example.ssauc.user.bid.entity.AutoBid;
import com.example.ssauc.user.bid.entity.Bid;
import com.example.ssauc.user.bid.entity.ProductReport;
import com.example.ssauc.user.bid.repository.AutoBidRepository;
import com.example.ssauc.user.bid.repository.BidRepository;
import com.example.ssauc.user.bid.repository.PdpRepository;
import com.example.ssauc.user.bid.repository.ProductReportRepository;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.main.entity.Notification;
import com.example.ssauc.user.main.repository.NotificationRepository;
import com.example.ssauc.user.main.repository.ProductLikeRepository;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BidService {
    @Autowired
    private PdpRepository pdpRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ProductReportRepository productReportRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AutoBidRepository autoBidRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationRepository notificationRepository;


    public ProductInformDto getBidInform(Long productId) {
        // pdp에 들어갈 정보 가져오기
        ProductInformDto informDto = pdpRepository.getPdpInform(productId);

        // 전체 시간 구하기
        Long totalTime = Duration.between(LocalDateTime.now(), informDto.getEndAt()).getSeconds();
        informDto.setTotalTime(totalTime);
        return informDto;
    }

    public List<CarouselImage> getCarouselImages(Long productId) {
        // pdp에 들어갈 정보 가져오기
        ProductInformDto informDto = pdpRepository.getPdpInform(productId);

        List<CarouselImage> CarouselImages = new ArrayList<>();
        String[] urls = informDto.getImageUrl().split(",");
        int i = 1;
        for (String url : urls) {
            CarouselImage image = new CarouselImage();
            image.setUrl(url);
            image.setAlt("Slide " + i);
            i++;
            CarouselImages.add(image);
        }
        return CarouselImages;
    }

    public void insertReportData(ReportDto dto) {
        Long reportedUserId = pdpRepository.findById(dto.getProductId()).get().getSeller().getUserId();
        dto.setReportedUserId(reportedUserId);

        ProductReport productReport = ProductReport.builder()
                .product(pdpRepository.findById(dto.getProductId()).orElseThrow())
                .reporter(usersRepository.findById(dto.getReporterId()).orElseThrow())
                .reportedUser(usersRepository.findById(dto.getReportedUserId()).orElseThrow())
                .reportReason(dto.getReportReason())
                .status("처리전")
                .details(dto.getDetails())
                .reportDate(LocalDateTime.now())
                .processedAt(null)
                .build();

        productReportRepository.save(productReport);
    }

    public Product getProduct(Long productId) {
        return pdpRepository.findById(productId).orElseThrow();
    }

    public Users getUser(Long userId) {
        return usersRepository.findById(userId).orElseThrow();
    }


    // 일반 입찰 기능 구현
    @Transactional
    public boolean placeBid(BidRequestDto bidRequestDto) {
        Product bidProduct = pdpRepository.findProductForUpdate(bidRequestDto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        Users bidUser = getUser(Long.valueOf(bidRequestDto.getUserId()));
        long bidAmount = (long) bidRequestDto.getBidAmount();

        // 일반 입찰이 유효한지 검증 및 현재가 업데이트
        int updatedRows = pdpRepository.updateProductField(bidAmount, bidRequestDto.getProductId());
        if (updatedRows == 0) {
            // 입찰 조건이 맞지 않아 업데이트가 이루어지지 않았을 경우 false 반환
            return false;
        }

        // Bid 객체 만들고 저장
        Bid bid = Bid.builder()
                .product(bidProduct)
                .user(bidUser)
                .bidPrice(bidAmount)
                .bidTime(LocalDateTime.now())
                .build();
        bidRepository.save(bid);
        setAdditionalTime(bidRequestDto.getProductId());

        overBidNotice(bidProduct.getProductId(), bidUser.getUserId());


        return true;
    }


    // 자동 입찰 기능 구현
    @Transactional
    public boolean autoBid(AutoBidRequestDto autoBidRequestDto) {
        Long productId = autoBidRequestDto.getProductId();
        String userIdStr = autoBidRequestDto.getUserId();
        Long maxBidAmount = (long) autoBidRequestDto.getMaxBidAmount();

        Product product = pdpRepository.findProductForUpdate(autoBidRequestDto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        Users user = getUser(Long.valueOf(userIdStr));

        // 이미 같은 (user, product)에 대한 자동입찰이 있는지 확인
        AutoBid existingAuto = autoBidRepository
                .findByProductAndActiveIsTrue(product)
                .stream()
                .filter(ab -> ab.getUser().getUserId().equals(user.getUserId()))
                .findFirst()
                .orElse(null);

        if (existingAuto != null) {
            // 기존에 등록한 자동입찰이 있을 경우 maxBidAmount 갱신
            existingAuto.setMaxBidAmount(maxBidAmount);
            autoBidRepository.save(existingAuto);
        } else {
            // 없으면 새로 생성
            AutoBid newAutoBidding = AutoBid.builder()
                    .product(product)
                    .user(user)
                    .maxBidAmount(maxBidAmount)
                    .createdAt(LocalDateTime.now())
                    .active(true)
                    .build();
            autoBidRepository.save(newAutoBidding);
        }

        // 이제 등록만 하는 것이 아니라,
        // 혹시 새로 등록된/갱신된 maxBidAmount가 현재가보다 높다면 자동으로 입찰 동작을 실행한다.
        //processAutoBidding(productId);

        return true;
    }


    @Transactional
    public void processAutoBidding(Long productId) {
        // 1) 해당 상품을 비관적 락으로 조회 (다른 트랜잭션에서 동시에 변경하지 못하게)
        Product product = pdpRepository.findProductForUpdate(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        Long currentPrice = product.getTempPrice(); // 현재가
        Long minIncrement = (long) product.getMinIncrement(); // 최소 증가 금액

        // 2) 해당 상품의 active=true인 자동입찰 목록 조회
        List<AutoBid> autoBidders = autoBidRepository.findByProductAndActiveIsTrue(product);
        if (autoBidders.isEmpty()) {
            return; // 자동입찰자가 없으면 로직 종료
        }

        // 3) 자동입찰자들을 maxBidAmount 기준 내림차순 정렬
        autoBidders.sort((a, b) -> b.getMaxBidAmount().compareTo(a.getMaxBidAmount()));

        // 4) 최고 자동입찰자(1등)와 해당 사용자가 설정한 최대 자동입찰 금액
        AutoBid topBidder = autoBidders.get(0);
        Long topMax = topBidder.getMaxBidAmount();

        // 5) 가장 최근의 입찰 내역 조회
        Bid lastBid = bidRepository.findTopByProductOrderByBidTimeDesc(product).orElse(null);

        // ★ 추가된 부분: 단일 자동입찰자인 경우,
        // 만약 이미 입찰 내역이 있다면(즉, lastBid가 null이 아니라면) 추가 입찰을 진행하지 않음.
        if (autoBidders.size() == 1 && lastBid != null) {
            // 단일 자동입찰자인데 이미 입찰한 상태라면 더 이상 입찰하지 않음
            return;
        }

        // 6) 이미 최고입찰자인 경우에는 자동입찰 진행하지 않음 (경쟁 입찰자가 있는 경우만 진행)
        if (lastBid != null && lastBid.getUser().getUserId().equals(topBidder.getUser().getUserId())) {
            return;
        }

        Long secondMax;
        // 7) 경쟁자가 두 명 이상일 경우,
        // 두 번째 최고 자동입찰 금액과 현재가 중 더 큰 값을 2등으로 간주
        if (autoBidders.size() >= 2) {
            // 상품의 최신 현재가를 다시 조회 (변경되었을 가능성 고려)
            Long updatedCurrentPrice = pdpRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found")).getTempPrice();
            secondMax = Math.max(autoBidders.get(1).getMaxBidAmount(), updatedCurrentPrice);
        } else {
            // 단일 자동입찰자일 경우, 경쟁자가 없으므로 현재가를 2등으로 간주
            secondMax = currentPrice;
        }

        // 8) 새 입찰 가격 계산: 2등 가격에 최소 증가분을 더함
        Long desiredPrice = secondMax + minIncrement;
        // 1등의 최대 자동입찰 금액을 초과하면 1등의 한도까지만 입찰
        if (desiredPrice > topMax) {
            desiredPrice = topMax;
        }

        // 9) 계산된 새 가격이 현재가보다 높을 경우에만 입찰 진행
        if (desiredPrice > currentPrice) {
            // 상품의 현재가를 새 입찰가로 업데이트
            pdpRepository.updateProductField(desiredPrice, productId);

            // 새 입찰 내역 생성 및 저장
            Bid newBid = Bid.builder()
                    .product(product)
                    .user(topBidder.getUser())
                    .bidPrice(desiredPrice)
                    .bidTime(LocalDateTime.now())
                    .build();
            bidRepository.save(newBid);

            // 입찰 시 추가 시간을 부여하는 로직 실행
            setAdditionalTime(productId);

            // 초과 입찰 알림 실행
            overBidNotice(productId, topBidder.getUser().getUserId());
        }
    }



    // 스케줄러 메서드: 매 분 0초마다 자동입찰 로직 실행
    @Scheduled(cron = "0 * * * * *")  // 초, 분, 시, 일, 월, 요일 순 (매 분 0초 실행)
    @Transactional
    public void scheduledAutoBidding() {


        //System.out.println("실행됨");
        //System.out.println(pdpRepository.findById(56L).orElseThrow().getTempPrice());


        // active 상태인 자동입찰 데이터들에서 상품 목록 추출

        List<AutoBid> activeAutoBids = autoBidRepository.findAll().stream()
                .filter(AutoBid::isActive)
                .toList();

        Set<Product> productSet = activeAutoBids.stream()
                .map(AutoBid::getProduct)
                .collect(Collectors.toSet());

        // 각 상품에 대해 자동입찰 로직 실행
        for (Product product : productSet) {
            processAutoBidding(product.getProductId());
        }
        deadlineBidNotice();
    }


    public void deadlineBidNotice(){
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            int gap = (int) Duration.between(LocalDateTime.now(), product.getEndAt()).toSeconds();
            if (1770 < gap && gap < 1830) {
                List<Users> users = bidRepository.findUserIdsByProductId(product.getProductId());

                String message = product.getName() + " 상품의 입찰마감시간이 30분 남았습니다.";

                for (Users user : users) {
                    Notification notification = Notification
                            .builder()
                            .user(user)
                            .type("입찰마감")
                            .message(message)
                            .createdAt(LocalDateTime.now())
                            .readStatus(1)
                            .build();
                    notificationRepository.save(notification);
                }

            }
        }
    }





    public String getHighestBidUser(Long productId) {
        Long tempUserId = bidRepository.findUserIdWithHighestBidPrice(productId);

        if (tempUserId == null) {
            return "입찰자 없음";
        } else {
            Users user = usersRepository.findById(tempUserId).orElseThrow();
            return user.getUserName();
        }

    }

    public boolean isProductLike(Long productId, Long userId) {
        return productLikeRepository.countByProductIdAndUserId(productId, userId) > 0;

    }

    // 종료 5분전 입찰 시 10분 추가
    public void setAdditionalTime(Long productId) {
        LocalDateTime now = LocalDateTime.now();
        Product product = pdpRepository.findById(productId).orElseThrow();
        long seconds = Duration.between(now, product.getEndAt()).getSeconds();
        if (seconds <= 300) {
            extendProductEndAt(productId, 600);
        }

    }

    public void extendProductEndAt(Long productId, long addTime) {
        Product product = pdpRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        LocalDateTime newEndAt = product.getEndAt().plusSeconds(addTime);

        // JPQL을 통해 업데이트
        pdpRepository.updateEndAt(productId, newEndAt);
    }

    public void overBidNotice(Long productId, Long userId) {

        // 해당 productId와 현재 입찰한 userId가 아닌 사용자들에게 알림 생성
        List<Long> overwhelmedUser = bidRepository.findDistinctUserIdByProductIdAndNotUserId(productId, userId);

        // 상품 이름 조회
        String productName = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found")).getName();

        for (Long over : overwhelmedUser) {

            String message = productName + " 상품이 상회 입찰 되었습니다.";

            // 먼저, 해당 사용자(over)에 대해 동일 조건의 알림이 존재하는지 확인
            Optional<Notification> existingNotificationOpt = notificationRepository
                    .findNotification(over, message, "상회입찰", 1);

            if (existingNotificationOpt.isPresent()) {
                // 이미 존재하면 readStatus 업데이트만 수행
                Notification existingNotification = existingNotificationOpt.get();
                existingNotification.setReadStatus(0);
                notificationRepository.save(existingNotification);
            } else {
                // 존재하지 않으면 새 알림 생성
                Users targetUser = usersRepository.findById(over)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Notification notification = Notification.builder()
                        .user(targetUser)
                        .type("상회입찰")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .readStatus(1)
                        .build();
                notificationRepository.save(notification);
            }
        }
    }


}
