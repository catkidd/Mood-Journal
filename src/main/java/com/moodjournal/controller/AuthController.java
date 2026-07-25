package com.moodjournal.controller;

import com.moodjournal.dto.RegistrationForm;
import com.moodjournal.model.PasswordResetToken;
import com.moodjournal.service.PasswordResetService;
import com.moodjournal.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    // ── Authentication ────────────────────────────────────────────────────

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
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.form", "Passwords do not match.");
        }
        if (!result.hasFieldErrors("username") && userService.usernameExists(form.getUsername())) {
            result.rejectValue("username", "error.form", "Username is already taken.");
        }
        if (!result.hasFieldErrors("email") && userService.emailExists(form.getEmail())) {
            result.rejectValue("email", "error.form", "An account with this email already exists.");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form.getUsername(), form.getEmail(), form.getPassword());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue("username", "error.form", e.getMessage());
            return "auth/register";
        }
    }

    // ── Password Reset ────────────────────────────────────────────────────

    /** Step 1 — Show the "enter your email" form. */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    /**
     * Step 2 — Accept the submitted email and dispatch a reset link.
     *
     * The response is intentionally ambiguous: we always show the same success
     * message regardless of whether the email was found (anti-enumeration).
     */
    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam("email") String email,
                                       RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.initiatePasswordReset(email.trim().toLowerCase());
        } catch (Exception e) {
            // Log the technical error internally but surface a generic message
            log.error("Error initiating password reset: {}", e.getMessage());
        }
        // Always redirect with the same message — prevents user enumeration
        redirectAttributes.addFlashAttribute("successMessage",
                "If an account exists for that email address, a reset link has been sent. " +
                "Please check your inbox (and spam folder).");
        return "redirect:/login";
    }

    /**
     * Step 3 — Validate the token and show the "choose new password" form.
     * Token is read from the query parameter but NEVER put into the model as plain text
     * (it only flows as a hidden field in the subsequent POST).
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        try {
            PasswordResetToken prt = passwordResetService.validateToken(token);
            model.addAttribute("token", prt.getToken()); // passed to the hidden form field
        } catch (IllegalArgumentException e) {
            model.addAttribute("tokenError", e.getMessage());
        }
        return "auth/reset-password";
    }

    /**
     * Step 4 — Accept the new password, update it, invalidate the token,
     * then redirect to login.
     */
    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Passwords do not match. Please try again.");
            return "auth/reset-password";
        }
        if (password.length() < 8) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Password must be at least 8 characters long.");
            return "auth/reset-password";
        }
        try {
            passwordResetService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your password has been reset successfully. Please sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("tokenError", e.getMessage());
            return "auth/reset-password";
        }
    }

    // ── Error pages ───────────────────────────────────────────────────────

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
