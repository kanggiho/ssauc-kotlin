package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.product.entity.Category;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.CategoryRepository;
import com.example.ssauc.user.product.repository.ProductRepository;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.main.repository.RecentlyViewedRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RecentlyViewedRepositoryTest {

    @Autowired
    private RecentlyViewedRepository recentlyViewedRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testSaveAndFindRecentlyViewed() {
        // 1. 판매자(Users) 엔티티 생성 및 저장
        Users user = new Users();
        user.setUserName("userTest");
        user.setEmail("userTest@example.com");
        user.setPassword("password");
        user.setLocation("seoul");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Users savedUser = usersRepository.save(user);

        // 카테고리(Category) 엔티티 생성 및 저장
        Category category = new Category();
        category.setName("categoryTest");
        Category savedCategory = categoryRepository.save(category);

        // 2. 상품(Product) 엔티티 생성 및 저장
        Product product = new Product();
        product.setSeller(savedUser);
        product.setCategory(savedCategory);
        product.setName("Sample Product");
        product.setDescription("This is a sample product.");
        product.setPrice(5000L);
        product.setImageUrl("http://example.com/product.jpg");
        product.setStatus("Available");
        product.setStartPrice(1000L);
        product.setCreatedAt(LocalDateTime.now());
        product.setEndAt(LocalDateTime.now());
        product.setViewCount(0L);
        Product savedProduct = productRepository.save(product);

        // 3. RecentlyViewed 엔티티 생성 및 저장
        RecentlyViewed recentlyViewed = new RecentlyViewed();
        recentlyViewed.setUser(savedUser);
        recentlyViewed.setProduct(savedProduct);
        recentlyViewed.setViewedAt(LocalDateTime.now());
        RecentlyViewed savedRecentlyViewed = recentlyViewedRepository.save(recentlyViewed);

        // 4. 저장된 RecentlyViewed 조회 및 검증
        Optional<RecentlyViewed> optionalRV = recentlyViewedRepository.findById(savedRecentlyViewed.getRecentlyId());
        assertTrue(optionalRV.isPresent(), "저장된 RecentlyViewed가 존재해야 합니다.");
        RecentlyViewed foundRV = optionalRV.get();
        assertEquals(savedUser.getUserId(), foundRV.getUser().getUserId(), "사용자 ID가 일치해야 합니다.");
        assertEquals(savedProduct.getProductId(), foundRV.getProduct().getProductId(), "상품 ID가 일치해야 합니다.");
    }
}
