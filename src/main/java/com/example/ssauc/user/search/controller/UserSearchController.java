package com.example.ssauc.user.search.controller;


import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.CategoryRepository;
import com.example.ssauc.user.product.repository.ProductRepository;
import com.example.ssauc.user.search.dto.ProductDTO;
import com.example.ssauc.user.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 실제 서비스에서 사용하는 상품 컨트롤러 (검색/카테고리/목록 조회)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class UserSearchController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSearchService productSearchService;

    /**
     * [사용자용] 간단한 검색 (ES에서 Product를 조회)
     */
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "30") int size) {
        return productSearchService.searchProducts(keyword);
    }

    /**
     * (중복 제거) 카테고리 조회는 여기서 제공
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> list = categoryRepository.findAll().stream()
                .map(cat -> cat.getName())
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * 필터 / 정렬 / 페이지네이션 플로우
     * 실제 프론트엔드(PLP)에서 호출하는 API
     */
    @GetMapping("/plp")
    public ResponseEntity<Map<String,Object>> getFilteredProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "VIEW_DESC") String sort,
            @RequestParam(required = false) Boolean auctionOnly,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice) {

        // 페이징 & 정렬
        Pageable pageable = PageRequest.of(page - 1, size, getSortOrder(sort));

        // 다양한 필터를 종합적으로 처리하는 JPA 쿼리
        List<Product> found = productRepository.findByFilters(keyword, auctionOnly, categories, minPrice, maxPrice, pageable);

        // DTO 변환
        List<ProductDTO> products = found.stream()
                .map(p -> new ProductDTO(
                        p.getProductId(),
                        p.getName(),
                        p.getDescription(),
                        p.getCategory() != null ? p.getCategory().getName() : "카테고리 없음",
                        p.getPrice(),
                        p.getStartPrice(),
                        p.getTempPrice(),
                        p.getMinIncrement(),
                        p.getBidCount(),
                        p.getLikeCount(),
                        (p.getImageUrl() != null) ? p.getImageUrl() : "/img/noimage.png",
                        p.getViewCount(),
                        p.getStatus(),
                        (p.getSeller() != null && p.getSeller().getLocation() != null) ? p.getSeller().getLocation() : "위치정보 없음"
                ))
                .collect(Collectors.toList());

        // 페이지네이션 계산
        int totalCount = products.size();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        Map<String,Object> result = new HashMap<>();
        result.put("products", products);
        result.put("totalCount", totalCount);
        result.put("page", page);
        result.put("totalPages", totalPages);

        return ResponseEntity.ok(result);
    }

    /**
     * 상품 클릭 시 조회수 증가 (ES 문서도 업데이트)
     */
    @PostMapping("/click/{productId}")
    public void incrementClick(@PathVariable Long productId) {
        productSearchService.incrementProductClick(productId);
    }

    /**
     * 정렬 옵션 헬퍼 메서드
     */
    private Sort getSortOrder(String sort) {
        switch (sort) {
            case "PRICE_ASC": return Sort.by(Sort.Direction.ASC, "price");
            case "PRICE_DESC": return Sort.by(Sort.Direction.DESC, "price");
            case "BID_DESC": return Sort.by(Sort.Direction.DESC, "bidCount");
            case "LIKE_DESC": return Sort.by(Sort.Direction.DESC, "likeCount");
            case "RECENT": return Sort.by(Sort.Direction.DESC, "createdAt");
            default: return Sort.by(Sort.Direction.DESC, "viewCount");
        }
    }
}
