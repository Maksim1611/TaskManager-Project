package com.example.TaskManager.utils;

import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskPriority;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.user.model.User;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class TaskUtils {

    public static Task generateTask(User user) {
        Task task = Task.builder()
                .title("Task")
                .description("Description")
                .user(user)
                .project(null)
                .createdOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .updatedOn(LocalDateTime.now())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .build();
        return task;
    }

}
