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
    public String recordPlay(User user, String gameName) {
        // Persist the play record
        userGameRepository.save(new UserGame(user, gameName, LocalDateTime.now()));

        // Check badges and return the first newly awarded badge name (if any)
        return checkAndAwardGameBadges(user, gameName);
    }

    /**
     * Evaluates game-related badge criteria.
     * @return newly awarded badge display name, or null.
     */
    private String checkAndAwardGameBadges(User user, String gameName) {
        long totalPlays = userGameRepository.countByUser(user);

        // GAME_EXPLORER — 1st play
        if (totalPlays >= 1) {
            String awarded = awardIfNotEarned(user, "GAME_EXPLORER");
            if (awarded != null) return awarded;
        }

        // PUZZLE_MASTER — 5 total plays
        if (totalPlays >= 5) {
            String awarded = awardIfNotEarned(user, "PUZZLE_MASTER");
            if (awarded != null) return awarded;
        }

        // ARCADE_CHAMPION — 10 total plays
        if (totalPlays >= 10) {
            String awarded = awardIfNotEarned(user, "ARCADE_CHAMPION");
            if (awarded != null) return awarded;
        }

        // BREATHING_ZEN — 3 breathing sessions
        if ("breathing".equals(gameName)) {
            long breathingCount = userGameRepository.countByUserAndGameName(user, "breathing");
            if (breathingCount >= 3) {
                String awarded = awardIfNotEarned(user, "BREATHING_ZEN");
                if (awarded != null) return awarded;
            }
        }

        // SNAKE_MASTER — 3 snake plays
        if ("snake".equals(gameName)) {
            long snakeCount = userGameRepository.countByUserAndGameName(user, "snake");
            if (snakeCount >= 3) {
                String awarded = awardIfNotEarned(user, "SNAKE_MASTER");
                if (awarded != null) return awarded;
            }
        }

        return null;
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

    /** Total number of games played by the user. */
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
