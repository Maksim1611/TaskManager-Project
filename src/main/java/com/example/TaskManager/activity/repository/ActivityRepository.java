package com.example.TaskManager.activity.repository;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    @Query("SELECT a FROM Activity a WHERE a.user.id = :id AND " +
            "LOWER(CAST(a.type AS string)) LIKE LOWER(CONCAT('%', :typeText, '%')) ORDER BY a.createdOn DESC")
    List<Activity> findByUserAndTypeText(@Param("id") UUID id, @Param("typeText") String typeText);

    List<Activity> findAllByUserIdOrderByCreatedOnDesc(UUID userId);

    long countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(UUID userId, ActivityType activityType, LocalDateTime yesterday, LocalDateTime now);

    void deleteAllByUserId(UUID id);
}
