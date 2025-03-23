package com.example.ssauc.user.contact.service;

import com.example.ssauc.common.service.CommonUserService;
import com.example.ssauc.user.contact.entity.Board;
import com.example.ssauc.user.contact.repository.BoardRepository;
import com.example.ssauc.user.login.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommonUserService commonUserService;

    public Users getCurrentUser(String email) {
        return commonUserService.getCurrentUser(email);
    }


    // 1) 등록 (QnA 작성)
    public Board createBoard(Users user,
                             String subject,
                             String message) {
        Board board = Board.builder()
                .user(user)               // 작성자
                .subject(subject)
                .message(message)
                .createdAt(LocalDateTime.now()) // 등록 시점
                .status("답변대기")          // 기본 상태값 (필요시 수정)
                .build();
        return boardRepository.save(board);
    }

    // 2) 목록 조회
    public List<Board> getBoardList() {
        return boardRepository.findAll();
    }

    // 3) 상세 조회
    public Board getBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID=" + boardId));
    }

    // 기타 수정, 삭제 등 필요 시 구현
}