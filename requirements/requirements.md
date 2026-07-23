# Mood Journal Web Application - Functionalities

## Core Features
1. **Authentication**
   - User registration and login using Spring Security.
   - Password hashing with BCrypt.
   - Role-based access (user/admin optional).

2. **Journal Entries (CRUD)**
   - Add, view, update, delete entries.
   - Each entry includes: `date`, `mood`, `text`, optional `tags`.
   - Entries linked to logged-in user.

3. **Mood Summary**
   - Weekly/monthly mood statistics.
   - Streak tracking (e.g., consecutive days logged).
   - Display via Bootstrap tables/cards.

---

## Unique Features
1. **Mood-Driven Recommendations**
   - Suggest motivational quotes, calming activities, or playlists based on mood.
   - Recommendations stored in DB or hardcoded.

2. **Mood Trends Visualization**
   - Integrate Chart.js for:
     - Pie chart (mood distribution).
     - Line chart (mood trends over time).

3. **Tagging & Search**
   - Allow users to tag entries (e.g., "exam stress", "work").
   - Search/filter entries by tag or keyword.

4. **Positive Reinforcement Badges**
   - Award badges for milestones:
     - “7-Day Streak”
     - “10 Entries Logged”
   - Display badges on dashboard.

5. **Export & Backup**
   - Export entries to PDF or CSV.
   - Option to download summaries.

---

## Technical Stack
- **Backend**: Spring Boot (Web, Data JPA, Security).
- **Database**: MySQL.
- **Frontend**: Thymeleaf templates + Bootstrap styling.
- **Charts**: Chart.js integration.
- **Build Tool**: Maven or Gradle.
