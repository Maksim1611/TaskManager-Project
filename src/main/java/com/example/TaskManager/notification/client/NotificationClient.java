package com.example.TaskManager.notification.client;

import com.example.TaskManager.notification.client.dto.NotificationRequest;
import com.example.TaskManager.notification.client.dto.NotificationResponse;
import com.example.TaskManager.notification.client.dto.PreferenceResponse;
import com.example.TaskManager.notification.client.dto.UpsertPreferenceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-svc", url = "http://localhost:8082/api/v1")
public interface NotificationClient {

    @PostMapping("/preferences")
    ResponseEntity<Void> upsertPreference(@RequestBody UpsertPreferenceRequest requestBody  );

    @GetMapping("/preferences")
    ResponseEntity<PreferenceResponse> getPreferenceByUserId(@RequestParam("userId") UUID userId);

    @GetMapping("/notifications")
    ResponseEntity<List<NotificationResponse>> getHistory(@RequestParam("userId") UUID userId);

    @PostMapping("/notifications")
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest requestBody);

    @DeleteMapping("/notifications/history")
    void deleteNotifications(@RequestParam("userId") UUID userId);
}
