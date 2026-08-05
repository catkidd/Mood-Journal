package com.moodjournal.repository;

import com.moodjournal.model.User;
import com.moodjournal.model.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    /** Total games played by a user (any game). */
    long countByUser(User user);

    /** Total plays for a specific game. */
    long countByUserAndGameName(User user, String gameName);

    /** Recent play history, newest first. */
    List<UserGame> findByUserOrderByPlayedAtDesc(User user);

    /**
     * Per-game play counts as Object[] rows: [0]=gameName (String), [1]=playCount (Long).
     * Using Object[] avoids Hibernate's inability to map JPQL aliases to Map keys.
     */
    @Query("SELECT ug.gameName, COUNT(ug) " +
           "FROM UserGame ug WHERE ug.user = :user GROUP BY ug.gameName")
    List<Object[]> countByUserGroupByGame(@Param("user") User user);
}
