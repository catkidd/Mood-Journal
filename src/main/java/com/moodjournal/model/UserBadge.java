package com.moodjournal.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_badges")
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "date_awarded")
    private LocalDate dateAwarded;

    public UserBadge() {}

    public UserBadge(User user, Badge badge, LocalDate dateAwarded) {
        this.user = user;
        this.badge = badge;
        this.dateAwarded = dateAwarded;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Badge getBadge() { return badge; }
    public void setBadge(Badge badge) { this.badge = badge; }

    public LocalDate getDateAwarded() { return dateAwarded; }
    public void setDateAwarded(LocalDate dateAwarded) { this.dateAwarded = dateAwarded; }

    @Override
    public String toString() {
        return "UserBadge{id=" + id + ", badge=" + (badge != null ? badge.getName() : "null") + ", dateAwarded=" + dateAwarded + "}";
    }
}
