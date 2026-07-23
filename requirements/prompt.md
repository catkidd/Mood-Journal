# BUILD: Mood Journal Web App (Spring Boot)

## STACK
Backend: Spring Boot (Web, Data JPA, Security) | DB: MySQL | ORM: Hibernate/JPA
Frontend: Thymeleaf + Bootstrap 5 | Charts: Chart.js | Build: Maven
Arch: 3-tier (Controller→Service→Repository), REST controllers return JSON for Chart.js endpoints

## SCHEMA (JPA entities, MySQL)
users(id PK, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL [BCrypt], role VARCHAR(20) DEFAULT 'USER')
journal_entries(id PK, user_id FK→users, date DATE NOT NULL, mood VARCHAR(20) NOT NULL, text TEXT, tags VARCHAR(100))
badges(id PK, name VARCHAR(50) NOT NULL, description VARCHAR(255), criteria VARCHAR(100))
user_badges(id PK, user_id FK→users, badge_id FK→badges, date_awarded DATE) -- junction table
recommendations(id PK, mood VARCHAR(20) NOT NULL, suggestion VARCHAR(255) NOT NULL)

Relations: users 1:M journal_entries | users M:M badges (via user_badges) | journal_entries M:1 recommendations (matched by mood string, not FK)

## FEATURES (priority order)
1. Auth: Spring Security, BCrypt hashing, role-based (USER/ADMIN optional), register+login
2. Journal CRUD: entries scoped to logged-in user only; fields = date, mood(dropdown), text, tags(comma-sep)
3. Mood Summary: weekly/monthly stats, streak counter (consecutive days logged), Chart.js pie(distribution)+line(trend)
4. Recommendations: lookup by mood → quote/activity/playlist (DB-backed or hardcoded fallback)
5. Tagging/Search: filter entries by tag or keyword, search bar
6. Badges: auto-award on milestone triggers (e.g. "7-Day Streak", "10 Entries Logged"), display grid on dashboard
7. Export: entries → PDF/CSV download, success alert on completion

## PAGES (Thymeleaf, shared navbar+footer)
- Login/Register: centered card, alert-based error/success
- Dashboard: navbar[Dashboard|Add Entry|View Entries|Mood Summary|Badges|Export|Logout], summary cards(today's mood, streak, badge highlights), featured recommendation
- Add/Edit Entry: date picker, mood dropdown, textarea, tags input, Save/Cancel
- View Entries: paginated table(Date|Mood|Tags|Text|Actions), search+filter bar
- Mood Summary: Chart.js pie+line, weekly count card
- Badges: card grid, icon+description per badge
- Export: button → PDF/CSV, confirmation alert

## UX RULES
Palette: soft/pastel (light blue, pastel green, lavender) | Consistent nav+footer | Mobile-responsive, large tap targets | Alert feedback on every mutating action

## NON-FUNCTIONAL
- Passwords never plaintext; BCrypt only
- Unauthorized access → redirect to login
- Entries strictly scoped by user_id (no cross-user leakage)
- Charts fed via dedicated JSON REST endpoints, not server-rendered

## BUILD ORDER
1. Entities + repositories → 2. Security config + auth flow → 3. Journal CRUD (service+controller+templates) → 4. Streak/summary logic + Chart.js endpoints → 5. Recommendations lookup → 6. Badge award logic (trigger on entry save) → 7. Export → 8. UI polish/styling pass