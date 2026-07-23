package com.moodjournal.controller;

import com.moodjournal.model.JournalEntry;
import com.moodjournal.model.User;
import com.moodjournal.service.ExportService;
import com.moodjournal.service.JournalEntryService;
import com.moodjournal.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/export")
public class ExportController {

    @Autowired private UserService userService;
    @Autowired private JournalEntryService journalEntryService;
    @Autowired private ExportService exportService;

    @GetMapping
    public String exportPage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        long count = journalEntryService.countEntries(user);
        model.addAttribute("entryCount", count);
        return "export";
    }

    @GetMapping("/csv")
    public void exportCsv(HttpServletResponse response, Principal principal,
                          RedirectAttributes ra) throws Exception {
        User user = userService.findByUsername(principal.getName());
        List<JournalEntry> entries = journalEntryService.getAllEntries(user);

        String filename = "mood-journal-" + LocalDate.now() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        byte[] data = exportService.generateCsv(entries);
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    @GetMapping("/pdf")
    public void exportPdf(HttpServletResponse response, Principal principal) throws Exception {
        User user = userService.findByUsername(principal.getName());
        List<JournalEntry> entries = journalEntryService.getAllEntries(user);

        String filename = "mood-journal-" + LocalDate.now() + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        byte[] data = exportService.generatePdf(entries);
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }
}
