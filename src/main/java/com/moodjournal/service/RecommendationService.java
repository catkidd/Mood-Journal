package com.moodjournal.service;

import com.moodjournal.model.Recommendation;
import com.moodjournal.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Random;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    @Autowired
    private RecommendationRepository recommendationRepository;

    private final Random random = new Random();

    /**
     * Returns a random recommendation for the given mood.
     * Falls back to a default if none found in DB.
     */
    public Recommendation getRandomByMood(String mood) {
        List<Recommendation> recs = recommendationRepository.findByMood(mood.toUpperCase());
        if (!recs.isEmpty()) {
            return recs.get(random.nextInt(recs.size()));
        }
        // Hardcoded fallback
        Recommendation fallback = new Recommendation();
        fallback.setMood(mood);
        fallback.setSuggestion(getFallbackSuggestion(mood));
        return fallback;
    }

    public List<Recommendation> getAllByMood(String mood) {
        return recommendationRepository.findByMood(mood.toUpperCase());
    }

    private String getFallbackSuggestion(String mood) {
        return switch (mood.toUpperCase()) {
            case "HAPPY"    -> "🎵 Keep spreading your positivity — it's contagious!";
            case "SAD"      -> "🌿 It's okay to feel sad. Be gentle with yourself today.";
            case "STRESSED" -> "🧘 Take a deep breath. One step at a time.";
            case "CALM"     -> "📚 Enjoy this peaceful moment — you deserve it.";
            case "ANGRY"    -> "🏃 Channel your energy into something productive.";
            case "ANXIOUS"  -> "🌸 Ground yourself: name 5 things you can see right now.";
            case "EXCITED"  -> "🎯 Use that energy to tackle something you've been putting off!";
            case "GRATEFUL" -> "💌 Write down what you're grateful for to preserve this feeling.";
            default         -> "🌟 Take a moment for yourself today — you matter.";
        };
    }
}
