package com.example.TaskManager.task.listener;

import com.example.TaskManager.notification.NotificationMessages;
import com.example.TaskManager.notification.NotificationType;
import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.task.event.TaskOverdueEvent;
import com.example.TaskManager.task.event.TaskUpcomingDeadlineEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TaskOverdueEventListener {

    private final NotificationService notificationService;

    public TaskOverdueEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleTaskOverdue(TaskOverdueEvent event) {
        notificationService.sendNotification(event.getUserId(), NotificationMessages.TASK_OVERDUE_SUBJECT
                .formatted(event.getTitle()),"", NotificationType.DEADLINE);
    }

    @EventListener
    public void handleUpcomingTaskDeadline(TaskUpcomingDeadlineEvent event) {
        notificationService.sendNotification(event.getUserId(), NotificationMessages.TASK_UPCOMING_DEADLINE
                .formatted(event.getTitle()), "", NotificationType.REMINDER);
    }
}
