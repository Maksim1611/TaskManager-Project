package com.example.TaskManager;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.analytics.service.TaskAnalyticsService;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.model.ProjectVisibility;
import com.example.TaskManager.project.repository.ProjectRepository;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskPriority;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.repository.TaskRepository;
import com.example.TaskManager.task.service.TaskService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.utils.UserUtils;
import com.example.TaskManager.web.dto.CreateTaskRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TaskITest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityService activityService;

    @Test
    void createTask_withNullProject_happyPath()  {
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);

        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Task")
                .description("Description")
                .priority("LOW")
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        Task task = taskService.createTask(request, user, null);

        Activity activity = activityService.getByUserId(user.getId()).get(0);

        assertEquals(ActivityType.TASK_CREATED, activity.getType());
        assertEquals("Task", task.getTitle());
        assertEquals("Description", task.getDescription());
        assertEquals(TaskPriority.LOW, task.getPriority());
        assertNotNull(task.getDueDate());
    }

    @Test
    void createTask_withNotNullProject_happyPath()  {
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);



        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Task")
                .description("Description")
                .priority("LOW")
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        Task task = taskService.createTask(request, user, generateProject(user));

        Activity activity = activityService.getByUserId(user.getId()).get(0);

        assertEquals(ActivityType.PROJECT_TASK_CREATED, activity.getType());
        assertEquals("Task", task.getTitle());
        assertEquals("Description", task.getDescription());
        assertEquals(TaskPriority.LOW, task.getPriority());
        assertNotNull(task.getProject());
        assertNotNull(task.getDueDate());
    }

    @Test
    void completeTask_withProjectNull_happyPath()  {
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);

        Task task = generateTask(user);

        taskService.completeTask(task.getId());
        Task updated = taskRepository.findById(task.getId()).orElseThrow();

        Activity activity = activityService.getByUserId(user.getId()).get(0);

        assertEquals(TaskStatus.COMPLETED, updated.getStatus());
        assertNotNull(updated.getCompletedOn());
        assertNotNull(updated.getUpdatedOn());
        assertEquals(ActivityType.TASK_COMPLETED, activity.getType());
    }

    @Test
    void completeTask_withProjectNotNull_happyPath()   {
        User user = UserUtils.randomUser();
        user.setId(null);
        userRepository.save(user);

        Task task = generateTask(user);
        task.setProject(generateProject(user));
        taskService.update(task);

        taskService.completeTask(task.getId());
        Task updated = taskRepository.findById(task.getId()).orElseThrow();

        Activity activity = activityService.getByUserId(user.getId()).get(0);

        assertEquals(TaskStatus.COMPLETED, updated.getStatus());
        assertNotNull(updated.getCompletedOn());
        assertNotNull(updated.getUpdatedOn());
        assertEquals(ActivityType.PROJECT_TASK_COMPLETED, activity.getType());
    }

    public Task generateTask(User user) {
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
        return taskRepository.save(task);
    }

    public Project generateProject(User user) {
        Project project = Project.builder()
                .title("Project")
                .description("Description")
                .user(user)
                .status(ProjectStatus.ACTIVE)
                .createdOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .updatedOn(LocalDateTime.now())
                .projectVisibility(ProjectVisibility.PRIVATE)
                .completionPercent(0)
                .deleted(false)
                .build();
        return projectRepository.save(project);
    }
}
