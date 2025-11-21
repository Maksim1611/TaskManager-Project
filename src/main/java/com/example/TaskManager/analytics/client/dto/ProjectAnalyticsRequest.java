package com.example.TaskManager.analytics.client.dto;

import com.example.TaskManager.project.model.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectAnalyticsRequest {

    private UUID userId;

    private UUID projectId;

    private LocalDateTime createdOn;

    private ProjectStatus status;

    private LocalDateTime dueDate;

    private int completionPercentage;

    private boolean deleted;

}
