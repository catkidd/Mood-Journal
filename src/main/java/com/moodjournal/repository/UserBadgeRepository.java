package com.moodjournal.repository;

import com.moodjournal.model.Badge;
import com.moodjournal.model.User;
import com.moodjournal.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserOrderByDateAwardedDesc(User user);

    boolean existsByUserAndBadge(User user, Badge badge);
}
