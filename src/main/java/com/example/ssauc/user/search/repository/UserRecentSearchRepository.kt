package com.example.ssauc.user.search.repository;


import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.search.entity.UserRecentSearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRecentSearchRepository extends JpaRepository<UserRecentSearch, Long> {
  List<UserRecentSearch> findTop10ByUserOrderBySearchedAtDesc(Users user);
  // 동일 키워드에 해당하는 모든 레코드를 삭제하는 메서드
  void deleteByUserAndKeyword(Users user, String keyword);
  Optional<UserRecentSearch> findByUserAndKeyword(Users user, String keyword);
  // 기존 단일 검색어 조회 (원하는 경우 유지)
  List<UserRecentSearch> findAllByUserAndKeyword(Users user, String keyword);
}