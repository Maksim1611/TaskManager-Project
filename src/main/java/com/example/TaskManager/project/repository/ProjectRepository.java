package com.example.TaskManager.project.repository;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByTitle(String title);

    List<Project> findAllByUserIdAndDeletedOrderByDueDateAsc(UUID id, boolean deleted);

    List<Project> findAllByUserId(UUID id);

    List<Project> findAllByUserIdAndDeletedFalse(UUID id);

    List<Project> findAllByUserIdAndDeletedFalseAndStatus(UUID userId, ProjectStatus status);

    List<Project> findByDueDateBeforeAndStatusNotAndDeletedFalse(LocalDateTime now, ProjectStatus projectStatus);

    List<Project> findByStatusNotAndDeletedFalse(ProjectStatus status);

    Optional<Project> findByIdAndDeletedFalse(UUID id);
}
