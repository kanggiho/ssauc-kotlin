package com.example.ssauc.repositoryTests;

import com.example.ssauc.user.chat.entity.ChatRoom;
import com.example.ssauc.user.chat.repository.ChatRoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Test
    void testSaveAndFindChatRoom() {
        // Given
        ChatRoom chatRoom = ChatRoom.builder()
                .createdAt(LocalDateTime.now())
                .build();

        // When
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        ChatRoom foundChatRoom = chatRoomRepository.findById(savedChatRoom.getChatRoomId()).orElse(null);

        // Then
        assertThat(foundChatRoom).isNotNull();
        assertThat(foundChatRoom.getChatRoomId()).isEqualTo(savedChatRoom.getChatRoomId());
    }
}
