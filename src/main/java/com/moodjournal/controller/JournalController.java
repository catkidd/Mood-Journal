package com.moodjournal.controller;

import com.moodjournal.model.JournalEntry;
import com.moodjournal.model.User;
import com.moodjournal.service.JournalEntryService;
import com.moodjournal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/entries")
public class JournalController {

    @Autowired private UserService userService;
    @Autowired private JournalEntryService journalEntryService;

    private static final List<String> MOODS = List.of(
            "HAPPY", "SAD", "STRESSED", "CALM", "ANGRY", "ANXIOUS", "EXCITED", "GRATEFUL");

    // ===== List / Search =====

    @GetMapping
    public String listEntries(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String mood,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, Principal principal) {

        User user = userService.findByUsername(principal.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "id"));
        Page<JournalEntry> entryPage = journalEntryService.searchEntries(user, keyword, mood, pageable);

        model.addAttribute("entries", entryPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedMood", mood);
        model.addAttribute("moods", MOODS);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", entryPage.getTotalPages());
        return "journal/list";
    }

    // ===== Create =====

    @GetMapping("/new")
    public String newEntryForm(Model model) {
        JournalEntry entry = new JournalEntry();
        entry.setDate(LocalDate.now());
        model.addAttribute("entry", entry);
        model.addAttribute("moods", MOODS);
        model.addAttribute("formMode", "create");
        return "journal/form";
    }

    @PostMapping("/new")
    public String createEntry(@Valid @ModelAttribute("entry") JournalEntry entry,
                              BindingResult result, Model model, Principal principal,
                              RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("moods", MOODS);
            model.addAttribute("formMode", "create");
            return "journal/form";
        }
        User user = userService.findByUsername(principal.getName());
        journalEntryService.createEntry(entry, user);
        ra.addFlashAttribute("successMessage", "✅ Journal entry saved successfully!");
        return "redirect:/entries";
    }

    // ===== Edit =====

    @GetMapping("/{id}/edit")
    public String editEntryForm(@PathVariable Long id, Model model, Principal principal,
                                RedirectAttributes ra) {
        User user = userService.findByUsername(principal.getName());
        return journalEntryService.getEntryById(id, user)
                .map(entry -> {
                    model.addAttribute("entry", entry);
                    model.addAttribute("moods", MOODS);
                    model.addAttribute("formMode", "edit");
                    return "journal/form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Entry not found.");
                    return "redirect:/entries";
                });
    }

    @PostMapping("/{id}/edit")
    public String updateEntry(@PathVariable Long id,
                              @Valid @ModelAttribute("entry") JournalEntry entry,
                              BindingResult result, Model model, Principal principal,
                              RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("moods", MOODS);
            model.addAttribute("formMode", "edit");
            return "journal/form";
        }
        User user = userService.findByUsername(principal.getName());
        try {
            journalEntryService.updateEntry(id, entry, user);
            ra.addFlashAttribute("successMessage", "✅ Entry updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Could not update entry: " + e.getMessage());
        }
        return "redirect:/entries";
    }

    // ===== Delete =====

    @PostMapping("/{id}/delete")
    public String deleteEntry(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        User user = userService.findByUsername(principal.getName());
        try {
            journalEntryService.deleteEntry(id, user);
            ra.addFlashAttribute("successMessage", "🗑️ Entry deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Could not delete entry: " + e.getMessage());
        }
        return "redirect:/entries";
    }
}
