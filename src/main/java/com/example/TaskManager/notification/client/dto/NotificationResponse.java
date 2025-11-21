package com.example.TaskManager.notification.client.dto;

import com.example.TaskManager.notification.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationResponse {

    private String subject;

    private LocalDateTime createdOn;

    private String status;

    private NotificationType type;

}
