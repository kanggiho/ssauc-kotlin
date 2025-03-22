package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.contact.entity.Board;
import com.example.ssauc.user.contact.repository.BoardRepository;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    void testSaveAndFindBoard() {
        // 유저 생성 및 저장
        Users user = Users.builder()
                .userName("boardAuthor")
                .email("boardAuthor@example.com")
                .password("pass")
                .createdAt(LocalDateTime.now())
                .build();
        user = usersRepository.save(user);

        // Board 엔티티 생성 및 저장
        Board board = Board.builder()
                .user(user)
                .subject("테스트 게시글")
                .message("게시글 내용입니다.")
                .createdAt(LocalDateTime.now())
                .status("활성")
                .build();

        Board saved = boardRepository.save(board);

        // 저장된 Board 조회 및 검증
        Board found = boardRepository.findById(saved.getBoardId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getSubject()).isEqualTo("테스트 게시글");
        assertThat(found.getStatus()).isEqualTo("활성");
    }
}
