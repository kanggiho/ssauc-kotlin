package com.example.ssauc.user.search.entity;

import com.example.ssauc.user.login.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_recent_search")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserRecentSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "session_id")
    private String sessionId;

    @Column(nullable = false)
    private String keyword;

    @CreationTimestamp
    private LocalDateTime searchedAt;

    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }
}