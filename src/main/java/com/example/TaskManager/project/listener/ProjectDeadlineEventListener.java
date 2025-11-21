package com.example.TaskManager.project.listener;

import com.example.TaskManager.notification.NotificationMessages;
import com.example.TaskManager.notification.NotificationType;
import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.project.event.ProjectOverdueEvent;
import com.example.TaskManager.project.event.ProjectUpcomingDeadlineEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProjectDeadlineEventListener {

    private final NotificationService notificationService;

    public ProjectDeadlineEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleProjectOverdue(ProjectOverdueEvent event) {
        notificationService.sendNotification(event.getOwnerId(), NotificationMessages.PROJECT_OVERDUE
                .formatted(event.getTitle()), "", NotificationType.DEADLINE);
    }

    @EventListener
    public void handleProjectUpcomingDeadline(ProjectUpcomingDeadlineEvent event) {
        notificationService.sendNotification(event.getOwnerId(), NotificationMessages.PROJECT_UPCOMING_DEADLINE
                .formatted(event.getTitle()), "", NotificationType.REMINDER);
    }
}
