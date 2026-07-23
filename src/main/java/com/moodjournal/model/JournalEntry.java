package com.moodjournal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate date;

    @NotBlank(message = "Please select a mood")
    @Column(nullable = false, length = 20)
    private String mood;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(length = 100)
    private String tags;

    public JournalEntry() {}

    public JournalEntry(User user, LocalDate date, String mood, String text, String tags) {
        this.user = user;
        this.date = date;
        this.mood = mood;
        this.text = text;
        this.tags = tags;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    @Override
    public String toString() {
        return "JournalEntry{id=" + id + ", mood='" + mood + "', date=" + date + "}";
    }
}
