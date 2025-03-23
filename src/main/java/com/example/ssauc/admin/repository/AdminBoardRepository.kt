package com.example.ssauc.admin.repository

import com.example.ssauc.user.contact.entity.Board
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface AdminBoardRepository : JpaRepository<Board?, Long?> {
    @EntityGraph(attributePaths = ["user"])
    override fun findAll(pageable: Pageable): Page<Board?>

    @Transactional
    @Modifying
    @Query("UPDATE Board b SET b.status = :status WHERE b.boardId = :boardId")
    fun updateBoardByBoardId(@Param("status") status: String?, @Param("boardId") boardId: Long?): Int
}