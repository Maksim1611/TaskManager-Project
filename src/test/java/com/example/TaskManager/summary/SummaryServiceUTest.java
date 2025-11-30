package com.example.TaskManager.summary;

import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.repository.ActivityRepository;
import com.example.TaskManager.notification.NotificationMessages;
import com.example.TaskManager.notification.NotificationType;
import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.summary.model.SummaryDto;
import com.example.TaskManager.summary.service.SummaryService;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.repository.TaskRepository;
import com.example.TaskManager.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SummaryServiceUTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SummaryService summaryService;

    @Test
    void dailySummary_shouldReturnCorrectSummary() {
        UUID userId = UUID.randomUUID();

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                eq(userId), eq(ActivityType.TASK_CREATED), any(), any()
        )).thenReturn(5L);

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                eq(userId), eq(ActivityType.TASK_COMPLETED), any(), any()
        )).thenReturn(3L);

        when(taskRepository.countByUserIdAndDeletedFalseAndStatusAndDueDateBetween(
                eq(userId), eq(TaskStatus.OVERDUE), any(), any()
        )).thenReturn(2L);

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                eq(userId), eq(ActivityType.PROJECT_COMPLETED), any(), any()
        )).thenReturn(1L);

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                eq(userId), eq(ActivityType.PROJECT_CREATED), any(), any()
        )).thenReturn(4L);

        when(taskService.getTaskCompletionRateLast24Hours(eq(userId), any(), any()))
                .thenReturn(0.75);

        SummaryDto summary = summaryService.dailySummary(userId);

        assertEquals(5, summary.getCreatedTasks());
        assertEquals(3, summary.getCompletedTasks());
        assertEquals(2, summary.getOverdueTasks());
        assertEquals(1, summary.getCompletedProjects());
        assertEquals(4, summary.getCreatedProjects());
        assertEquals(0.75, summary.getTaskCompletionRate());
    }

    @Test
    void sendDailySummary_shouldCallNotificationServiceWithCorrectArguments() {

        UUID userId = UUID.randomUUID();

        SummaryDto dto = SummaryDto.builder()
                .createdTasks(5)
                .completedTasks(3)
                .overdueTasks(2)
                .createdProjects(4)
                .completedProjects(1)
                .taskCompletionRate(0.75)
                .build();

        summaryService.sendDailySummary(userId, dto);

        String expectedBody = NotificationMessages.DAILY_SUMMARY_MESSAGE_BODY.formatted(
                dto.getCreatedTasks(),
                dto.getCompletedTasks(),
                dto.getOverdueTasks(),
                dto.getCreatedProjects(),
                dto.getCompletedProjects(),
                dto.getTaskCompletionRate()
        );

        verify(notificationService).sendNotification(
                eq(userId),
                eq(NotificationMessages.DAILY_SUMMARY_MESSAGE_SUBJECT),
                eq(expectedBody),
                eq(NotificationType.EMAIL)
        );
    }

}
