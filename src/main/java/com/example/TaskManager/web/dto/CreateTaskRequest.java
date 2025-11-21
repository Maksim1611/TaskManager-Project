package com.example.TaskManager.web.dto;

import com.example.TaskManager.project.model.Project;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateTaskRequest {

    @NotNull
    @Size(min = 3, max = 20)
    private String title;

    @Size(min = 3, max = 100)
    private String description;

    @NotNull
    private String priority;

    @NotNull
    private LocalDateTime dueDate;
}
