package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.product.entity.Category;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.CategoryRepository;
import com.example.ssauc.user.product.repository.ProductRepository;
import com.example.ssauc.user.main.entity.ProductLike;
import com.example.ssauc.user.main.repository.ProductLikeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductLikeRepositoryTest {

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testSaveAndFindProductLike() {
        // 판매자(Users) 엔티티 생성 및 저장
        Users seller = new Users();
        seller.setUserName("sellerTest");
        seller.setEmail("sellerTest@example.com");
        seller.setPassword("password");
        seller.setCreatedAt(LocalDateTime.now());
        seller.setUpdatedAt(LocalDateTime.now());
        Users savedSeller = usersRepository.save(seller);

        // 카테고리(Category) 엔티티 생성 및 저장
        Category category = new Category();
        category.setName("categoryTest");
        Category savedCategory = categoryRepository.save(category);

        // 상품(Product) 엔티티 생성 및 저장
        Product product = new Product();
        product.setSeller(savedSeller);
        product.setCategory(savedCategory);
        product.setName("Test Product");
        product.setDescription("This is a test product.");
        product.setPrice(10000L);
        product.setImageUrl("http://example.com/product.jpg");
        product.setStatus("Available");
        product.setStartPrice(1000L);
        product.setCreatedAt(LocalDateTime.now());
        product.setEndAt(LocalDateTime.now());
        product.setViewCount(0L);
        Product savedProduct = productRepository.save(product);

        // ProductLike 엔티티 생성 및 저장
        ProductLike productLike = new ProductLike();
        productLike.setUser(savedSeller);
        productLike.setProduct(savedProduct);
        productLike.setLikedAt(LocalDateTime.now());
        ProductLike savedProductLike = productLikeRepository.save(productLike);

        // 저장된 ProductLike 조회 및 검증
        Optional<ProductLike> optionalProductLike = productLikeRepository.findById(savedProductLike.getLikeId());
        assertTrue(optionalProductLike.isPresent(), "저장된 ProductLike가 존재해야 합니다.");

        ProductLike foundProductLike = optionalProductLike.get();
        assertEquals(savedProductLike.getLikeId(), foundProductLike.getLikeId(), "ID가 일치해야 합니다.");
        assertEquals(savedSeller.getUserId(), foundProductLike.getUser().getUserId(), "판매자 ID가 일치해야 합니다.");
        assertEquals(savedProduct.getProductId(), foundProductLike.getProduct().getProductId(), "상품 ID가 일치해야 합니다.");
    }
}
