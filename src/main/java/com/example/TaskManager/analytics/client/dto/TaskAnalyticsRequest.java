package com.example.TaskManager.analytics.client.dto;

import com.example.TaskManager.task.model.TaskPriority;
import com.example.TaskManager.task.model.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TaskAnalyticsRequest {

    private UUID taskId;

    private UUID userId;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDateTime createdOn;

    private LocalDateTime dueDate;

    private LocalDateTime completedOn;

    private boolean deleted;
}
