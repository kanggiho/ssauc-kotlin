package com.example.ssauc.admin.repository

import com.example.ssauc.admin.entity.Reply
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface AdminReplyRepository : JpaRepository<Reply?, Long?> {
    @Query("SELECT r FROM Reply r WHERE r.board.boardId = :boardId")
    fun findByBoardId(@Param("boardId") boardId: Long?): Optional<Reply?>?
}