package com.example.TaskManager.notification.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferenceResponse {

    private boolean emailNotificationEnabled;

    private boolean deadLineNotificationEnabled;

    private boolean summaryNotificationEnabled;

    private boolean reminderNotificationEnabled;

    private String email;

}
