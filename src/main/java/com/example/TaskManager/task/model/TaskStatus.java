package com.example.TaskManager.task.model;

import lombok.Getter;

@Getter
public enum TaskStatus {

    TODO("Todo"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    OVERDUE("Overdue"),;

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

}
