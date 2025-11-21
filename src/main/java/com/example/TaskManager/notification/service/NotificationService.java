package com.example.TaskManager.notification.service;

import com.example.TaskManager.notification.NotificationType;
import com.example.TaskManager.notification.client.NotificationClient;
import com.example.TaskManager.notification.client.dto.NotificationRequest;
import com.example.TaskManager.notification.client.dto.NotificationResponse;
import com.example.TaskManager.notification.client.dto.PreferenceResponse;
import com.example.TaskManager.notification.client.dto.UpsertPreferenceRequest;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.EditPreferenceRequest;
import com.example.TaskManager.web.dto.GlobalNotificationRequest;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationClient client;

    public NotificationService(NotificationClient notificationClient ) {
        this.client = notificationClient;
    }

    public void upsertPreferences(UUID userId, boolean emailNotificationEnabled, boolean deadLineNotificationEnabled, boolean summaryNotificationEnabled, boolean reminderNotificationEnabled, String email) {

        UpsertPreferenceRequest request = UpsertPreferenceRequest.builder()
                .userId(userId)
                .emailNotificationEnabled(emailNotificationEnabled)
                .deadLineNotificationEnabled(deadLineNotificationEnabled)
                .summaryNotificationEnabled(summaryNotificationEnabled)
                .reminderNotificationEnabled(reminderNotificationEnabled)
                .email(email)
                .build();

        try {
            client.upsertPreference(request);
            log.info("Successfully upserted preference request");
        } catch (FeignException e) {
            log.error("[S2S Call]: Failed due to %s".formatted(e.getMessage()));
        }
    }

    public PreferenceResponse getPreferenceByUserId(UUID userId) {
        return client.getPreferenceByUserId(userId).getBody();
    }

    public List<NotificationResponse> getUserNotifications(UUID userId) {
        ResponseEntity<List<NotificationResponse>> response = client.getHistory(userId);

        return response.getBody();
    }

    public void sendNotification(UUID userId, String subject, String body, NotificationType type) {
        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .subject(subject)
                .body(body)
                .type(type)
                .build();

        try {
            client.sendNotification(request);
            log.info("Successfully sent notification request");
        } catch (FeignException e) {
            log.error("[S2S Call]: Failed due to %s".formatted(e.getMessage()));
        }
    }

    public void updatePreferences(User user, @Valid EditPreferenceRequest editPreferenceRequest) {
        upsertPreferences(user.getId(), editPreferenceRequest.isEmailNotificationEnabled(), editPreferenceRequest.isDeadLineNotificationEnabled(),
                editPreferenceRequest.isSummaryNotificationEnabled(), editPreferenceRequest.isReminderNotificationEnabled(), user.getEmail());
    }

    public void clearHistory(UUID userId) {
        try {
            client.deleteNotifications(userId);
            log.info("Successfully deleted notifications for user [%s]".formatted(userId));
        } catch (FeignException e) {
            log.error("[S2S Call]: Failed due to %s".formatted(e.getMessage()));

        }
    }

    public void sendGlobalNotification(@Valid GlobalNotificationRequest globalNotificationRequest, List<User> users) {
        for (User user : users) {
            sendNotification(user.getId(), globalNotificationRequest.getSubject(),
                    globalNotificationRequest.getBody(), globalNotificationRequest.getNotificationType());
        }
    }
}
