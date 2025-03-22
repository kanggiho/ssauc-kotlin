package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.product.entity.Category;
import com.example.ssauc.user.product.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {


    @Autowired
    private UsersRepository usersRepository; // 판매자(Users) 엔티티 관련 Repository

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testSaveAndFindCategory() {

        // 판매자(Users) 엔티티 생성 및 저장
        Users user = new Users();
        user.setUserName("seller1");
        user.setEmail("seller1@example.com");
        user.setPassword("password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Users savedUser = usersRepository.save(user);



        Category category = new Category();
        category.setName("Electronics");
        Category savedCategory = categoryRepository.save(category);
        Category foundCategory = categoryRepository.findById(savedCategory.getCategoryId()).orElse(null);

        assertThat(foundCategory).isNotNull();
        assertThat(foundCategory.getCategoryId()).isEqualTo(savedCategory.getCategoryId());
        assertThat(foundCategory.getName()).isEqualTo("Electronics");
    }
}
