package com.example.TaskManager.task.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TaskOverdueEvent {

    private UUID taskId;

    private UUID userId;

    private String title;

    private LocalDateTime dueDate;

}
