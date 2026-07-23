package com.moodjournal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String mood;

    @Column(nullable = false, length = 500)
    private String suggestion;

    public Recommendation() {}

    public Recommendation(String mood, String suggestion) {
        this.mood = mood;
        this.suggestion = suggestion;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    @Override
    public String toString() {
        return "Recommendation{id=" + id + ", mood='" + mood + "'}";
    }
}
