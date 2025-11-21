package com.example.TaskManager.project.model;

import lombok.Getter;

@Getter
public enum ProjectStatus {

    ACTIVE("Active"), // When the project stays open and the work is mild without forcing it
    ON_HOLD("On Hold"), // When the project is left sideways and not being worked on
    IN_PROGRESS("In Progress"), // When the major focus is over the project
    COMPLETED("Completed"),
    OVERDUE("Overdue");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

}
