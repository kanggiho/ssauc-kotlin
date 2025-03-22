package com.example.ssauc.user.search.controller;


import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.search.document.ProductDocument;
import com.example.ssauc.user.search.service.ProductIndexService;
import com.example.ssauc.user.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSearchController {

    private final ProductIndexService productIndexService;
    private final ProductSearchService productSearchService;

    @PostMapping("/products")
    public ProductDocument createProduct(@RequestBody ProductDocument doc) {
        Product product = new Product();
        if (doc.getProductId() != null) {
            product.setProductId(Long.valueOf(doc.getProductId()));
        }
        product.setName(doc.getName());
        product.setDescription(doc.getDescription());
        product.setPrice(doc.getPrice());
        // 필요한 필드 매핑 추가
        productIndexService.saveProduct(product);
        return doc;
    }

    @GetMapping("/analyze")
    public String analyze(@RequestParam String analyzer,
                          @RequestParam String text) {
        return productSearchService.analyzeKeyword(analyzer, text);
    }

    @GetMapping("/search")
    public List<ProductDocument> search(@RequestParam String keyword) {
        return productSearchService.adminSearchProducts(keyword);
    }
}
