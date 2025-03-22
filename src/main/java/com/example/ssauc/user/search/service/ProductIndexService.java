package com.example.ssauc.user.search.service;


import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.ProductRepository;
import com.example.ssauc.user.search.document.ProductDocument;
import com.example.ssauc.user.search.repository.ProductSearchRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductIndexService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    // 생성자 주입 방식 사용 (Spring 권장)
    public ProductIndexService(ProductRepository productRepository, ProductSearchRepository productSearchRepository) {
        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
    }

    //MySQL → Elasticsearch로 전체 데이터 색인 (애플리케이션 실행 시 자동 실행)
    @PostConstruct
    public void initIndex() {
        reindexAllProducts();
        System.out.println("✅ MySQL 데이터를 Elasticsearch에 색인 완료!");
    }

    // MySQL에서 모든 상품을 가져와 Elasticsearch에 저장
    public void reindexAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDocument> documents = products.stream()
                .map(this::convertToDocument)  // 🔵 변환 로직을 별도 메서드로 분리
                .collect(Collectors.toList());
        productSearchRepository.saveAll(documents);
    }

    //새로운 상품 추가 시 MySQL과 Elasticsearch에 저장
    public void saveProduct(Product product) {
        // MySQL에 저장
        productRepository.save(product);
        // Elasticsearch에 저장
        ProductDocument doc = convertToDocument(product);
        productSearchRepository.save(doc);
        System.out.println("✅ 새로운 상품 저장됨! (MySQL + Elasticsearch)");
    }

    //상품 삭제 시 MySQL과 Elasticsearch에서도 삭제
    public void deleteProduct(Long productId) {
        // MySQL에서 삭제
        productRepository.deleteById(productId);
        // Elasticsearch에서 삭제
        productSearchRepository.deleteById(String.valueOf(productId));
        System.out.println(" 상품 삭제됨! (MySQL + Elasticsearch)");
    }


    //  MySQL 상품 데이터를 Elasticsearch 문서로 변환하는 메서드
    private ProductDocument convertToDocument(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setProductId(product.getProductId().toString());
        doc.setName(product.getName());
        doc.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
        doc.setDescription(product.getDescription());
        doc.setPrice(product.getPrice());
        doc.setCreatedAt(product.getCreatedAt());
        doc.setUpdatedAt(product.getUpdatedAt());
        doc.setEndAt(product.getEndAt());
        doc.setViewCount(product.getViewCount());
        doc.setDealType(product.getDealType());
        doc.setBidCount(product.getBidCount());
        doc.setLikeCount(product.getLikeCount());
        return doc;
    }
}