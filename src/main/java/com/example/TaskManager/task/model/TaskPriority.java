package com.example.TaskManager.task.model;

import lombok.Getter;

@Getter
public enum TaskPriority {

    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String displayName;

    TaskPriority(String displayName) {
        this.displayName = displayName;
    }
}
