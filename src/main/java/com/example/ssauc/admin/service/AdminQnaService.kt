package com.example.ssauc.admin.service

import com.example.ssauc.admin.dto.ReplyDto
import com.example.ssauc.admin.entity.Admin
import com.example.ssauc.admin.entity.Reply
import com.example.ssauc.admin.repository.AdminBoardRepository
import com.example.ssauc.admin.repository.AdminReplyRepository
import com.example.ssauc.user.contact.entity.Board
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AdminQnaService {
    @Autowired
    private val adminBoardRepository: AdminBoardRepository? = null

    @Autowired
    private val adminReplyRepository: AdminReplyRepository? = null


    fun getBoards(page: Int, sortField: String, sortDir: String): Page<Board?> {
        val sort = Sort.by(Sort.Direction.fromString(sortDir), sortField)
        return adminBoardRepository!!.findAll(PageRequest.of(page, 10, sort))
    }

    fun findBoardById(boardId: Long): Board? {
        return adminBoardRepository!!.findById(boardId).orElse(null)
    }

    fun updateBoardInfo(replyDto: ReplyDto): Boolean {
        val board = adminBoardRepository!!.findById(replyDto.getBoardId()).orElse(null)
        val admin: Admin = replyDto.getAdmin()

        // board 테이블의 답변상태 답변완료로 수정
        val updateResult = adminBoardRepository.updateBoardByBoardId("답변완료", replyDto.getBoardId())

        // reply 테이블에 builder로 데이터 추가 후 넣기
        val reply: Reply = Reply.builder()
            .board(board)
            .admin(admin)
            .subject(replyDto.getTitle())
            .message(replyDto.getContent())
            .completeAt(LocalDateTime.now())
            .build()

        adminReplyRepository!!.save(reply)

        return updateResult == 1
    }

    fun findAllBoards(): List<Board?> {
        return adminBoardRepository!!.findAll()
    }


    fun findReplyByBoardId(boardId: Long?): Reply? {
        return adminReplyRepository!!.findByBoardId(boardId)!!.orElse(null)
    }
}