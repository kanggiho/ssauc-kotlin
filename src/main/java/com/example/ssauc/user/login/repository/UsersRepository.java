package com.example.ssauc.user.login.repository;

import com.example.ssauc.user.login.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

  Optional<Users> findByEmail(String email);
  Optional<Users> findByUserName(String userName);
  Optional<Users> findByPhone(String phone);

  // active만 찾는 메서드 (예: findByUserNameAndStatus, findByEmailAndStatus)
  Optional<Users> findByEmailAndStatus(String email, String status);
  Optional<Users> findByUserNameAndPhoneAndStatus(String userName, String phone, String status);

  boolean existsByEmail(String email);
  boolean existsByUserName(String userName);
  boolean existsByPhone(String phone);

  @Transactional
  @Modifying
  @Query("update Users u set u.cash = u.cash + ?1 where u.userId = ?2")
  void addCash(Long cash, Long userId);

  @Transactional
  @Modifying
  @Query("update Users u set u.cash = u.cash - ?1 where u.userId = ?2")
  void minusCash(Long cash, Long userId);

  @Transactional
  @Modifying
  @Query("UPDATE Users u SET u.warningCount = u.warningCount + :warningCount WHERE u.userId = :userId")
  int updateUserByWarningCount(@Param("warningCount") int warningCount, @Param("userId") Long userId);

  List<Users> findByLastLoginBefore(LocalDateTime date);

}
