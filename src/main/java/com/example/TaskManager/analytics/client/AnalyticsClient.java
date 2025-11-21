package com.example.TaskManager.analytics.client;

import com.example.TaskManager.analytics.client.dto.ProjectAnalyticsRequest;
import com.example.TaskManager.analytics.client.dto.ProjectAnalyticsResponse;
import com.example.TaskManager.analytics.client.dto.TaskAnalyticsRequest;
import com.example.TaskManager.analytics.client.dto.TaskAnalyticsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "analytics-svc", url = "http://localhost:8081/api/v1")
public interface AnalyticsClient {

    @PostMapping("/tasks/{userId}")
    ResponseEntity<Void> upsertTasks(@RequestBody List<TaskAnalyticsRequest> requestBody, @PathVariable UUID userId);

    @GetMapping("/tasks/{userId}")
    TaskAnalyticsResponse getTaskAnalytics(@PathVariable UUID userId);

    @PostMapping("/projects/{userId}")
    ResponseEntity<Void> upsertProjects(@RequestBody List<ProjectAnalyticsRequest> requestBody, @PathVariable UUID userId);

    @GetMapping("/projects/{userId}")
    ProjectAnalyticsResponse getProjectAnalytics(@PathVariable UUID userId);

}
