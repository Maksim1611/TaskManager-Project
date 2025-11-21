package com.example.TaskManager.web.dto;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.task.model.TaskPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditTaskRequest {

    @NotNull
    @Size(min = 3, max = 20)
    private String title;

    @Size(min = 3, max = 100)
    private String description;

    private TaskPriority priority;

    private LocalDateTime dueDate;

}
