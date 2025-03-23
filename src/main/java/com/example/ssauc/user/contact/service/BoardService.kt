package com.example.ssauc.user.contact.service

import com.example.ssauc.common.service.CommonUserService
import com.example.ssauc.user.contact.entity.Board
import com.example.ssauc.user.contact.repository.BoardRepository
import com.example.ssauc.user.login.entity.Users
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@RequiredArgsConstructor
class BoardService {
    private val boardRepository: BoardRepository? = null
    private val commonUserService: CommonUserService? = null

    fun getCurrentUser(email: String?): Users {
        return commonUserService!!.getCurrentUser(email)
    }


    // 1) 등록 (QnA 작성)
    fun createBoard(
        user: Users?,
        subject: String?,
        message: String?
    ): Board {
        val board: Board = Board.builder()
            .user(user) // 작성자
            .subject(subject)
            .message(message)
            .createdAt(LocalDateTime.now()) // 등록 시점
            .status("답변대기") // 기본 상태값 (필요시 수정)
            .build()
        return boardRepository!!.save(board)
    }

    val boardList: List<Board?>
        // 2) 목록 조회
        get() = boardRepository!!.findAll()

    // 3) 상세 조회
    fun getBoard(boardId: Long): Board {
        return boardRepository!!.findById(boardId)
            .orElseThrow {
                RuntimeException(
                    "게시글을 찾을 수 없습니다. ID=$boardId"
                )
            }!!
    } // 기타 수정, 삭제 등 필요 시 구현
}