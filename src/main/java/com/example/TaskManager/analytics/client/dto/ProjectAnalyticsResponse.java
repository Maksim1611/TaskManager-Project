package com.example.TaskManager.analytics.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectAnalyticsResponse {

    private int totalProjects;

    private int activeProjects;

    private int completedProjects;

    private int averageProgress;

    private int overdueProjects;

    private int totalProjectsLifetime;

    private int completedProjectsLifetime;

    private int abandonedProjectsLifetime;

    private long averageProjectDurationLifetime;

    private double projectCompletionRateLifetime;

}
