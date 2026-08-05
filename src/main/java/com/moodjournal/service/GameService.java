package com.moodjournal.service;

import com.moodjournal.model.Badge;
import com.moodjournal.model.User;
import com.moodjournal.model.UserBadge;
import com.moodjournal.model.UserGame;
import com.moodjournal.repository.BadgeRepository;
import com.moodjournal.repository.UserBadgeRepository;
import com.moodjournal.repository.UserGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class GameService {

    @Autowired private UserGameRepository userGameRepository;
    @Autowired private BadgeRepository    badgeRepository;
    @Autowired private UserBadgeRepository userBadgeRepository;

    /** Valid game identifiers accepted from the front-end. */
    public static final List<String> VALID_GAMES = List.of("snake", "tictactoe", "memory", "breathing");

    /**
     * Records a completed game play and triggers badge checks.
     * @return the name of a newly awarded badge, or null if none was earned this call.
     */
    public String recordPlay(User user, String gameName, Map<String, Object> metrics) {
        // Persist the play record
        userGameRepository.save(new UserGame(user, gameName, LocalDateTime.now()));

        // Check badges and return the first newly awarded badge name (if any)
        return checkAndAwardGameBadges(user, gameName, metrics);
    }

    /**
     * Evaluates game-related badge criteria.
     * @return newly awarded badge display name, or null.
     */
    private String checkAndAwardGameBadges(User user, String gameName, Map<String, Object> metrics) {
        long totalPlays = userGameRepository.countByUser(user);

        // Overall Activity Badges
        if (totalPlays >= 1)  { String a = awardIfNotEarned(user, "GAME_EXPLORER");   if (a != null) return a; }
        if (totalPlays >= 5)  { String a = awardIfNotEarned(user, "PUZZLE_MASTER");   if (a != null) return a; }
        if (totalPlays >= 10) { String a = awardIfNotEarned(user, "ARCADE_CHAMPION"); if (a != null) return a; }
        if (totalPlays >= 25) { String a = awardIfNotEarned(user, "ARCADE_LEGEND");   if (a != null) return a; }

        // Snake Badges
        if ("snake".equals(gameName)) {
            long snakeCount = userGameRepository.countByUserAndGameName(user, "snake");
            if (snakeCount >= 3) { String a = awardIfNotEarned(user, "SNAKE_MASTER"); if (a != null) return a; }

            int score = getIntMetric(metrics, "score");
            if (score >= 10) { String a = awardIfNotEarned(user, "SNAKE_HIGH_10"); if (a != null) return a; }
            if (score >= 20) { String a = awardIfNotEarned(user, "SNAKE_HIGH_20"); if (a != null) return a; }
        }

        // Tic-Tac-Toe Badges
        if ("tictactoe".equals(gameName)) {
            long tttCount = userGameRepository.countByUserAndGameName(user, "tictactoe");
            if (tttCount >= 3) { String a = awardIfNotEarned(user, "TTT_MASTER"); if (a != null) return a; }

            String result = getStringMetric(metrics, "result");
            if ("win".equalsIgnoreCase(result) || "X".equalsIgnoreCase(result)) {
                String a = awardIfNotEarned(user, "TTT_VICTORY"); if (a != null) return a;
            }
        }

        // Memory Match Badges
        if ("memory".equals(gameName)) {
            long memCount = userGameRepository.countByUserAndGameName(user, "memory");
            if (memCount >= 3) { String a = awardIfNotEarned(user, "MEM_MASTER"); if (a != null) return a; }

            int moves = getIntMetric(metrics, "moves");
            int time  = getIntMetric(metrics, "time");
            if (moves > 0 && moves <= 12) { String a = awardIfNotEarned(user, "MEM_FLAWLESS"); if (a != null) return a; }
            if (time > 0 && time <= 45)   { String a = awardIfNotEarned(user, "MEM_SPEED");    if (a != null) return a; }
        }

        // Breathing Badges
        if ("breathing".equals(gameName)) {
            long breathCount = userGameRepository.countByUserAndGameName(user, "breathing");
            if (breathCount >= 3) { String a = awardIfNotEarned(user, "BREATHING_ZEN"); if (a != null) return a; }

            int cycles = getIntMetric(metrics, "cycles");
            if (cycles >= 5) { String a = awardIfNotEarned(user, "BREATHING_CYCLES_10"); if (a != null) return a; }
        }

        return null;
    }

    private int getIntMetric(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return 0;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }

    private String getStringMetric(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return "";
        return String.valueOf(map.get(key));
    }

    /**
     * Awards the badge with the given criteria if the user doesn't already have it.
     * @return the badge's display name if newly awarded, otherwise null.
     */
    private String awardIfNotEarned(User user, String criteria) {
        Optional<Badge> optBadge = badgeRepository.findByCriteria(criteria);
        if (optBadge.isEmpty()) return null;

        Badge badge = optBadge.get();
        if (!userBadgeRepository.existsByUserAndBadge(user, badge)) {
            userBadgeRepository.save(new UserBadge(user, badge, LocalDate.now()));
            return badge.getName();
        }
        return null;
    }

    /** Set of criteria strings for all badges earned by the user. */
    @Transactional(readOnly = true)
    public java.util.Set<String> getEarnedCriteriaSet(User user) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserOrderByDateAwardedDesc(user);
        return userBadges.stream()
                .map(ub -> ub.getBadge().getCriteria())
                .collect(java.util.stream.Collectors.toSet());
    }
    @Transactional(readOnly = true)
    public long getPlayCount(User user) {
        return userGameRepository.countByUser(user);
    }

    /**
     * Per-game play counts, e.g. {"snake": 3, "memory": 2, "tictactoe": 1}.
     * The repository returns Object[] rows: [0]=gameName, [1]=playCount.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getGameStats(User user) {
        Map<String, Long> stats = new HashMap<>();
        // Initialise all games to 0
        VALID_GAMES.forEach(g -> stats.put(g, 0L));

        List<Object[]> rows = userGameRepository.countByUserGroupByGame(user);
        for (Object[] row : rows) {
            String name  = (String) row[0];
            Long   count = (Long)   row[1];
            if (name != null && count != null) {
                stats.put(name, count);
            }
        }
        return stats;
    }
}
