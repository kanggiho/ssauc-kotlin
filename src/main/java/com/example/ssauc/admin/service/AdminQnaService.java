package com.example.ssauc.admin.service;

import com.example.ssauc.admin.dto.ReplyDto;
import com.example.ssauc.admin.entity.Admin;
import com.example.ssauc.admin.repository.AdminReplyRepository;
import com.example.ssauc.admin.entity.Reply;
import com.example.ssauc.admin.repository.AdminBoardRepository;
import com.example.ssauc.user.contact.entity.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminQnaService
{
    @Autowired
    private AdminBoardRepository adminBoardRepository;

    @Autowired
    private AdminReplyRepository adminReplyRepository;


    public Page<Board> getBoards(int page, String sortField, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        return adminBoardRepository.findAll(PageRequest.of(page, 10, sort));
    }

    public Board findBoardById(Long boardId) {
        return adminBoardRepository.findById(boardId).orElse(null);
    }

    public boolean updateBoardInfo(ReplyDto replyDto) {

        Board board = adminBoardRepository.findById(replyDto.getBoardId()).orElse(null);
        Admin admin = replyDto.getAdmin();

        // board 테이블의 답변상태 답변완료로 수정
        int updateResult = adminBoardRepository.updateBoardByBoardId("답변완료", replyDto.getBoardId());

        // reply 테이블에 builder로 데이터 추가 후 넣기
        Reply reply = Reply.builder()
                .board(board)
                .admin(admin)
                .subject(replyDto.getTitle())
                .message(replyDto.getContent())
                .completeAt(LocalDateTime.now())
                .build();

        adminReplyRepository.save(reply);

        return updateResult == 1;
    }

    public List<Board> findAllBoards() {
        return adminBoardRepository.findAll();
    }


    public Reply findReplyByBoardId(Long boardId) {
        return adminReplyRepository.findByBoardId(boardId).orElse(null);
    }
}