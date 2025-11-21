package com.example.TaskManager.analytics.service;

import com.example.TaskManager.analytics.client.AnalyticsClient;
import com.example.TaskManager.analytics.client.dto.ProjectAnalyticsRequest;
import com.example.TaskManager.analytics.client.dto.ProjectAnalyticsResponse;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.repository.ProjectRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProjectAnalyticsService {

    private final AnalyticsClient analyticsClient;
    private final ProjectRepository projectRepository;

    public ProjectAnalyticsService(AnalyticsClient analyticsClient, ProjectRepository projectRepository) {
        this.analyticsClient = analyticsClient;
        this.projectRepository = projectRepository;
    }

    public void upsertProjects(UUID userId) {

        List<ProjectAnalyticsRequest> dtos = convertProjectsToDtos(projectRepository.findAllByUserId(userId));

        try {
            analyticsClient.upsertProjects(dtos, userId);
            log.info("Successfully upserted projects!");
        } catch (FeignException e) {
            log.error("[S2S Call]: Failed due to %s".formatted(e.getMessage()));
        }
    }

    private List<ProjectAnalyticsRequest> convertProjectsToDtos(List<Project> projects) {
        List<ProjectAnalyticsRequest> dtos = new ArrayList<>();

        for (Project project : projects) {
            ProjectAnalyticsRequest request = ProjectAnalyticsRequest.builder()
                    .userId(project.getUser().getId())
                    .projectId(project.getId())
                    .createdOn(project.getCreatedOn())
                    .status(project.getStatus())
                    .dueDate(project.getDueDate())
                    .completionPercentage(project.getCompletionPercent())
                    .deleted(project.isDeleted())
                    .build();
            dtos.add(request);
        }

        return dtos;
    }

}
