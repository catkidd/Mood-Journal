package com.moodjournal.controller;

import com.moodjournal.dto.RegistrationForm;
import com.moodjournal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        // Validate password confirmation
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.form", "Passwords do not match.");
        }

        // Check username availability
        if (!result.hasFieldErrors("username") && userService.usernameExists(form.getUsername())) {
            result.rejectValue("username", "error.form", "Username is already taken.");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(form.getUsername(), form.getPassword());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue("username", "error.form", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
