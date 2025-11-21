package com.example.TaskManager.analytics.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskAnalyticsResponse {
    private int totalTasks;

    private int completedTasks;

    private int inProgressTasks;

    private int todoTasks;

    private int overdueTasks;

    private double completionRate;

    private double avgCompletionTime;

    private int lowPriorityCount;

    private int mediumPriorityCount;

    private int highPriorityCount;

    private int lifetimeTotalTasks;

    private int lifetimeCompletedTasks;

    private int lifetimeAbandonedTasks;

    private int lifetimeOverdueTasks;

    private double lifetimeAverageCompletionTime;

    private long fastestCompletionTime;

    private int lifetimeCompletionRate;

}
