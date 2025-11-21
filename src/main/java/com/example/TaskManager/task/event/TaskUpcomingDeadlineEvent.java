package com.example.TaskManager.task.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TaskUpcomingDeadlineEvent {

    private UUID taskId;

    private UUID userId;

    private String title;

    private LocalDateTime deadline;

}
