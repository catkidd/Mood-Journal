package com.moodjournal.controller;

import com.moodjournal.model.User;
import com.moodjournal.service.JournalEntryService;
import com.moodjournal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class SummaryController {

    @Autowired private UserService userService;
    @Autowired private JournalEntryService journalEntryService;

    private static final Map<String, String> MOOD_COLORS = Map.of(
        "HAPPY",    "#6DBF6D",
        "SAD",      "#5B8FBF",
        "STRESSED", "#E07070",
        "CALM",     "#9B7DB5",
        "ANGRY",    "#E09050",
        "ANXIOUS",  "#BF7DB0",
        "EXCITED",  "#E0BF50",
        "GRATEFUL", "#50B090"
    );

    @GetMapping("/summary")
    public String summaryPage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        int streak = journalEntryService.calculateStreak(user);
        long total = journalEntryService.countEntries(user);
        Map<String, Object> weeklyStats = journalEntryService.getWeeklyStats(user);

        model.addAttribute("streak", streak);
        model.addAttribute("totalEntries", total);
        model.addAttribute("weeklyStats", weeklyStats);
        return "summary";
    }

    // ===== REST endpoints for Chart.js =====

    @GetMapping("/api/mood-stats")
    @ResponseBody
    public Map<String, Object> getMoodStats(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Map<String, Long> distribution = journalEntryService.getMoodDistribution(user);

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        distribution.forEach((mood, count) -> {
            labels.add(capitalize(mood));
            data.add(count);
            colors.add(MOOD_COLORS.getOrDefault(mood, "#AAAAAA"));
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        result.put("colors", colors);
        return result;
    }

    @GetMapping("/api/mood-trend")
    @ResponseBody
    public Map<String, Object> getMoodTrend(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Map<LocalDate, Long> dailyCounts = journalEntryService.getDailyEntryCounts(user, 30);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        dailyCounts.forEach((date, count) -> {
            labels.add(date.format(fmt));
            data.add(count);
        });

        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", "Entries per Day");
        dataset.put("data", data);
        dataset.put("borderColor", "#5B8FBF");
        dataset.put("backgroundColor", "rgba(91, 143, 191, 0.12)");
        dataset.put("fill", true);
        dataset.put("tension", 0.4);
        dataset.put("pointBackgroundColor", "#5B8FBF");
        dataset.put("pointRadius", 4);
        dataset.put("pointHoverRadius", 6);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("datasets", List.of(dataset));
        return result;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
