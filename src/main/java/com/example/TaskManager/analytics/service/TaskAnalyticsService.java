package com.example.TaskManager.analytics.service;

import com.example.TaskManager.analytics.client.AnalyticsClient;
import com.example.TaskManager.analytics.client.dto.TaskAnalyticsRequest;
import com.example.TaskManager.analytics.client.dto.TaskAnalyticsResponse;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.repository.TaskRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TaskAnalyticsService {

    private final AnalyticsClient client;
    private final TaskRepository taskRepository;

    public TaskAnalyticsService(AnalyticsClient client, TaskRepository taskRepository) {
        this.client = client;
        this.taskRepository = taskRepository;
    }

    public void upsertTasks(UUID userId) {

        List<TaskAnalyticsRequest> dtos = convertTasksToDtos(taskRepository.findAllByUserIdAndProjectNull(userId));

        try {
            client.upsertTasks(dtos, userId);
            log.info("Successfully upserted tasks!");
        } catch (FeignException e) {
            log.error("[S2S Call]: Failed due to %s".formatted(e.getMessage()));
        }
    }

    private List<TaskAnalyticsRequest> convertTasksToDtos(List<Task> tasks) {
        List<TaskAnalyticsRequest> dtos = new ArrayList<>();

        for (Task task : tasks) {
            TaskAnalyticsRequest dto = TaskAnalyticsRequest.builder()
                    .userId(task.getUser().getId())
                    .taskId(task.getId())
                    .status(task.getStatus())
                    .priority(task.getPriority())
                    .createdOn(task.getCreatedOn())
                    .dueDate(task.getDueDate())
                    .completedOn(task.getCompletedOn())
                    .deleted(task.isDeleted())
                    .build();

            dtos.add(dto);
        }

        return dtos;
    }

    public TaskAnalyticsResponse getUserAnalytics(UUID userId) {
        return client.getTaskAnalytics(userId);
    }

}
