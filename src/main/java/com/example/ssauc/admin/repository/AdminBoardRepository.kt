package com.example.ssauc.admin.repository;

import com.example.ssauc.user.chat.entity.Report;
import com.example.ssauc.user.contact.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface AdminBoardRepository extends JpaRepository<Board, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<Board> findAll(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Board b SET b.status = :status WHERE b.boardId = :boardId")
    int updateBoardByBoardId(@Param("status") String status, @Param("boardId") Long boardId);
}