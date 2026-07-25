package com.moodjournal.repository;

import com.moodjournal.model.PasswordResetToken;
import com.moodjournal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /** Remove any existing (potentially stale) tokens for a user before issuing a new one. */
    void deleteByUser(User user);

    /** Scheduled cleanup: removes tokens that have already expired. */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
    void deleteAllExpiredBefore(LocalDateTime now);
}
