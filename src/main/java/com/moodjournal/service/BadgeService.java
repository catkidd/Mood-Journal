package com.moodjournal.service;

import com.moodjournal.model.Badge;
import com.moodjournal.model.User;
import com.moodjournal.model.UserBadge;
import com.moodjournal.repository.BadgeRepository;
import com.moodjournal.repository.JournalEntryRepository;
import com.moodjournal.repository.UserBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    /**
     * Checks all badge criteria and awards any newly earned badges.
     * Called automatically after each journal entry save.
     */
    public void checkAndAwardBadges(User user) {
        long totalEntries = journalEntryRepository.countByUser(user);
        int streak = calculateStreak(user);
        Set<String> distinctMoods = journalEntryRepository.findDistinctMoodsByUser(user);

        // FIRST_ENTRY: logged at least 1 entry
        if (totalEntries >= 1) {
            awardIfNotEarned(user, "FIRST_ENTRY");
        }
        // STREAK_7: 7 consecutive days
        if (streak >= 7) {
            awardIfNotEarned(user, "STREAK_7");
        }
        // ENTRIES_10: 10 total entries
        if (totalEntries >= 10) {
            awardIfNotEarned(user, "ENTRIES_10");
        }
        // ENTRIES_30: 30 total entries
        if (totalEntries >= 30) {
            awardIfNotEarned(user, "ENTRIES_30");
        }
        // ALL_MOODS: used 5 or more different mood types
        if (distinctMoods.size() >= 5) {
            awardIfNotEarned(user, "ALL_MOODS");
        }
    }

    private void awardIfNotEarned(User user, String criteria) {
        badgeRepository.findByCriteria(criteria).ifPresent(badge -> {
            if (!userBadgeRepository.existsByUserAndBadge(user, badge)) {
                UserBadge userBadge = new UserBadge(user, badge, LocalDate.now());
                userBadgeRepository.save(userBadge);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<UserBadge> getUserBadges(User user) {
        return userBadgeRepository.findByUserOrderByDateAwardedDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }

    // Inline streak calculation (to avoid circular dependency with JournalEntryService)
    private int calculateStreak(User user) {
        List<LocalDate> dates = journalEntryRepository.findDistinctDatesByUserOrderByDateDesc(user);
        if (dates.isEmpty()) return 0;

        Set<LocalDate> dateSet = new java.util.HashSet<>(dates);
        LocalDate today = LocalDate.now();

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
}
