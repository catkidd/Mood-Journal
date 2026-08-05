package com.moodjournal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Records a single game play session for a user.
 * Used for badge awarding (Game Explorer, Puzzle Master) and game stats.
 */
@Entity
@Table(name = "user_games", indexes = {
        @Index(name = "idx_ug_user", columnList = "user_id"),
        @Index(name = "idx_ug_game", columnList = "game_name")
})
public class UserGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "game_name", nullable = false, length = 50)
    private String gameName;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    public UserGame() {}

    public UserGame(User user, String gameName, LocalDateTime playedAt) {
        this.user = user;
        this.gameName = gameName;
        this.playedAt = playedAt;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }

    @Override
    public String toString() {
        return "UserGame{id=" + id + ", gameName='" + gameName + "', playedAt=" + playedAt + "}";
    }
}
