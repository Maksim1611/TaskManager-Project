package com.example.TaskManager.web.dto;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.user.model.User;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Autowired;

@UtilityClass
public class DtoMapper {

    public static EditProfileRequest fromUser(User user) {
        return EditProfileRequest.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }


    public static EditProjectRequest fromProject(Project project, String tags) {
        return EditProjectRequest.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .projectStatus(project.getStatus())
                .projectVisibility(project.getProjectVisibility())
                .dueDate(project.getDueDate())
                .tags(tags)
                .build();
    }

    public static EditTaskRequest fromTask(Task task) {
        return EditTaskRequest.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .build();
    }

    public static EditPreferenceRequest fromUserPreference(User user) {
        return EditPreferenceRequest.builder()
                .userId(user.getId())
                .summaryNotificationEnabled(user.isSummaryNotificationEnabled())
                .deadLineNotificationEnabled(user.isDeadLineNotificationEnabled())
                .emailNotificationEnabled(user.isEmailNotificationEnabled())
                .reminderNotificationEnabled(user.isReminderNotificationEnabled())
                .build();
    }
}
