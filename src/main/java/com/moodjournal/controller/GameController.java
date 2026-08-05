package com.moodjournal.controller;

import com.moodjournal.model.User;
import com.moodjournal.service.GameService;
import com.moodjournal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
public class GameController {

    @Autowired private UserService userService;
    @Autowired private GameService gameService;

    /**
     * Renders the games hub page.
     * @param highlight optional query param ("snake", "tictactoe", "memory") to auto-switch tab.
     */
    @GetMapping("/games")
    public String gamesPage(@RequestParam(required = false, defaultValue = "") String highlight,
                            Model model,
                            Principal principal) {
        User user = userService.findByUsername(principal.getName());

        long totalPlays             = gameService.getPlayCount(user);
        Map<String, Long> gameStats = gameService.getGameStats(user);
        java.util.Set<String> earned = gameService.getEarnedCriteriaSet(user);

        model.addAttribute("highlight",    highlight);
        model.addAttribute("totalPlays",   totalPlays);
        model.addAttribute("gameStats",    gameStats);
        model.addAttribute("earnedBadges", earned);

        return "games";
    }

    /**
     * AJAX endpoint called by the front-end when a game session ends.
     * Records the play and returns JSON indicating success + any new badge.
     */
    @PostMapping("/games/play")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordPlay(@RequestBody Map<String, Object> body,
                                                          Principal principal) {
        String gameName = String.valueOf(body.getOrDefault("game", "")).trim().toLowerCase();

        // Validate game name
        if (!GameService.VALID_GAMES.contains(gameName)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Unknown game: " + gameName));
        }

        User user = userService.findByUsername(principal.getName());
        String newBadge = gameService.recordPlay(user, gameName, body);

        return ResponseEntity.ok(Map.of(
                "success",  true,
                "newBadge", newBadge != null ? newBadge : ""
        ));
    }
}
