package com.example.TaskManager.notification.client.dto;

import com.example.TaskManager.notification.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationRequest {

    private UUID userId;

    private String subject;

    private String body;

    private NotificationType type;

}
