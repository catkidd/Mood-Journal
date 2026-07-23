# Mood Journal App - System Architecture

## Overview
The Mood Journal App follows a **three-tier architecture** integrating frontend, backend, and database layers. It is designed for modularity, scalability, and maintainability using **Spring Boot**, **MySQL**, **Thymeleaf**, **Bootstrap**, and **Chart.js**.

---

## 🧩 Architecture Layers

### 1. Frontend Layer
- **Technologies**: Thymeleaf + Bootstrap 5
- **Purpose**: Handles user interaction and presentation.
- **Components**:
  - Thymeleaf templates render dynamic HTML pages.
  - Bootstrap ensures responsive and visually appealing UI.
  - Chart.js visualizes mood trends and summaries.
- **Responsibilities**:
  - Display forms for login, registration, and journal entries.
  - Render mood analytics charts.
  - Provide feedback alerts and navigation.

### 2. Backend Layer
- **Technology**: Spring Boot Framework
- **Purpose**: Manages business logic, security, and data flow.
- **Components**:
  - **Controllers**: Handle HTTP requests and responses.
  - **Services**: Implement business logic (CRUD, summaries, badges).
  - **Repositories**: Interface with the database using JPA/Hibernate.
  - **Spring Security**: Manages authentication and authorization.
- **Responsibilities**:
  - Validate user credentials.
  - Process CRUD operations for journal entries.
  - Generate mood summaries and recommendations.
  - Award badges based on user activity.

### 3. Database Layer
- **Technology**: MySQL
- **Purpose**: Stores persistent data for users, entries, badges, and recommendations.
- **Components**:
  - Tables: `users`, `journal_entries`, `badges`, `user_badges`, `recommendations`.
  - ORM: JPA/Hibernate for mapping entities to tables.
- **Responsibilities**:
  - Maintain data integrity and relationships.
  - Support queries for analytics and summaries.

---

## 🔄 Data Flow
1. **User Interaction**
   - User submits requests via Thymeleaf forms.
   - Frontend sends HTTP requests to Spring Boot controllers.
2. **Backend Processing**
   - Controllers delegate tasks to services.
   - Services interact with repositories for data access.
3. **Database Operations**
   - Repositories perform CRUD operations using JPA/Hibernate.
   - Data retrieved or updated in MySQL.
4. **Response Rendering**
   - Backend sends processed data to Thymeleaf templates.
   - Frontend displays results, charts, and feedback.

---

## 📊 Analytics Integration
- **Chart.js** connects to backend endpoints that provide JSON data.
- Backend aggregates mood statistics and sends them to Chart.js.
- Charts displayed on the dashboard and summary pages.

---

## 🔐 Security Flow
1. User logs in via Spring Security.
2. Credentials verified using BCrypt password hashing.
3. Authorized users gain access to dashboard and CRUD features.
4. Unauthorized access redirected to login page.

---

## 🧠 Key Interactions
| Layer | Technology | Communication | Purpose |
|-------|-------------|---------------|----------|
| Frontend | Thymeleaf, Bootstrap | HTTP Requests | UI Rendering |
| Backend | Spring Boot, Spring Security | REST Controllers | Business Logic |
| Database | MySQL, JPA/Hibernate | SQL Queries | Data Persistence |
| Analytics | Chart.js | JSON Data | Visualization |

---

## 🖼 Architecture Diagram
![Mood Journal App - System Architecture](system_architecture_diagram.png)

---

## ⚙️ Summary
This architecture ensures:
- **Separation of concerns** between UI, logic, and data.
- **Scalability** for future features (e.g., AI-based mood prediction).
- **Security** through Spring Security and encrypted credentials.
- **User engagement** via interactive charts and badges.
