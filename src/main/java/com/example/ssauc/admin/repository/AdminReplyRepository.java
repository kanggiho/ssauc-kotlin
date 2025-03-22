package com.example.ssauc.admin.repository;

import com.example.ssauc.admin.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminReplyRepository extends JpaRepository<Reply, Long> {
    @Query("SELECT r FROM Reply r WHERE r.board.boardId = :boardId")
    Optional<Reply> findByBoardId(@Param("boardId") Long boardId);

}