package com.example.ssauc.admin.service;

import com.example.ssauc.admin.repository.AdminProductRepository;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminProductService {

    @Autowired
    private AdminProductRepository adminProductRepository;


    public Page<Product> getProducts(int page, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        return adminProductRepository.findAll(PageRequest.of(page, 10, sort));
    }

    public Page<Product> searchProductsByName(String keyword, int page, String sortField, String sortDir) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.fromString(sortDir), sortField));
        return adminProductRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    public Product findProductById(Long productId) {
        return adminProductRepository.findById(productId).orElse(null);
    }

    @Transactional
    public void changeProductStatus(Long productId, String status) {
        Product product = adminProductRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("잘못된 상태 값입니다.");
        }

        product.setStatus(status);
        adminProductRepository.save(product);  // 상태 변경 후 저장
    }

    private boolean isValidStatus(String status) {
        return "판매중".equals(status) || "판매중지".equals(status);
    }


    public List<Product> findAllProducts() {
        return adminProductRepository.findAll();
    }
}