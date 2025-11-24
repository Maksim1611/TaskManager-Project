package com.example.TaskManager.project.model;

import lombok.Getter;

@Getter
public enum ProjectStatus {

    ACTIVE("Active"),
    ON_HOLD("On Hold"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    OVERDUE("Overdue");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

}
