package com.example.ssauc.user.contact.repository;

import com.example.ssauc.user.contact.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    // 기본 CRUD 메서드는 JpaRepository가 제공
}
