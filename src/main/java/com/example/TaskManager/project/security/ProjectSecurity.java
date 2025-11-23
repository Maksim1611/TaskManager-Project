package com.example.TaskManager.project.security;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProjectSecurity {

    private final ProjectService projectService;

    public ProjectSecurity(ProjectService projectService) {
        this.projectService = projectService;
    }

    public boolean isOwner(UUID id, Authentication authentication) {
        Project project = projectService.getByIdNotDeleted(id);
        String email = authentication.getName();

        return project.getUser().getEmail().equals(email);
    }
}
