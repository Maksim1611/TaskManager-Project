package com.example.TaskManager.web.dto;

import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.model.ProjectVisibility;
import com.example.TaskManager.tag.model.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
public class EditProjectRequest {

    @NotNull
    @Size(min = 3, max = 20)
    private String title;

    @Size(min = 3, max = 300)
    private String description;

    private ProjectStatus projectStatus;

    private ProjectVisibility projectVisibility;

    private LocalDateTime dueDate;

    private String tags;

    public List<String> getTagNames() {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(tags.split(",\\s*")).filter(s -> !s.isEmpty()).toList();
    }

}
