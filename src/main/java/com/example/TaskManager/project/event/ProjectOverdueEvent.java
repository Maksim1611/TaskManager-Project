package com.example.TaskManager.project.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectOverdueEvent {

    private UUID projectId;

    private UUID ownerId;

    private String title;

    private LocalDateTime deadline;

}
