package com.example.TaskManager.activity.service;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.repository.ActivityRepository;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.summary.model.SummaryDto;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.utils.ActivityUtils;
import com.example.TaskManager.web.dto.DtoMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public void createActivity(ActivityType activityType, User user, Object object) {
        String message = switch (activityType) {
            case TASK_CREATED -> "Added new task \"%s\"";
            case TASK_UPDATED -> "Updated task \"%s\"";
            case TASK_DELETED -> "Deleted task \"%s\"";
            case TASK_COMPLETED -> "Completed task \"%s\"";
            case PROJECT_CREATED -> "Added new project \"%s\"";
            case PROJECT_UPDATED -> "Updated project \"%s\"";
            case PROJECT_DELETED -> "Deleted project \"%s\"";
            case PROJECT_COMPLETED -> "Completed project \"%s\"";
            case PROJECT_TASK_COMPLETED -> "Completed task \"%s\" in project \"%s\"";
            case PROJECT_TASK_UPDATED -> "Updated task \"%s\" in project \"%s\"";
            case PROJECT_TASK_CREATED -> "Created task \"%s\" in project \"%s\"";
            case PROJECT_TASK_DELETED -> "Deleted task \"%s\" in project \"%s\"";
        };
        Activity activity = Activity.builder()
                .message("")
                .user(user)
                .createdOn(LocalDateTime.now())
                .type(activityType)
                .updatedOn(LocalDateTime.now())
                .build();

        if (object instanceof Task task) {
            if (task.getProject() != null) {
                activity.setMessage(message.formatted(task.getTitle(), task.getProject().getTitle()));
            }  else {
                activity.setMessage(message.formatted(task.getTitle()));
            }
        } else if (object instanceof Project project) {
            activity.setMessage(message.formatted(project.getTitle()));
        }

        ActivityUtils.setActivityCreatedDateFormatted(activity);
        activityRepository.save(activity);
    }

    public List<Activity> getByUserId(UUID id) {
        return activityRepository.findAllByUserIdOrderByCreatedOnDesc(id);
    }

    public List<Activity> getActivityByTypeAndUserId(UUID id, String typeText) {

        if (typeText == null) {
            List<Activity> list = getByUserId(id);
            list.forEach(ActivityUtils::setActivityCreatedDateFormatted);
            return list;
        }

        List<Activity> list = activityRepository.findByUserAndTypeText(id, typeText);
        list.forEach(ActivityUtils::setActivityCreatedDateFormatted);
        return list;
    }

    private void update(Activity activity) {
        activity.setUpdatedOn(LocalDateTime.now());
        activityRepository.save(activity);
    }

    @Transactional
    public void deleteActivity(UUID id) {
        activityRepository.deleteAllByUserId(id);
    }
}
