package com.example.TaskManager;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.repository.ActivityRepository;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.model.ProjectVisibility;
import com.example.TaskManager.project.repository.ProjectRepository;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskPriority;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.repository.TaskRepository;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.utils.ActivityUtils;
import com.example.TaskManager.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@SpringBootTest
public class ActivityITest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void createActivity_withTaskCreatedNoProject_theHappyPath() {
        ActivityType type = ActivityType.TASK_CREATED;
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);

        Task task = Task.builder()
                .title("Task")
                .description("Desc")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .deleted(false)
                .project(null)
                .build();
        taskRepository.save(task);

        activityService.createActivity(type, user, task);
        Activity activity = activityService.getByUserId(user.getId()).get(0);

        assertEquals("Added new task \"Task\"", activity.getMessage());
        assertEquals(type, activity.getType());
        assertEquals(user.getId(), activity.getUser().getId());
        assertNotNull(activity.getCreatedOn());
        assertNotNull(activity.getUpdatedOn());
        assertEquals("Just now", activity.getDateOutput());
    }

    @Test
    void createActivity_withTaskCreatedWithProject_theHappyPath() {
        ActivityType type = ActivityType.PROJECT_TASK_CREATED;
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);

        Project project = Project.builder()
                .title("Project")
                .description("Description")
                .createdOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .status(ProjectStatus.ACTIVE)
                .user(user)
                .tags(null)
                .completionPercent(0)
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .projectVisibility(ProjectVisibility.PRIVATE)
                .members(new ArrayList<>())
                .build();
        projectRepository.save(project);

        Task task = Task.builder()
                .title("Task")
                .description("Desc")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .deleted(false)
                .project(project)
                .build();
        taskRepository.save(task);

        activityService.createActivity(type, user, task);
        Activity activity = activityService.getByUserId(user.getId()).get(0);

        assertEquals("Created task \"Task\" in project \"Project\"", activity.getMessage());
        assertEquals(type, activity.getType());
        assertEquals(user.getId(), activity.getUser().getId());
        assertNotNull(activity.getCreatedOn());
        assertNotNull(activity.getUpdatedOn());
        assertEquals("Just now", activity.getDateOutput());
    }

    @Test
    void createActivity_withProjectCreated_theHappyPath() {
        ActivityType type = ActivityType.TASK_CREATED;
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);

        Project project = Project.builder()
                .title("Project")
                .description("Description")
                .createdOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .status(ProjectStatus.ACTIVE)
                .user(user)
                .tags(null)
                .completionPercent(0)
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .projectVisibility(ProjectVisibility.PRIVATE)
                .members(new ArrayList<>())
                .build();
        projectRepository.save(project);

        activityService.createActivity(type, user, project);
        Activity activity = activityService.getByUserId(user.getId()).get(0);

        assertEquals("Added new task \"Project\"", activity.getMessage());
        assertEquals(type, activity.getType());
        assertEquals(user.getId(), activity.getUser().getId());
        assertNotNull(activity.getCreatedOn());
        assertNotNull(activity.getUpdatedOn());
        assertEquals("Just now", activity.getDateOutput());
    }

    @Test
    void getActivityByTypeAndUserId_withNullType() {
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);

        UUID userId = user.getId();

        for (int i = 0; i < 3; i++) {
            generateActivity(user);
        }

        List<Activity> activities = activityService.getActivityByTypeAndUserId(userId, null);

        assertSame(3, activities.size());
    }

    @Test
    void getActivityByTypeAndUserId_withNotNullType() {
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);

        UUID userId = user.getId();

        for (int i = 0; i < 3; i++) {
            generateActivity(user);
        }

        List<Activity> activities = activityService.getActivityByTypeAndUserId(userId, "TASK_CREATED");

        List<Activity> result = activityRepository.findByUserAndTypeText(userId, "TASK_CREATED");
        result.forEach(ActivityUtils::setActivityCreatedDateFormatted);

        assertSame(result.size(), activities.size());

    }

    public Activity generateActivity(User user) {
        Random random = new Random();

        Task task = Task.builder()
                .title("Task")
                .description("Desc")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .deleted(false)
                .project(null)
                .build();
        taskRepository.save(task);

        activityService.createActivity(ActivityType.TASK_CREATED, user, task);
        return activityService.getByUserId(user.getId()).get(0);
    }

}
