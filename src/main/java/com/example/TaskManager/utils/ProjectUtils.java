package com.example.TaskManager.utils;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.model.ProjectVisibility;
import com.example.TaskManager.user.model.User;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@UtilityClass
public class ProjectUtils {

    public static Project generateProject(User user) {
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .title("Project")
                .description("Description")
                .user(user)
                .status(ProjectStatus.ACTIVE)
                .createdOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .updatedOn(LocalDateTime.now())
                .projectVisibility(ProjectVisibility.PRIVATE)
                .completionPercent(0)
                .deleted(false)
                .tasks(new ArrayList<>())
                .tags(new ArrayList<>())
                .build();
        return project;
    }

}
