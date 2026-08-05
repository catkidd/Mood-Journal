package com.moodjournal.config;

import com.moodjournal.model.Badge;
import com.moodjournal.model.Recommendation;
import com.moodjournal.repository.BadgeRepository;
import com.moodjournal.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Seeds the database with initial badges and recommendations on first run.
 * Uses count checks to avoid re-inserting on subsequent starts.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Override
    public void run(String... args) throws Exception {
        initBadges();
        initGameBadges();
        initRecommendations();
    }

    private void initBadges() {
        if (badgeRepository.count() == 0) {
            List<Badge> badges = List.of(
                new Badge("🌱 First Entry",   "Logged your very first mood entry!",                    "FIRST_ENTRY"),
                new Badge("🔥 7-Day Streak",  "Logged mood entries for 7 consecutive days!",           "STREAK_7"),
                new Badge("📝 10 Entries",    "Logged 10 journal entries — keep it up!",               "ENTRIES_10"),
                new Badge("🏆 30 Entries",    "Amazing! You have logged 30 journal entries!",          "ENTRIES_30"),
                new Badge("🎭 Mood Explorer", "Experienced and logged 5 different mood types!",        "ALL_MOODS")
            );
            badgeRepository.saveAll(badges);
        }
    }

    /** Seeds game badges individually (safe to run even when other badges already exist). */
    private void initGameBadges() {
        // Overall Activity Badges
        seedBadgeIfMissing("🎮 Game Explorer", "Play your very first mini-game!",          "GAME_EXPLORER");
        seedBadgeIfMissing("🧩 Puzzle Master",  "Play 5 activities to unlock this badge!",  "PUZZLE_MASTER");
        seedBadgeIfMissing("👑 Arcade Champ",   "Play 10 total activities to unlock!",      "ARCADE_CHAMPION");
        seedBadgeIfMissing("🏆 Gaming Legend",  "Play 25 total activities — true master!", "ARCADE_LEGEND");

        // Snake Game Badges
        seedBadgeIfMissing("🐍 Snake Charmer",  "Play 3 Snake games to unlock this!",      "SNAKE_MASTER");
        seedBadgeIfMissing("⚡ Slither Speedster","Score 10+ points in a Snake game!",     "SNAKE_HIGH_10");
        seedBadgeIfMissing("👑 Serpent King",   "Score 20+ points in a Snake game!",        "SNAKE_HIGH_20");

        // Tic-Tac-Toe Badges
        seedBadgeIfMissing("❌ Tactician",      "Play 3 Tic-Tac-Toe matches!",              "TTT_MASTER");
        seedBadgeIfMissing("🤖 Grandmaster",    "Defeat the AI in Tic-Tac-Toe!",            "TTT_VICTORY");

        // Memory Match Badges
        seedBadgeIfMissing("🃏 Memory Novice",  "Play 3 Memory Match games!",              "MEM_MASTER");
        seedBadgeIfMissing("⚡ Mind Master",     "Solve Memory Match in under 45s!",        "MEM_SPEED");
        seedBadgeIfMissing("🎯 Flawless Recall","Solve Memory Match in under 12 moves!",   "MEM_FLAWLESS");

        // Breathing Exercise Badges
        seedBadgeIfMissing("🫁 Zen Master",     "Complete 3 deep breathing sessions!",      "BREATHING_ZEN");
        seedBadgeIfMissing("🧘 Nirvana Explorer","Complete 10 total breathing cycles!",     "BREATHING_CYCLES_10");
    }

    private void seedBadgeIfMissing(String name, String description, String criteria) {
        if (badgeRepository.findByCriteria(criteria).isEmpty()) {
            badgeRepository.save(new Badge(name, description, criteria));
        }
    }

    private void initRecommendations() {
        if (recommendationRepository.count() == 0) {
            List<Recommendation> recs = List.of(
                // HAPPY
                rec("HAPPY",    "🎵 Listen to your favorite upbeat playlist and dance like no one is watching!"),
                rec("HAPPY",    "📞 Share your joy with a close friend or family member today."),
                rec("HAPPY",    "📓 Write down 3 things you are grateful for to amplify this feeling."),
                // SAD
                rec("SAD",      "🚶 Take a gentle walk in nature to clear your mind and reset."),
                rec("SAD",      "🎬 Watch a comforting movie or TV show that brings you peace."),
                rec("SAD",      "💬 Reach out to someone you trust — you don't have to face this alone."),
                // STRESSED
                rec("STRESSED", "🧘 Try a 5-minute deep breathing exercise: inhale 4s, hold 4s, exhale 6s."),
                rec("STRESSED", "🤸 Stretch your body and take a 10-minute break from your desk."),
                rec("STRESSED", "📋 Write down your stressors and tackle them one small step at a time."),
                // CALM
                rec("CALM",     "📚 Curl up with a good book and enjoy this peaceful moment."),
                rec("CALM",     "🍵 Brew a cup of herbal tea and savor the quiet around you."),
                rec("CALM",     "🎨 Channel your calm energy into a creative activity you love."),
                // ANGRY
                rec("ANGRY",    "🔢 Count to 10 slowly and take 5 deep breaths before reacting."),
                rec("ANGRY",    "🏃 Go for a run or workout to release the built-up tension safely."),
                rec("ANGRY",    "✍️ Write your feelings down privately — it helps process anger safely."),
                // ANXIOUS
                rec("ANXIOUS",  "🌿 Try grounding: name 5 things you see, 4 you touch, 3 you hear."),
                rec("ANXIOUS",  "🎧 Listen to calming music or nature sounds to ease your mind."),
                rec("ANXIOUS",  "📱 Take a break from news and social media for the next few hours."),
                // EXCITED
                rec("EXCITED",  "🎯 Channel your excitement into a project or goal you care about!"),
                rec("EXCITED",  "🤝 Share your excitement with someone close — joy is contagious!"),
                rec("EXCITED",  "📔 Journal what is making you excited so you can look back on this!"),
                // GRATEFUL
                rec("GRATEFUL", "💌 Write a heartfelt thank-you note to someone who has helped you."),
                rec("GRATEFUL", "🌸 Take a mindful moment to appreciate the beauty around you."),
                rec("GRATEFUL", "📖 Start a gratitude journal if you haven't already — you will love it!")
            );
            recommendationRepository.saveAll(recs);
        }
    }

    private Recommendation rec(String mood, String suggestion) {
        return new Recommendation(mood, suggestion);
    }
}
