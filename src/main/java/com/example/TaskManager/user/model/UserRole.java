package com.example.TaskManager.user.model;

import lombok.Getter;

@Getter
public enum UserRole {

    USER("User"),
    MODERATOR("Moderator"),
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

}
