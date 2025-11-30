package com.example.TaskManager.task.repository;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByProjectIdAndDeletedFalse(UUID id);

    Optional<Task> findByIdAndDeletedFalse(UUID id);

    List<Task> findByDueDateBeforeAndStatusNotAndDeletedFalse(LocalDateTime now, TaskStatus taskStatus);

    List<Task> findByStatusNotAndDeletedFalse(TaskStatus taskStatus);

    long countByUserIdAndDeletedFalseAndStatusAndDueDateBetween(UUID userId, TaskStatus taskStatus, LocalDateTime yesterday, LocalDateTime now);

    List<Task> findAllByUserIdAndProjectNullAndDeletedFalse(UUID userId);

    List<Task> findAllByUserIdAndProjectNull(UUID userId);

    List<Task> findAllByUserIdAndStatusNotAndStatusNotAndDeletedFalseAndProjectNullOrderByDueDateAsc(UUID id, TaskStatus taskStatus, TaskStatus taskStatus1);

    List<Task> findAllByUserIdAndDeletedFalseOrderByCreatedOnDesc(UUID id);

    Optional<Task> findByTitleAndProjectNullAndDeletedFalse( String title);

    List<Task> findAllByUserIdAndProjectNullAndDeletedFalseAndDueDateBetweenAndStatusNotAndStatusNot(UUID userId, LocalDateTime dueDateAfter, LocalDateTime dueDateBefore, TaskStatus status, TaskStatus status1);

    List<Task> findAllByUserIdAndStatusAndDeletedFalse(UUID userId, TaskStatus taskStatus);
}
