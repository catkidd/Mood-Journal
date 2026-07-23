package com.moodjournal.controller;

import com.moodjournal.model.Badge;
import com.moodjournal.model.User;
import com.moodjournal.model.UserBadge;
import com.moodjournal.service.BadgeService;
import com.moodjournal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class BadgeController {

    @Autowired private UserService userService;
    @Autowired private BadgeService badgeService;

    @GetMapping("/badges")
    public String badgesPage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());

        List<Badge> allBadges = badgeService.getAllBadges();
        List<UserBadge> earnedBadges = badgeService.getUserBadges(user);

        Set<Long> earnedBadgeIds = earnedBadges.stream()
                .map(ub -> ub.getBadge().getId())
                .collect(Collectors.toSet());

        model.addAttribute("allBadges", allBadges);
        model.addAttribute("earnedBadges", earnedBadges);
        model.addAttribute("earnedBadgeIds", earnedBadgeIds);
        model.addAttribute("earnedCount", earnedBadges.size());
        model.addAttribute("totalCount", allBadges.size());
        return "badges";
    }
}
