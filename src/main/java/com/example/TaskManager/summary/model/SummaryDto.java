package com.example.TaskManager.summary.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SummaryDto {

    private long createdTasks;
    private long completedTasks;
    private long overdueTasks;
    private long completedProjects;
    private long createdProjects;
    private double taskCompletionRate;

}
