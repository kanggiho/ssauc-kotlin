package com.example.ssauc.user.product.service;

import com.example.ssauc.common.service.CommonUserService;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.product.dto.ProductInsertDto;
import com.example.ssauc.user.product.dto.ProductUpdateDto;
import com.example.ssauc.user.product.entity.Category;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.CategoryRepository;
import com.example.ssauc.user.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final CommonUserService commonUserService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    // JWT 현재 이메일을 기반으로 사용자 정보를 조회
    public Users getCurrentUser(String email) {
        return commonUserService.getCurrentUser(email);
    }

    // 새로운 상품을 등록
    public Product insertProduct(ProductInsertDto dto, Users seller) {
        // 카테고리 검증: categoryName으로 조회
        Category category = categoryRepository.findByName(dto.getCategoryName())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        // 마감 시간(LocalDateTime) 계산: auctionDate, auctionHour, auctionMinute 사용
        LocalDate closingDate = LocalDate.parse(dto.getAuctionDate());
        LocalTime closingTime = LocalTime.of(dto.getAuctionHour(), dto.getAuctionMinute());
        LocalDateTime auctionClosingDateTime = LocalDateTime.of(closingDate, closingTime);

        // Product 엔티티 빌더 사용
        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .tempPrice(dto.getStartPrice()) // 이거 tempPrice 라고 안하고 startPrice로 한게 잘못한게 아님 초기값은 현재입찰가 == 초기입찰가 같아서 이렇게 함
                .startPrice(dto.getStartPrice())
                .imageUrl(dto.getImageUrl())
                .status("판매중")
                .createdAt(LocalDateTime.now())
                .endAt(auctionClosingDateTime)
                .viewCount(0L)
                .minIncrement(dto.getMinIncrement())
                .dealType(dto.getDealType())
                .build();
        return productRepository.save(product);
    }

    // 상품 ID를 기반으로 상품 정보를 조회
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
    }

    // 모든 상품 카테고리 목록을 조회
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 특정 상품에 입찰이 존재하는지 확인
    public boolean hasBids(Long productId) {
        // Product 엔티티의 bidCount 필드를 이용하거나 bidRepository를 이용하여 해당 상품의 입찰 건수가 있는지 체크
        Product product = getProductById(productId);
        return product.getBidCount() > 0;
    }

    // 상품 정보 수정
    public void updateProduct(ProductUpdateDto dto, Users seller) {
        Product product = getProductById(dto.getProductId());
        if (!product.getSeller().getUserId().equals(seller.getUserId())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }
        // 필요에 따라 카테고리 조회
        Category category = categoryRepository.findByName(dto.getCategoryName())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        // 상품 정보 업데이트
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStartPrice(dto.getStartPrice());
        product.setTempPrice(dto.getStartPrice()); // 초기 입찰가와 동일하게
        product.setImageUrl(dto.getImageUrl());
        product.setMinIncrement(dto.getMinIncrement());
        product.setDealType(dto.getDealType());

        // 경매 종료 시간 업데이트 (auctionDate, auctionHour, auctionMinute 이용)
        LocalDate closingDate = LocalDate.parse(dto.getAuctionDate());
        LocalTime closingTime = LocalTime.of(dto.getAuctionHour(), dto.getAuctionMinute());
        product.setEndAt(LocalDateTime.of(closingDate, closingTime));

        product.setCategory(category);
        productRepository.save(product);
    }

    // 상품 삭제
    public void deleteProduct(Long productId, Users seller) {
        Product product = getProductById(productId);
        if (!product.getSeller().getUserId().equals(seller.getUserId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        productRepository.delete(product);
    }
}
