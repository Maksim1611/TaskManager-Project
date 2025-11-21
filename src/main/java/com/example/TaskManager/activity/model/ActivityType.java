package com.example.TaskManager.activity.model;

import lombok.Getter;

@Getter
public enum ActivityType {

    TASK_COMPLETED,
    TASK_UPDATED,
    TASK_CREATED,
    TASK_DELETED,
    PROJECT_COMPLETED,
    PROJECT_UPDATED,
    PROJECT_CREATED,
    PROJECT_DELETED,
    PROJECT_TASK_COMPLETED,
    PROJECT_TASK_UPDATED,
    PROJECT_TASK_CREATED,
    PROJECT_TASK_DELETED
}
