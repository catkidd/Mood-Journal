package com.moodjournal.controller;

import com.moodjournal.model.Recommendation;
import com.moodjournal.model.User;
import com.moodjournal.model.UserBadge;
import com.moodjournal.service.BadgeService;
import com.moodjournal.service.JournalEntryService;
import com.moodjournal.service.RecommendationService;
import com.moodjournal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired private UserService userService;
    @Autowired private JournalEntryService journalEntryService;
    @Autowired private RecommendationService recommendationService;
    @Autowired private BadgeService badgeService;

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());

        // Greeting
        int hour = java.time.LocalTime.now().getHour();
        String greeting = (hour < 12) ? "Morning" : (hour < 17) ? "Afternoon" : "Evening";
        model.addAttribute("greeting", greeting);

        // Today's mood
        String todaysMood = journalEntryService.getTodaysEntry(user)
                .map(e -> e.getMood())
                .orElse(null);
        model.addAttribute("todaysMood", todaysMood);

        // Streak
        int streak = journalEntryService.calculateStreak(user);
        model.addAttribute("streak", streak);

        // Total entries
        long totalEntries = journalEntryService.countEntries(user);
        model.addAttribute("totalEntries", totalEntries);

        // Badges
        List<UserBadge> userBadges = badgeService.getUserBadges(user);
        model.addAttribute("badgeCount", userBadges.size());
        model.addAttribute("recentBadges", userBadges.stream().limit(3).toList());

        // Recommendation (based on today's mood or default HAPPY)
        String moodForRec = (todaysMood != null) ? todaysMood : "HAPPY";
        Recommendation recommendation = recommendationService.getRandomByMood(moodForRec);
        model.addAttribute("recommendation", recommendation);

        // Recent journal entries (last 5)
        model.addAttribute("recentEntries", journalEntryService.getRecentEntries(user, 5));

        // Weekly stats
        model.addAttribute("weeklyStats", journalEntryService.getWeeklyStats(user));

        return "dashboard";
    }
}
