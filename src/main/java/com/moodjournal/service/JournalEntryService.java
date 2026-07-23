package com.moodjournal.service;

import com.moodjournal.model.JournalEntry;
import com.moodjournal.model.User;
import com.moodjournal.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private BadgeService badgeService;

    // ===== CRUD =====

    public JournalEntry createEntry(JournalEntry entry, User user) {
        entry.setUser(user);
        JournalEntry saved = journalEntryRepository.save(entry);
        // Trigger badge check after save
        badgeService.checkAndAwardBadges(user);
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<JournalEntry> getEntryById(Long id, User user) {
        return journalEntryRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()));
    }

    public JournalEntry updateEntry(Long id, JournalEntry updated, User user) {
        JournalEntry existing = getEntryById(id, user)
                .orElseThrow(() -> new RuntimeException("Entry not found or access denied"));
        existing.setDate(updated.getDate());
        existing.setMood(updated.getMood());
        existing.setText(updated.getText());
        existing.setTags(updated.getTags());
        return journalEntryRepository.save(existing);
    }

    public void deleteEntry(Long id, User user) {
        JournalEntry entry = getEntryById(id, user)
                .orElseThrow(() -> new RuntimeException("Entry not found or access denied"));
        journalEntryRepository.delete(entry);
    }

    // ===== Query Methods =====

    @Transactional(readOnly = true)
    public Page<JournalEntry> searchEntries(User user, String keyword, String mood, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String m = (mood != null && !mood.isBlank()) ? mood.trim() : null;
        return journalEntryRepository.searchEntries(user, m, kw, pageable);
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> getRecentEntries(User user, int count) {
        Pageable pageable = PageRequest.of(0, count);
        return journalEntryRepository.findRecentByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> getAllEntries(User user) {
        return journalEntryRepository.findByUserOrderByDateDesc(user);
    }

    @Transactional(readOnly = true)
    public Optional<JournalEntry> getTodaysEntry(User user) {
        List<JournalEntry> entries = journalEntryRepository.findByUserAndDate(user, LocalDate.now());
        return entries.isEmpty() ? Optional.empty() : Optional.of(entries.get(0));
    }

    @Transactional(readOnly = true)
    public long countEntries(User user) {
        return journalEntryRepository.countByUser(user);
    }

    // ===== Streak Calculation =====

    @Transactional(readOnly = true)
    public int calculateStreak(User user) {
        List<LocalDate> dates = journalEntryRepository.findDistinctDatesByUserOrderByDateDesc(user);
        if (dates.isEmpty()) return 0;

        Set<LocalDate> dateSet = new HashSet<>(dates);
        LocalDate today = LocalDate.now();

        // Streak must include today or yesterday to be considered active
        LocalDate startDate;
        if (dateSet.contains(today)) {
            startDate = today;
        } else if (dateSet.contains(today.minusDays(1))) {
            startDate = today.minusDays(1);
        } else {
            return 0;
        }

        int streak = 0;
        LocalDate checkDate = startDate;
        while (dateSet.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        return streak;
    }

    // ===== Analytics =====

    @Transactional(readOnly = true)
    public Map<String, Long> getMoodDistribution(User user) {
        List<Object[]> results = journalEntryRepository.getMoodCounts(user);
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : results) {
            distribution.put((String) row[0], (Long) row[1]);
        }
        return distribution;
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Long> getDailyEntryCounts(User user, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<Object[]> results = journalEntryRepository.getDailyEntryCounts(user, startDate);

        // Build a map with all days initialized to 0
        Map<LocalDate, Long> countMap = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            countMap.put(startDate.plusDays(i), 0L);
        }
        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Long count = (Long) row[1];
            countMap.put(date, count);
        }
        return countMap;
    }

    @Transactional(readOnly = true)
    public Set<String> getDistinctMoods(User user) {
        return journalEntryRepository.findDistinctMoodsByUser(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWeeklyStats(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);
        List<JournalEntry> weekEntries = journalEntryRepository.findByUserAndDateBetweenOrderByDateDesc(user, weekAgo, today);

        Map<String, Long> moodCounts = new HashMap<>();
        for (JournalEntry e : weekEntries) {
            moodCounts.merge(e.getMood(), 1L, Long::sum);
        }
        String mostCommonMood = moodCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        Map<String, Object> stats = new HashMap<>();
        stats.put("count", weekEntries.size());
        stats.put("mostCommonMood", mostCommonMood);
        return stats;
    }
}
