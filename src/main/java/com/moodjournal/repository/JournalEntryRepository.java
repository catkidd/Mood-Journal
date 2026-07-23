package com.moodjournal.repository;

import com.moodjournal.model.JournalEntry;
import com.moodjournal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    // Basic finders
    List<JournalEntry> findByUserOrderByDateDesc(User user);

    List<JournalEntry> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);

    long countByUser(User user);

    // Find today's entry
    List<JournalEntry> findByUserAndDate(User user, LocalDate date);

    // Recent entries (limited)
    @Query("SELECT e FROM JournalEntry e WHERE e.user = :user ORDER BY e.date DESC, e.id DESC")
    List<JournalEntry> findRecentByUser(@Param("user") User user, Pageable pageable);

    // Search with keyword and mood filter (paginated)
    @Query("SELECT e FROM JournalEntry e WHERE e.user = :user " +
           "AND (:mood IS NULL OR :mood = '' OR e.mood = :mood) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(e.text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.mood) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JournalEntry> searchEntries(@Param("user") User user,
                                     @Param("mood") String mood,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    // Streak: distinct dates ordered desc
    @Query("SELECT DISTINCT e.date FROM JournalEntry e WHERE e.user = :user ORDER BY e.date DESC")
    List<LocalDate> findDistinctDatesByUserOrderByDateDesc(@Param("user") User user);

    // Mood distribution for pie chart
    @Query("SELECT e.mood, COUNT(e) FROM JournalEntry e WHERE e.user = :user GROUP BY e.mood")
    List<Object[]> getMoodCounts(@Param("user") User user);

    // Daily entry counts for line chart
    @Query("SELECT e.date, COUNT(e) FROM JournalEntry e WHERE e.user = :user AND e.date >= :startDate GROUP BY e.date ORDER BY e.date")
    List<Object[]> getDailyEntryCounts(@Param("user") User user, @Param("startDate") LocalDate startDate);

    // Distinct moods used by user (for ALL_MOODS badge)
    @Query("SELECT DISTINCT e.mood FROM JournalEntry e WHERE e.user = :user")
    Set<String> findDistinctMoodsByUser(@Param("user") User user);
}
