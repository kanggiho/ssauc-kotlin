package com.example.ssauc.user.list.Service;

import com.example.ssauc.user.list.dto.ListDto;
import com.example.ssauc.user.list.dto.TempDto;
import com.example.ssauc.user.list.dto.WithLikeDto;
import com.example.ssauc.user.list.repository.ListRepository;
import java.time.Duration;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ListService {
    @Autowired
    private ListRepository listRepository;
    private UsersRepository usersRepository;

    // JWT 현재 이메일을 기반으로 사용자 정보를 조회
    public Users getCurrentUser(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보가 없습니다.3"));
    }

    public Page<TempDto> list(Pageable pageable, Users user) {

        Page<WithLikeDto> list;

        if (user != null) {
            list = listRepository.getWithLikeProductList(user.getUserId(), pageable);
        } else {
            list = listRepository.getAllProductsWithoutUser(pageable); // 로그인 안 한 경우 전체 상품 목록
        }

        List<TempDto> tempList = list.getContent().stream().map(listDto -> {
            Duration duration = Duration.between(LocalDateTime.now(), listDto.getEndAt());

            int days = (int) duration.toDays();
            int hours = (int) duration.toHours() % 24;
            String bidCount = "입찰 %d회".formatted(listDto.getBidCount());
            String inform = "⏳ %d일 %d시간".formatted(days, hours);
            String like = addCommas(String.valueOf(listDto.getLikeCount()));
            String price = addCommas(listDto.getPrice().toString());
            String[] mainImage = listDto.getImageUrl().split(",");
            String status = listDto.getStatus();

            if (days < 0 || hours < 0 || status.equals("판매완료")) { // 마감된 경우
                inform = "⏳ 입찰 마감";
            }

            return TempDto.builder()
                    .productId(listDto.getProductId())
                    .imageUrl(mainImage[0])
                    .name(listDto.getName())
                    .price(price)
                    .bidCount(bidCount)
                    .gap(inform)
                    .location(listDto.getLocation())
                    .likeCount(like)
                    .liked(user != null && listDto.isLiked()) // 로그인 안 하면 liked는 false로 설정
                    .status(listDto.getStatus())
                    .build();
        }).toList();

        return new PageImpl<>(tempList, pageable, list.getTotalElements());
    }

    public Page<TempDto> likelist(Pageable pageable, Users user) {
        Page<WithLikeDto> list = listRepository.getLikeList(user.getUserId(), pageable);;

        List<TempDto> tempList = list.getContent().stream().map(listDto -> {
            Duration duration = Duration.between(LocalDateTime.now(), listDto.getEndAt());

            int days = (int) duration.toDays();
            int hours = (int) duration.toHours() % 24;
            String bidCount = "입찰 %d회".formatted(listDto.getBidCount());
            String inform = "⏳ %d일 %d시간".formatted(days, hours);
            String like = addCommas(String.valueOf(listDto.getLikeCount()));
            String price = addCommas(listDto.getPrice().toString());
            String[] mainImage = listDto.getImageUrl().split(",");
            String status = listDto.getStatus();

            if (days < 0 || hours < 0 || status.equals("판매완료")) { // 마감된 경우
                inform = "⏳ 입찰 마감";
            }


            return TempDto.builder()
                    .productId(listDto.getProductId())
                    .imageUrl(mainImage[0])
                    .name(listDto.getName())
                    .price(price)
                    .bidCount(bidCount)
                    .gap(inform)
                    .location(listDto.getLocation())
                    .likeCount(like)
                    .liked(listDto.isLiked())
                    .status(listDto.getStatus())
                    .build();
        }).toList();

        return new PageImpl<>(tempList, pageable, list.getTotalElements());
    }

    public Page<TempDto> categoryList(Pageable pageable, Users user, Long categoryId) {
        Page<WithLikeDto> list;

        if(user != null) {
            Long id = user.getUserId();
            list = listRepository.getCategoryList(id, categoryId, pageable);
        } else {
            list = listRepository.getCategoryListWithoutUser(categoryId, pageable);
        }

        List<TempDto> tempList = list.getContent().stream().map(listDto -> {
            Duration duration = Duration.between(LocalDateTime.now(), listDto.getEndAt());

            int days = (int) duration.toDays();
            int hours = (int) duration.toHours() % 24;
            String bidCount = "입찰 %d회".formatted(listDto.getBidCount());
            String inform = "⏳ %d일 %d시간".formatted(days, hours);
            String like = addCommas(String.valueOf(listDto.getLikeCount()));
            String price = addCommas(listDto.getPrice().toString());
            String[] mainImage = listDto.getImageUrl().split(",");
            String status = listDto.getStatus();

            if (days < 0 || hours < 0 || status.equals("판매완료")) { // 마감된 경우
                inform = "⏳ 입찰 마감";
            }

            return TempDto.builder()
                    .productId(listDto.getProductId())
                    .imageUrl(mainImage[0])
                    .name(listDto.getName())
                    .price(price)
                    .bidCount(bidCount)
                    .gap(inform)
                    .location(listDto.getLocation())
                    .likeCount(like)
                    .liked(listDto.isLiked())
                    .status(listDto.getStatus())
                    .build();
        }).toList();

        return new PageImpl<>(tempList, pageable, list.getTotalElements());
    }

    public Page<TempDto> getProductsByPrice(Pageable pageable, Users user, int minPrice, int maxPrice) {
        Page<WithLikeDto> list;

        if(user != null) {
            Long userId = user.getUserId();
            list = listRepository.findByPriceRange(userId, minPrice, maxPrice, pageable);
        } else {
            list = listRepository.findByPriceRangeWithUserId(minPrice, maxPrice, pageable);
        }

        List<TempDto> tempList = list.getContent().stream().map(listDto -> {
            Duration duration = Duration.between(LocalDateTime.now(), listDto.getEndAt());

            int days = (int) duration.toDays();
            int hours = (int) duration.toHours() % 24;
            String bidCount = "입찰 %d회".formatted(listDto.getBidCount());
            String inform = "⏳ %d일 %d시간".formatted(days, hours);
            String like = addCommas(String.valueOf(listDto.getLikeCount()));
            String price = addCommas(listDto.getPrice().toString());
            String[] mainImage = listDto.getImageUrl().split(",");
            String status = listDto.getStatus();

            if (days < 0 || hours < 0 || status.equals("판매완료")) { // 마감된 경우
                inform = "⏳ 입찰 마감";
            }

            return TempDto.builder()
                    .productId(listDto.getProductId())
                    .imageUrl(mainImage[0])
                    .name(listDto.getName())
                    .price(price)
                    .bidCount(bidCount)
                    .gap(inform)
                    .location(listDto.getLocation())
                    .likeCount(like)
                    .liked(listDto.isLiked())
                    .status(listDto.getStatus())
                    .build();
        }).toList();

        return new PageImpl<>(tempList, pageable, list.getTotalElements());
    }

    public Page<TempDto> getAvailableBidWithLike(Pageable pageable, Users user) {
        Page<WithLikeDto> list = listRepository.getAvailableProductListWithLike(user.getUserId(), pageable);

        List<TempDto> tempList = list.getContent().stream()
                .filter(listDto -> { // 마감되지 않은 상품만 필터링
                    Duration duration = Duration.between(LocalDateTime.now(), listDto.getEndAt());
                    int days = (int) duration.toDays();
                    int hours = (int) duration.toHours() % 24;
                    return days > 0 || (days == 0 && hours > 0);
                })
                .map(listDto -> {
                    Duration duration = Duration.between(LocalDateTime.now(), listDto.getEndAt());
                    int days = (int) duration.toDays();
                    int hours = (int) duration.toHours() % 24;
                    String bidCount = "입찰 %d회".formatted(listDto.getBidCount());
                    String inform = "⏳ %d일 %d시간".formatted(days, hours);
                    String like = addCommas(String.valueOf(listDto.getLikeCount()));
                    String price = addCommas(listDto.getPrice().toString());
                    String[] mainImage = listDto.getImageUrl().split(",");

                    return TempDto.builder()
                            .productId(listDto.getProductId())
                            .imageUrl(mainImage[0])
                            .name(listDto.getName())
                            .price(price)
                            .bidCount(bidCount)
                            .gap(inform)
                            .location(listDto.getLocation())
                            .likeCount(like)
                            .liked(listDto.isLiked())
                            .status(listDto.getStatus())
                            .build();
                }).toList();

        return new PageImpl<>(tempList, pageable, list.getTotalElements());
    }

    public Page<TempDto> getAvailableBid(Pageable pageable) {
        Page<ListDto> list = listRepository.getAvailableProductList(pageable);

        List<TempDto> tempList = list.getContent().stream()
                .map(listDto -> {
                    Duration duration = Duration.between(LocalDateTime.now(), listDto.getEndAt());
                    int days = (int) duration.toDays();
                    int hours = (int) duration.toHours() % 24;
                    String bidCount = "입찰 %d회".formatted(listDto.getBidCount());
                    String inform = "⏳ %d일 %d시간".formatted(days, hours);
                    String like = addCommas(String.valueOf(listDto.getLikeCount()));
                    String price = addCommas(listDto.getPrice().toString());
                    String[] mainImage = listDto.getImageUrl().split(",");

                    return TempDto.builder()
                            .productId(listDto.getProductId())
                            .imageUrl(mainImage[0])
                            .name(listDto.getName())
                            .price(price)
                            .bidCount(bidCount)
                            .gap(inform)
                            .location(listDto.getLocation())
                            .likeCount(like)
                            .status(listDto.getStatus())
                            .build();
                }).toList();

        return new PageImpl<>(tempList, pageable, list.getTotalElements());
    }

    public static String addCommas(String number) {
        try {
            double value = Double.parseDouble(number); // 실수도 처리 가능
            return String.format("%,.0f", value); // 천 단위마다 콤마 추가
        } catch (NumberFormatException e) {
            return "Invalid number format";
        }
    }
}