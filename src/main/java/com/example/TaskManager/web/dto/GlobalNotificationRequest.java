package com.example.TaskManager.web.dto;

import com.example.TaskManager.notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalNotificationRequest {

    @NotBlank
    private String subject;

    private String body;

    private NotificationType notificationType;


}
