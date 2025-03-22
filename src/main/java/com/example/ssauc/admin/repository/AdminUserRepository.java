package com.example.ssauc.admin.repository;

import com.example.ssauc.user.login.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<Users, Long> {

  Page<Users> findAll(Pageable pageable);

  @Transactional
  @Modifying
  @Query("UPDATE Users u SET u.status = :status WHERE u.userId = :userId")
  int updateUsersByUserId(@Param("status") String status, @Param("userId") Long userId);


  List<Users> findByWarningCountGreaterThanEqual(int warningCount);

  Page<Users> findByUserNameContainingIgnoreCase(String keyword, Pageable pageable);
}