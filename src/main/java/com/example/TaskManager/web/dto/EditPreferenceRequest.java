package com.example.TaskManager.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EditPreferenceRequest {

    private UUID userId;
    private boolean emailNotificationEnabled;
    private boolean deadLineNotificationEnabled;
    private boolean summaryNotificationEnabled;
    private boolean reminderNotificationEnabled;


}
