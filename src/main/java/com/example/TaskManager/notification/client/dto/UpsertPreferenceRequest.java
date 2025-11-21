package com.example.TaskManager.notification.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpsertPreferenceRequest {

    private UUID userId;

    private boolean emailNotificationEnabled;

    private boolean deadLineNotificationEnabled;

    private boolean summaryNotificationEnabled;

    private boolean reminderNotificationEnabled;

    private String email;
}
