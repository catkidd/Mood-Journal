# 🌿 MoodJournal

A full-stack **Spring Boot** web application for tracking your daily mood, journaling your thoughts, earning achievement badges, and visualizing emotional trends over time.

---

## ✨ Features

| Feature | Details |
|---------|---------|
| 🔐 **Authentication** | Secure registration & login with BCrypt password hashing |
| 📓 **Journal Entries** | Create, read, update, delete entries with mood, notes & tags |
| 🔍 **Search & Filter** | Keyword search + mood filter with pagination |
| 📊 **Mood Summary** | Doughnut & line charts (Chart.js) for mood distribution and 30-day trends |
| 💡 **Recommendations** | Personalized daily suggestions based on your current mood |
| 🏆 **Achievement Badges** | Auto-awarded milestones (first entry, streaks, entry counts, mood variety) |
| 📥 **Export** | Download all entries as **PDF** (styled) or **CSV** (Excel-compatible) |
| 🔥 **Streak Tracking** | Tracks consecutive days of journaling |

---

## 🖥️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17 · Spring Boot 3.2.5 · Spring Security · Spring Data JPA |
| **Database** | MySQL 8+ · Hibernate (auto DDL) |
| **Templating** | Thymeleaf 3 + Thymeleaf Extras Spring Security |
| **Frontend** | Bootstrap 5.3 · Bootstrap Icons · Chart.js 4 · Vanilla CSS |
| **Export** | iText 5 (PDF) · OpenCSV (CSV) |
| **Build** | Maven 3 |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (JDK 17 or higher)
- **MySQL 8+** running locally
- **Maven 3.6+** (or use IntelliJ's bundled Maven)

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/mood-journal.git
cd mood-journal
```

### 2. Create the Database

```sql
CREATE DATABASE moodjournal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure Database Credentials

Edit [`src/main/resources/application.properties`](src/main/resources/application.properties):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/moodjournal
spring.datasource.username=root
spring.datasource.password=root
```

> **Default credentials** are `root / root`. Change these to match your MySQL setup.

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or in IntelliJ IDEA, click the **▶ Run** button on `MoodJournalApplication.java`.

### 5. Open in Browser

```
http://localhost:8080
```

> On first startup, the app automatically:
> - Creates all database tables via `ddl-auto=update`
> - Seeds **5 achievement badges**
> - Seeds **24 mood recommendations** (3 per mood type)

---

## 📁 Project Structure

```
mood-journal/
├── requirements/                   # Project specification documents
│   ├── prompt.md                   # Build prompt & requirements
│   ├── database_schema.md          # Database design
│   ├── design.md                   # UI/UX design spec
│   ├── requirements.md             # Functional requirements
│   ├── system_architecture.md      # Architecture overview
│   └── testing_and_security.md     # Testing & security spec
├── src/
│   └── main/
│       ├── java/com/moodjournal/
│       │   ├── MoodJournalApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── DataInitializer.java
│       │   ├── model/              # JPA Entities
│       │   ├── dto/                # Data Transfer Objects
│       │   ├── repository/         # Spring Data JPA Repositories
│       │   ├── service/            # Business Logic
│       │   └── controller/         # MVC + REST Controllers
│       └── resources/
│           ├── application.properties
│           ├── templates/          # Thymeleaf HTML templates
│           └── static/css/         # Custom stylesheet
├── pom.xml
└── README.md
```

---

## 🔐 Security

- All passwords are stored as **BCrypt hashes** — never plaintext
- Every authenticated route requires a valid session
- Journal entries are **user-scoped** — users can only access their own data
- CSRF protection is enabled via Spring Security (Thymeleaf injects tokens automatically)
- Session is fully invalidated on logout

---

## 🏆 Achievement Badges

| Badge | Criteria |
|-------|----------|
| 🌱 First Entry | Log your very first mood entry |
| 🔥 7-Day Streak | Log entries for 7 consecutive days |
| 📝 10 Entries | Accumulate 10 total journal entries |
| 🏆 30 Entries | Accumulate 30 total journal entries |
| 🎭 Mood Explorer | Use 5 different mood types in your entries |

---

## 🎨 Design

- **Palette**: Soft Blue `#5B8FBF` · Pastel Green `#7DBD7D` · Lavender `#9B7DB5`
- **Typography**: [Inter](https://fonts.google.com/specimen/Inter) via Google Fonts
- **UI Library**: Bootstrap 5.3 with custom component overrides
- Fully responsive — works on desktop, tablet, and mobile

---

## 📤 Export Formats

| Format | Description |
|--------|-------------|
| **PDF** | Styled A4 report with alternating row colors, mood badges, and a summary header |
| **CSV** | UTF-8 BOM encoded (Excel-friendly), includes Date, Mood, Tags, and Notes |

---

## 🛣️ API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/dashboard` | Main dashboard |
| `GET` | `/entries` | Paginated entry list (supports `?keyword=&mood=&page=`) |
| `GET/POST` | `/entries/new` | Create entry |
| `GET/POST` | `/entries/{id}/edit` | Edit entry |
| `POST` | `/entries/{id}/delete` | Delete entry |
| `GET` | `/summary` | Mood summary page |
| `GET` | `/api/mood-stats` | JSON — mood distribution (for Chart.js doughnut) |
| `GET` | `/api/mood-trend` | JSON — 30-day trend (for Chart.js line) |
| `GET` | `/badges` | Badges gallery |
| `GET` | `/export` | Export page |
| `GET` | `/export/pdf` | Download PDF |
| `GET` | `/export/csv` | Download CSV |
| `GET/POST` | `/register` | Registration |
| `GET` | `/login` | Login page |
| `POST` | `/logout` | Logout |

---

## 📋 Requirements Documents

All project specification files are located in the [`requirements/`](requirements/) folder:

- [`prompt.md`](requirements/prompt.md) — Build prompt and feature specifications
- [`database_schema.md`](requirements/database_schema.md) — Database design
- [`design.md`](requirements/design.md) — UI/UX design decisions
- [`requirements.md`](requirements/requirements.md) — Functional requirements
- [`system_architecture.md`](requirements/system_architecture.md) — System architecture
- [`testing_and_security.md`](requirements/testing_and_security.md) — Testing & security considerations

---

## 📝 License

This project is for educational purposes. Feel free to use and modify it.

---

> *Track your emotions, grow your mind.* 🌿
