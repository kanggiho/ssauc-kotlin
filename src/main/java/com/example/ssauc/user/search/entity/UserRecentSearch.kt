package com.example.ssauc.user.search.entity

import com.example.ssauc.user.login.entity.Users
import jakarta.persistence.*
import lombok.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "user_recent_search")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
class UserRecentSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @ManyToOne
    @JoinColumn(name = "user_id")
    private val user: Users? = null

    @Column(name = "session_id")
    private var sessionId: String? = null

    @Column(nullable = false)
    private var keyword: String? = null

    @CreationTimestamp
    private var searchedAt: LocalDateTime? = null

    fun setSearchedAt(searchedAt: LocalDateTime?) {
        this.searchedAt = searchedAt
    }
}