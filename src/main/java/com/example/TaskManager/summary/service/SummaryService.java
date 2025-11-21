package com.example.TaskManager.summary.service;

import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.repository.ActivityRepository;
import com.example.TaskManager.notification.NotificationMessages;
import com.example.TaskManager.notification.NotificationType;
import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.summary.model.SummaryDto;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.repository.TaskRepository;
import com.example.TaskManager.task.service.TaskService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SummaryService {

    private final ActivityRepository activityRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final NotificationService notificationService;

    public SummaryService(ActivityRepository activityRepository, TaskRepository taskRepository, TaskService taskService, NotificationService notificationService) {
        this.activityRepository = activityRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    @Cacheable(value = "dailySummary", key = "#userId")
    public SummaryDto dailySummary(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusHours(24);

        long createdTasks = activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(userId, ActivityType.TASK_CREATED ,yesterday, now);
        long completedTasks = activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(userId, ActivityType.TASK_COMPLETED ,yesterday, now);
        long overdueTasks = taskRepository.countByUserIdAndDeletedFalseAndStatusAndDueDateBetween(userId, TaskStatus.OVERDUE, yesterday, now);
        long completedProjects = activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(userId, ActivityType.PROJECT_COMPLETED ,yesterday, now);
        long createdProjects = activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(userId, ActivityType.PROJECT_CREATED ,yesterday, now);

        double taskCompletionRate = taskService.getTaskCompletionRateLast24Hours(userId, yesterday, now);

        return SummaryDto.builder()
                .createdTasks(createdTasks)
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .completedProjects(completedProjects)
                .createdProjects(createdProjects)
                .taskCompletionRate(taskCompletionRate)
                .build();
    }

    public void sendDailySummary(UUID userId, SummaryDto dto) {
        notificationService.sendNotification(userId, NotificationMessages.DAILY_SUMMARY_MESSAGE_SUBJECT
                , NotificationMessages.DAILY_SUMMARY_MESSAGE_BODY.formatted(dto.getCreatedTasks(), dto.getCompletedTasks(),
                        dto.getOverdueTasks(), dto.getCreatedProjects(), dto.getCompletedProjects(), dto.getTaskCompletionRate()), NotificationType.EMAIL);

    }

}
