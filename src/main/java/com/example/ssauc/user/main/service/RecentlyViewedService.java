package com.example.ssauc.user.main.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.main.repository.RecentlyViewedRepository;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentlyViewedService {

    private final RecentlyViewedRepository recentlyViewedRepository;
    private final ProductService productService;

    /**
     * 특정 유저가 최근에 본 상품 목록 조회
     */
    public List<RecentlyViewed> getRecentlyViewedItems(Users user) {
        return recentlyViewedRepository.findAllByUserOrderByViewedAtDesc(user);
    }

    public void saveViewedProduct(Users user, Long productId) {
        Product product = productService.getProductById(productId);

        // 이미 본 상품인지 확인
        RecentlyViewed existing = recentlyViewedRepository.findByUserAndProduct(user, product);

        if (existing != null) {
            // 기존 기록이 있으면 시간만 갱신 -> 최신 기록으로 올라감
            existing.setViewedAt(LocalDateTime.now());
            recentlyViewedRepository.save(existing);
        } else {
            // 처음 보는 상품이면 새로 저장
            RecentlyViewed viewed = RecentlyViewed.builder()
                    .user(user)
                    .product(product)
                    .viewedAt(LocalDateTime.now())
                    .build();
            recentlyViewedRepository.save(viewed);
        }

//         필요 시, DB 자체를 7개까지만 유지하고 싶다면 여기서 정리
//         (예: 7개 초과 시 가장 오래된 기록 삭제)
         List<RecentlyViewed> userViews = recentlyViewedRepository.findAllByUserOrderByViewedAtDesc(user);
         if (userViews.size() > 7) {
             for (int i = 7; i < userViews.size(); i++) {
                 recentlyViewedRepository.delete(userViews.get(i));
             }
         }
    }
}
