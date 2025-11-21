package com.example.TaskManager.project.model;

import lombok.Getter;

@Getter
public enum ProjectVisibility {
    PUBLIC("Public"),
    PRIVATE("Private");

    private final String displayName;
    ProjectVisibility(String displayName) {
        this.displayName = displayName;
    }

}
