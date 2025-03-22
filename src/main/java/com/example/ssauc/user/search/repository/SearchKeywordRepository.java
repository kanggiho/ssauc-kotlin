package com.example.ssauc.user.search.repository;


import com.example.ssauc.user.search.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, String> {
  Optional<SearchKeyword> findByKeyword(String keyword);
  List<SearchKeyword> findTop10ByOrderBySearchCountDesc();
}