# Mood Journal App - Database Schema

## Overview
This schema defines the relational structure for the Mood Journal App, supporting user authentication, journal entries, badges, and mood-based recommendations. It is optimized for MySQL and integrates seamlessly with Spring Boot via JPA/Hibernate.

---

## 🧱 Tables & Relationships

### 1. users
Stores user credentials and roles.

| Column | Type | Constraints | Description |
|---------|------|-------------|--------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | User login name |
| password | VARCHAR(255) | NOT NULL | Encrypted password |
| role | VARCHAR(20) | DEFAULT 'USER' | Access role |

**Relationships:**
- One-to-Many → `journal_entries`
- One-to-Many → `user_badges`

---

### 2. journal_entries
Stores daily mood logs and reflections.

| Column | Type | Constraints | Description |
|---------|------|-------------|--------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique entry identifier |
| user_id | INT | FOREIGN KEY REFERENCES users(id) | Linked user |
| date | DATE | NOT NULL | Entry date |
| mood | VARCHAR(20) | NOT NULL | Mood type |
| text | TEXT | NULL | Journal content |
| tags | VARCHAR(100) | NULL | Optional tags |

**Relationships:**
- Many-to-One → `users`
- Many-to-One → `recommendations` (via mood)

---

### 3. badges
Defines achievement badges and criteria.

| Column | Type | Constraints | Description |
|---------|------|-------------|--------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique badge identifier |
| name | VARCHAR(50) | NOT NULL | Badge name |
| description | VARCHAR(255) | NULL | Badge description |
| criteria | VARCHAR(100) | NULL | Award condition |

**Relationships:**
- Many-to-Many → `users` (via `user_badges`)

---

### 4. user_badges
Junction table linking users and badges.

| Column | Type | Constraints | Description |
|---------|------|-------------|--------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique record identifier |
| user_id | INT | FOREIGN KEY REFERENCES users(id) | Linked user |
| badge_id | INT | FOREIGN KEY REFERENCES badges(id) | Linked badge |
| date_awarded | DATE | NULL | Award date |

**Relationships:**
- Many-to-One → `users`
- Many-to-One → `badges`

---

### 5. recommendations
Stores mood-based suggestions.

| Column | Type | Constraints | Description |
|---------|------|-------------|--------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique recommendation identifier |
| mood | VARCHAR(20) | NOT NULL | Associated mood |
| suggestion | VARCHAR(255) | NOT NULL | Quote, activity, or playlist suggestion |

**Relationships:**
- One-to-Many → `journal_entries` (via mood)

---

## 🔗 Relationship Summary
| Relationship | Type | Description |
|---------------|------|-------------|
| users → journal_entries | 1:M | Each user can have multiple entries |
| users → user_badges | 1:M | Each user can earn multiple badges |
| badges → user_badges | 1:M | Each badge can be earned by multiple users |
| journal_entries → recommendations | M:1 | Each entry links to one recommendation |

---

## 🧠 Example Queries

### Insert a new journal entry
```sql
INSERT INTO journal_entries (user_id, date, mood, text, tags)
VALUES (1, '2026-07-23', 'Happy', 'Had a great day!', 'work,success');
