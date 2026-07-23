# Mood Journal Web Application - UI/UX Design

## Overall Design
- **Theme**: Clean, minimal, calming (soft colors like light blue, pastel green, lavender).
- **Framework**: Bootstrap 5 for responsive design.
- **Layout**: Consistent navbar, card-based dashboard, and form-driven pages.

---

## Page Designs

### 1. Login & Registration
- Centered card with Bootstrap form.
- Clear error/success messages (alerts).
- Minimal distractions, focus on authentication.

### 2. Dashboard
- Navbar with links: Dashboard | Add Entry | View Entries | Mood Summary | Badges | Export | Logout.
- Quick summary cards:
  - Today’s mood.
  - Current streak.
  - Badge highlights.
- Motivational quote or recommendation displayed prominently.

### 3. Add/Edit Entry
- Bootstrap form with:
  - Date picker.
  - Mood dropdown (Happy, Sad, Stressed, etc.).
  - Text area for journal entry.
  - Optional tags input (chips or comma-separated).
- Save/Cancel buttons styled clearly.

### 4. View Entries
- Bootstrap table with pagination.
- Columns: Date | Mood | Tags | Text | Actions (Edit/Delete).
- Search bar + filter by mood/tag.

### 5. Mood Summary
- Chart.js integration:
  - Pie chart for mood distribution.
  - Line chart for mood trends over time.
- Weekly summary card with counts.

### 6. Badges
- Grid of Bootstrap cards showing earned badges.
- Each badge has icon + description (e.g., “7-Day Streak”).

### 7. Export
- Button to export entries (PDF/CSV).
- Confirmation alert after download.

---

## UX Principles
- **Consistency**: Same navbar and footer across all pages.
- **Feedback**: Alerts for success/error (e.g., “Entry saved successfully”).
- **Accessibility**: Large buttons, readable fonts, mobile-friendly.
- **Engagement**: Motivational quotes, badges, and charts keep users coming back.
