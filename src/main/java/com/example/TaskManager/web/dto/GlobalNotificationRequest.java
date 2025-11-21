package com.example.TaskManager.web.dto;

import com.example.TaskManager.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalNotificationRequest {

    private String subject;

    private String body;

    private NotificationType notificationType;


}
