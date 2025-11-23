package com.example.TaskManager.task.service;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.repository.ActivityRepository;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.analytics.service.TaskAnalyticsService;
import com.example.TaskManager.exception.TaskAlreadyExistException;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.task.event.TaskOverdueEvent;
import com.example.TaskManager.task.event.TaskUpcomingDeadlineEvent;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskPriority;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.repository.TaskRepository;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.CreateTaskRequest;
import com.example.TaskManager.web.dto.EditTaskRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ActivityService activityService;
    private final TaskAnalyticsService taskAnalyticsService;
    private final ApplicationEventPublisher eventPublisher;
    private final ActivityRepository activityRepository;

    public TaskService(TaskRepository taskRepository, UserService userService, ActivityService activityService, TaskAnalyticsService taskAnalyticsService, ApplicationEventPublisher eventPublisher, ActivityRepository activityRepository ) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.activityService = activityService;
        this.taskAnalyticsService = taskAnalyticsService;
        this.eventPublisher = eventPublisher;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public Task createTask(@Valid CreateTaskRequest createTaskRequest, User user, Project project) {
        Optional<Task> taskOptional = taskRepository.findByTitleAndProjectNullAndDeletedFalse(createTaskRequest.getTitle());

        if (taskOptional.isPresent()) {
            throw new TaskAlreadyExistException("Task with title [%s] already exists".formatted(createTaskRequest.getTitle()));
        }

        Task task = Task.builder()
                .title(createTaskRequest.getTitle())
                .description(createTaskRequest.getDescription())
                .status(TaskStatus.TODO)
                .priority(TaskPriority.valueOf(createTaskRequest.getPriority()))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .dueDate(createTaskRequest.getDueDate())
                .user(user)
                .deleted(false)
                .project(project)
                .build();



        Task save = this.taskRepository.save(task);

        if (task.getProject() == null) {
            createActivityBasedOnProjectStatus(task, ActivityType.TASK_CREATED);
            taskAnalyticsService.upsertTasks(task.getUser().getId());
        } else {
            createActivityBasedOnProjectStatus(task, ActivityType.PROJECT_TASK_CREATED);
        }

        return save;
    }

    public List<Task> getOverDueTasksByUser(User user) {
        return getAllTasksByUserIdAndProjectNull(user.getId()).stream().filter(task -> task.getStatus().getDisplayName().equals("Overdue")).toList();
    }

    public List<Task> getRecentTasks(User user) {
        List<Task> tasks = taskRepository.findAllByUserIdAndStatusNotAndStatusNotAndDeletedFalseAndProjectNullOrderByDueDateAsc(user.getId(), TaskStatus.OVERDUE, TaskStatus.COMPLETED);
        return tasks.stream().sorted(Comparator.comparing(Task::getCreatedOn).reversed()).limit(2).collect(Collectors.toList());
    }

    public void update(Task task) {
        task.setUpdatedOn(LocalDateTime.now());
        this.taskRepository.save(task);
        taskAnalyticsService.upsertTasks(task.getUser().getId());
    }

    public void completeTask(UUID taskId) {
        Task task = getByIdNotDeleted(taskId);

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedOn(LocalDateTime.now());
        task.setUpdatedOn(LocalDateTime.now());
        update(task);

        if (task.getProject() == null) {
            createActivityBasedOnProjectStatus(task, ActivityType.TASK_COMPLETED);
        } else {
            createActivityBasedOnProjectStatus(task, ActivityType.PROJECT_TASK_COMPLETED);
        }
    }

    public String checkIfTaskHasProjectRedirect(UUID id) {
        Task task = getByIdNotDeleted(id);

        if (task.getProject() != null) {
            return "redirect:/projects/" + task.getProject().getId();
        }
        return "redirect:/tasks";
    }

    public String checkDeletedTaskHasProjectRedirect(UUID id) {
        Task task = getById(id);

        if (task.getProject() != null) {
            return "redirect:/projects/" + task.getProject().getId();
        }
        return "redirect:/tasks";
    }

    private Task getById(UUID id) {
        return this.taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task with id [%s] does not exist".formatted(id.toString())));
    }

    public void deleteTask(UUID id) {
        Task task = getByIdNotDeleted(id);

        if (task.getProject() == null) {
            createActivityBasedOnProjectStatus(task, ActivityType.TASK_DELETED);
        } else {
            createActivityBasedOnProjectStatus(task, ActivityType.PROJECT_TASK_DELETED);
            task.setProject(null);
        }

        task.setDeleted(true);
        task.setUpdatedOn(LocalDateTime.now());
        update(task);

    }

    public void changeStatus(UUID id) {
        Task task = this.getByIdNotDeleted(id);

        if (task.isDeleted()) {
            throw new RuntimeException("Task with id [%s] is already deleted".formatted(id));
        }

        if (task.getStatus().equals(TaskStatus.TODO)) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        } else if (task.getStatus().equals(TaskStatus.IN_PROGRESS)) {
            task.setStatus(TaskStatus.TODO);
        }

        update(task);
    }

    public Task getByIdNotDeleted(UUID id) {
        return this.taskRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new RuntimeException("Task with id [%s] not found".formatted(id)));
    }

    @Scheduled(fixedRate = 60000)
    public void checkForOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();

        List<Task> tasks = taskRepository
                .findByDueDateBeforeAndStatusNotAndDeletedFalse(now, TaskStatus.COMPLETED);

        for (Task task : tasks) {
            processTask(task);
        }
    }

    private void processTask(Task task) {
        LocalDateTime now = LocalDateTime.now();

        if (task.getDueDate().isBefore(now)
                && task.getStatus() != TaskStatus.COMPLETED
                && !task.isDeleted()) {

            task.setStatus(TaskStatus.OVERDUE);

            if (!task.isNotifiedOverdue()) {
                eventPublisher.publishEvent(new TaskOverdueEvent(
                        task.getId(),
                        task.getUser().getId(),
                        task.getTitle(),
                        task.getDueDate()
                ));
                task.setNotifiedOverdue(true);
            }

            taskRepository.save(task);
        }
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void upcomingDeadlineChecker() {
        List<Task> tasks = taskRepository.findByStatusNotAndDeletedFalse(TaskStatus.COMPLETED);

        for (Task task : tasks) {
            processUpcomingDeadline(task);
        }
    }

    private void processUpcomingDeadline(Task task) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration;

        duration = Duration.between(now, task.getDueDate());

        if (!task.isNotifiedUpcoming()) {
            if (duration.toHours() >= 23 && duration.toHours() <= 24) {
                eventPublisher.publishEvent(new TaskUpcomingDeadlineEvent(
                        task.getId(),
                        task.getUser().getId(),
                        task.getTitle(),
                        task.getDueDate()
                ));
                task.setNotifiedUpcoming(true);
                taskRepository.save(task);
            }
        }

    }

    public List<Task> getUpcomingTasks(UUID userId) {
        List<Task> tasks = getAllTasksByUserIdAndProjectNull(userId);
        return tasks.stream().sorted(Comparator.comparing(Task::getDueDate)).collect(Collectors.toList());
    }

    public List<Task> getTasksByProjectIdNotDeleted(UUID id) {
        return taskRepository.findAllByProjectIdAndDeletedFalse(id);
    }

    public ModelAndView buildTasksPageView(User user) {

        ModelAndView mv = new ModelAndView("tasks");
        List<Task> tasks = getAllTasksByUserIdWithNoProject(user.getId());
        List<Activity> recentActivity = activityService.getActivityByTypeAndUserId(user.getId(), "TASK").stream()
                .filter(a -> !a.getType().name().contains("PROJECT")).limit(3).toList();

        mv.addObject("user", user);
        mv.addObject("tasks", tasks);
        mv.addObject("countCompletedTasks", getAllTasksByUserIdAndStatusNotDeleted(user.getId(), TaskStatus.COMPLETED).size());
        mv.addObject("countInProgressTasks", getAllTasksByUserIdAndStatusNotDeleted(user.getId(), TaskStatus.IN_PROGRESS).size());
        mv.addObject("countTodoTasks", getAllTasksByUserIdAndStatusNotDeleted(user.getId(), TaskStatus.TODO).size());
        mv.addObject("completedTasksPercentage", calculateByStatusTasksPercentageNonDeleted( TaskStatus.COMPLETED, tasks));
        mv.addObject("inProgressTasksPercentage", calculateByStatusTasksPercentageNonDeleted(TaskStatus.IN_PROGRESS, tasks));
        mv.addObject("overdueTasksPercentage", calculateByStatusTasksPercentageNonDeleted(TaskStatus.OVERDUE, tasks));
        mv.addObject("recentActivity", recentActivity);

        return mv;
    }

    public List<Task> getAllTasksByUserIdAndStatusNotDeleted(UUID userId ,TaskStatus status) {
        return getAllTasksByUserIdAndProjectNull(userId).stream().filter(t -> t.getStatus().equals(status)).collect(Collectors.toList());
    }

    public List<Task> getAllTasksByUserIdAndProjectNull(UUID userId) {
        return taskRepository.findAllByUserIdAndProjectNullAndDeletedFalse(userId);
    }

    private List<Task> getAllTasksByUserIdWithNoProject(UUID id) {
        List<Task> tasks = taskRepository.findAllByUserIdAndDeletedFalseOrderByCreatedOnDesc(id);

        return tasks.stream().filter(t -> t.getProject() == null).toList();
    }


    private int calculateByStatusTasksPercentageNonDeleted(TaskStatus status, List<Task> tasks) {
        int tasksByStatus = Math.toIntExact(tasks.stream().filter(t -> t.getStatus() == status).count());

        if (tasksByStatus == 0) {
            return 0;
        }

        double calc = ((double) tasksByStatus / tasks.size()) * 100;

        return Math.toIntExact(Math.round(calc));
    }

    @Transactional
    public void editTask(UUID id, @Valid EditTaskRequest editTaskRequest) {
        Task task = getByIdNotDeleted(id);
        task.setTitle(editTaskRequest.getTitle());
        task.setDescription(editTaskRequest.getDescription());
        task.setPriority(editTaskRequest.getPriority());

        if (editTaskRequest.getDueDate() == null) {
            taskRepository.save(task);
            if (task.getProject() == null) {
                createActivityBasedOnProjectStatus(task, ActivityType.TASK_UPDATED);
            } else {
                createActivityBasedOnProjectStatus(task, ActivityType.PROJECT_TASK_UPDATED);
            }
            return;
        }

        task.setDueDate(editTaskRequest.getDueDate());

        if (task.getProject() == null) {
            createActivityBasedOnProjectStatus(task, ActivityType.TASK_UPDATED);
        } else {
            createActivityBasedOnProjectStatus(task, ActivityType.PROJECT_TASK_UPDATED);
        }
        taskRepository.save(task);
    }

    public double getTaskCompletionRateLast24Hours(UUID userId, LocalDateTime yesterday, LocalDateTime now) {
        long created = activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                userId, ActivityType.TASK_CREATED, yesterday, now);

        long completed = activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                userId, ActivityType.TASK_COMPLETED, yesterday, now);

        if (created == 0) {
            return 0;
        }

        double percentage = (double) completed / created * 100;
        return Math.round(percentage * 100.0) / 100.0;
    }

    public int getAllTimeCompletionRate(UUID userId) {
        int tasks = taskRepository.findAllByUserIdAndProjectNull(userId).size();
        int completed = (int) taskRepository.findAllByUserIdAndProjectNull(userId).stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

        double calc = (double) completed / tasks * 100;

        return Math.toIntExact(Math.round(calc));
    }

    public int getTasksDueThisWeek(UUID userId) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfWeek = now
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        LocalDateTime endOfWeek = now
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59).withNano(999_000_000);

        return taskRepository.findAllByUserIdAndProjectNullAndDeletedFalseAndDueDateBetweenAndStatusNot(
                userId, startOfWeek, endOfWeek, TaskStatus.OVERDUE).size();
    }

    private void createActivityBasedOnProjectStatus(Task task, ActivityType activityType) {

        if (task.getProject() == null) {
            activityService.createActivity(activityType, userService.getById(task.getUser().getId()), task);
        } else {
            activityService.createActivity(activityType, userService.getById(task.getUser().getId()), task);
        }

    }
}
