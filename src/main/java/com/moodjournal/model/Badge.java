package com.moodjournal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String criteria;

    public Badge() {}

    public Badge(String name, String description, String criteria) {
        this.name = name;
        this.description = description;
        this.criteria = criteria;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCriteria() { return criteria; }
    public void setCriteria(String criteria) { this.criteria = criteria; }

    @Override
    public String toString() {
        return "Badge{id=" + id + ", name='" + name + "', criteria='" + criteria + "'}";
    }
}
