package com.moodjournal.service;

import com.moodjournal.model.PasswordResetToken;
import com.moodjournal.model.User;
import com.moodjournal.repository.PasswordResetTokenRepository;
import com.moodjournal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orchestrates the full password-reset lifecycle:
 * token creation → email dispatch → token validation → password update.
 *
 * Security invariants:
 *  - Tokens expire after TOKEN_TTL_MINUTES minutes (default 30).
 *  - Tokens are single-use: the {@code used} flag is set to {@code true} on consumption.
 *  - Any pre-existing tokens for the same user are deleted before a new one is issued
 *    (prevents token accumulation / replay risk).
 *  - Raw tokens are NEVER written to any log.
 */
@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_TTL_MINUTES = 30;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private EmailService emailService;

    @Autowired
    @Lazy
    private BCryptPasswordEncoder passwordEncoder;

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Initiates a password-reset request for the given email address.
     *
     * <p>If no user is found for the email, the method returns silently —
     * this prevents user enumeration (callers cannot distinguish "email found"
     * vs "email not found" from the HTTP response).
     *
     * @param email the address to send the reset link to
     */
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Invalidate any existing tokens for this user
            tokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES);
            tokenRepository.save(new PasswordResetToken(token, user, expiry));

            String resetLink = baseUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(email, resetLink);

            log.info("Password reset token issued for user id={}", user.getId());
        });
    }

    /**
     * Validates a reset token, throwing descriptive exceptions for each failure mode.
     *
     * @param token the raw UUID token from the query parameter
     * @return the valid, unexpired, unused {@link PasswordResetToken}
     * @throws IllegalArgumentException if the token is not found, has expired, or was already used
     */
    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(String token) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or unknown reset token."));

        if (prt.isUsed()) {
            throw new IllegalArgumentException("This reset link has already been used.");
        }
        if (prt.isExpired()) {
            throw new IllegalArgumentException(
                    "This reset link has expired. Please request a new one.");
        }
        return prt;
    }

    /**
     * Resets the user's password after validating the token.
     *
     * @param token       the raw UUID token
     * @param newPassword the plain-text new password (will be BCrypt-encoded)
     * @throws IllegalArgumentException if token validation fails
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = validateToken(token);

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);

        log.info("Password successfully reset for user id={}", user.getId());
    }

    // ── Scheduled Cleanup ─────────────────────────────────────────────────

    /**
     * Removes expired tokens from the database every hour to keep the table lean.
     * Runs at the top of every hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpiredTokens() {
        tokenRepository.deleteAllExpiredBefore(LocalDateTime.now());
        log.debug("Purged expired password reset tokens.");
    }
}
