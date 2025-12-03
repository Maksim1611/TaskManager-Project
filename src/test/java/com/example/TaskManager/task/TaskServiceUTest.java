package com.example.TaskManager.task;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.repository.ActivityRepository;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.analytics.service.TaskAnalyticsService;
import com.example.TaskManager.exception.task.TaskAlreadyExistException;
import com.example.TaskManager.exception.task.TaskNotFoundException;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.task.event.TaskOverdueEvent;
import com.example.TaskManager.task.event.TaskUpcomingDeadlineEvent;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskPriority;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.repository.TaskRepository;
import com.example.TaskManager.task.service.TaskService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.utils.UserUtils;
import com.example.TaskManager.web.dto.CreateTaskRequest;
import com.example.TaskManager.web.dto.EditTaskRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.ModelAndView;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceUTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserService userService;
    @Mock
    private ActivityService activityService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private TaskAnalyticsService taskAnalyticsService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void whenCreateTask_andRepositoryReturnsOptionalPresent_thenThrowException() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("title")
                .build();

        Task task = Task.builder()
                .title("title")
                        .build();

        when(taskRepository.findByTitleAndProjectNullAndDeletedFalse(request.getTitle())).thenReturn(Optional.of(task));

        assertThrows(TaskAlreadyExistException.class, () -> taskService.createTask(request, null,null));
    }

    @Test
    void whenCompleteTask_andRepositoryReturnsOptionalEmpty_thenThrowException() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.completeTask(taskId));
    }

    @Test
    void whenCompleteTask_andRepositoryReturnsTaskWithProjectNull_thenUpdateStatusToCompletedAndPersistInDatabase() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .build();
        when(userService.getById(userId)).thenReturn(user);

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .build();
        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.completeTask(taskId);

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertThat(task.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        assertThat(task.getCompletedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(taskRepository).save(task);
    }

    @Test
    void whenCompleteTask_andRepositoryReturnsTaskWithProject_thenUpdateStatusToCompletedAndPersistInDatabase() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Project project = Project.builder()
                .id(projectId)
                .build();

        User user = User.builder()
                .id(userId)
                .build();
        when(userService.getById(userId)).thenReturn(user);

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .project(project)
                .build();
        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.completeTask(taskId);

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertThat(task.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        assertThat(task.getCompletedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(taskRepository).save(task);

    }

    @Test
    void whenDeleteTask_andRepositoryReturnsOptionalEmpty_thenThrowException() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(taskId));
    }

    @Test
    void whenDeleteTask_andRepositoryReturnsTaskWithProjectNull_thenSetDeletedToTrueAndPersistInDatabase() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).build();

        Task task = Task.builder()
                .id(taskId)
                .project(null)
                .user(user)
                .build();
        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        assertTrue(task.isDeleted());
        assertThat(task.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(taskRepository).save(task);
    }

    @Test
    void whenDeleteTask_andRepositoryReturnsTaskWithProjectNotNull_thenSetDeletedToTrueAndPersistInDatabase() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        User user = UserUtils.randomUser();
        Project project = Project.builder().id(projectId).build();

        Task task = Task.builder()
                .id(taskId)
                .project(project)
                .user(user)
                .build();
        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));
        when(userService.getById(user.getId())).thenReturn(user);

        taskService.deleteTask(taskId);

        assertTrue(task.isDeleted());
        assertThat(task.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(taskRepository).save(task);
    }

    @Test
    void whenGetTaskById_andRepositoryReturnsOptionalEmpty_thenThrowException() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.getById(taskId));
    }

    @Test
    void whenGetTaskById_andRepositoryReturnsTask_getTask() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.getById(taskId));
    }

    @Test
    void whenChangeTaskStatus_andRepositoryReturnsOptionalEmpty_thenThrowException() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.changeStatus(taskId));
    }

    @Test
    void whenChangeTaskStatus_andRepositoryReturnsTaskWithDeletedTrue_thenThrowException() {
        UUID taskId = UUID.randomUUID();
        Task task = Task.builder().id(taskId).deleted(true).build();

        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        assertThrows(RuntimeException.class, () -> taskService.changeStatus(taskId));
    }

    @Test
    void whenChangeTaskStatus_andRepositoryReturnsTaskWithStatusTodo_thenChangeStatusToInProgressAndPersist() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();


        Task task = Task.builder()
                .id(taskId)
                .status(TaskStatus.TODO)
                .user(user)
                .build();
        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.changeStatus(taskId);

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());

        verify(taskRepository).save(task);
    }

    @Test
    void whenChangeTaskStatus_andRepositoryReturnsTaskWithStatusInProgress_thenChangeStatusToTodoAndPersist() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();


        Task task = Task.builder()
                .id(taskId)
                .status(TaskStatus.IN_PROGRESS)
                .user(user)
                .build();
        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.changeStatus(taskId);

        assertEquals(TaskStatus.TODO, task.getStatus());

        verify(taskRepository).save(task);
    }

    @Test
    void whenGetAllTasksByUserIdAndStatusNotDeleted_andTaskListIsEmpty_thenReturnEmptyList() {
        UUID userId = UUID.randomUUID();

        List<Task> emptyList = Collections.emptyList();

        List<Task> result = taskService.getAllTasksByUserIdAndProjectNull(userId);

        assertTrue(result.isEmpty());
        assertEquals(emptyList, result);
    }

    @Test
    void whenGetAllTasksByUserIdAndStatusNotDeleted_andTaskListIsNotEmpty_thenReturnTasks() {
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).build();

        List<Task> taskList = List.of(
                Task.builder().id(UUID.randomUUID()).user(user).status(TaskStatus.TODO).build(),
                Task.builder().id(UUID.randomUUID()).user(user).status(TaskStatus.TODO).build());

        when(taskRepository.findAllByUserIdAndProjectNullAndDeletedFalse(userId)).thenReturn(taskList);

        List<Task> result = taskService.getAllTasksByUserIdAndStatusNotDeleted(userId, TaskStatus.TODO);

        assertFalse(result.isEmpty());
        assertEquals(taskList, result);
    }

    @Test
    void whenEditTask_andDueDateIsNullAndProjectIsNull() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).build();

        EditTaskRequest request = EditTaskRequest.builder()
                .title("title")
                .description("description")
                .dueDate(null)
                .priority(TaskPriority.MEDIUM)
                .build();

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .title("old title")
                .description("old description")
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(TaskPriority.HIGH)
                .updatedOn(LocalDateTime.now())
                .build();

        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.editTask(taskId, request);
        verify(taskRepository).save(task);

        assertNotNull(task.getDueDate());
        assertEquals("title", task.getTitle());
        assertEquals("description", task.getDescription());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertThat(task.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void whenEditTask_andDueDateIsNullAndProjectIsNotNull() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        Project project = Project.builder().id(projectId).build();

        EditTaskRequest request = EditTaskRequest.builder()
                .title("title")
                .description("description")
                .dueDate(null)
                .priority(TaskPriority.MEDIUM)
                .build();

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .title("old title")
                .description("old description")
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(TaskPriority.HIGH)
                .project(project)
                .updatedOn(LocalDateTime.now())
                .build();

        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.editTask(taskId, request);
        verify(taskRepository).save(task);

        assertNotNull(task.getDueDate());
        assertEquals("title", task.getTitle());
        assertEquals("description", task.getDescription());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertThat(task.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void whenEditTask_andDueDateIsNotNullAndProjectIsNull() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder().id(userId).build();

        EditTaskRequest request = EditTaskRequest.builder()
                .title("title")
                .description("description")
                .dueDate(null)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(2))
                .build();

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .title("old title")
                .description("old description")
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(TaskPriority.HIGH)
                .project(null)
                .updatedOn(LocalDateTime.now())
                .build();

        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.editTask(taskId, request);

        assertNotNull(task.getDueDate());
        assertEquals(request.getDueDate(), task.getDueDate());
        assertEquals("title", task.getTitle());
        assertEquals("description", task.getDescription());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertThat(task.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(taskRepository).save(task);
    }

    @Test
    void whenEditTask_andDueDateIsNotNullAndProjectIsNotNull() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Project project = Project.builder().id(projectId).build();
        User user = User.builder().id(userId).build();

        EditTaskRequest request = EditTaskRequest.builder()
                .title("title")
                .description("description")
                .dueDate(null)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(2))
                .build();

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .title("old title")
                .description("old description")
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(TaskPriority.HIGH)
                .project(project)
                .updatedOn(LocalDateTime.now())
                .build();

        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(task));

        taskService.editTask(taskId, request);

        assertNotNull(task.getDueDate());
        assertEquals(request.getDueDate(), task.getDueDate());
        assertEquals("title", task.getTitle());
        assertEquals("description", task.getDescription());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertThat(task.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(taskRepository).save(task);
    }

    @Test
    void whenTaskIsOverdue_andNotCompleted_andNotDeleted_andNotNotified_thenMarkOverduePublishEventAndSave() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .build();

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .dueDate(LocalDateTime.now().minusDays(1))
                .status(TaskStatus.TODO)
                .deleted(false)
                .notifiedOverdue(false)
                .title("Test Task")
                .build();

        taskService.processTask(task);

        assertEquals(TaskStatus.OVERDUE, task.getStatus());
        assertTrue(task.isNotifiedOverdue());

        ArgumentCaptor<TaskOverdueEvent> eventCaptor = ArgumentCaptor.forClass(TaskOverdueEvent.class);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        verify(taskRepository).save(task);
    }

    @Test
    void whenTaskIsNotOverdue_thenDoNothing() {
        UUID taskId = UUID.randomUUID();

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .dueDate(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.TODO)
                .deleted(false)
                .notifiedOverdue(false)
                .build();

        taskService.processTask(task);

        assertEquals(TaskStatus.TODO, task.getStatus());
        assertFalse(task.isNotifiedOverdue());

        verify(eventPublisher, never()).publishEvent(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void whenTaskIsCompletedAndOverdue_thenDoNothing() {
        UUID taskId = UUID.randomUUID();

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .dueDate(LocalDateTime.now().minusDays(1))
                .status(TaskStatus.COMPLETED)
                .deleted(false)
                .notifiedOverdue(false)
                .build();

        taskService.processTask(task);

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertFalse(task.isNotifiedOverdue());

        verify(eventPublisher, never()).publishEvent(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void whenTaskIsOverdueButDeleted_thenDoNothing() {
        UUID taskId = UUID.randomUUID();

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .dueDate(LocalDateTime.now().minusDays(1))
                .status(TaskStatus.TODO)
                .deleted(true)
                .notifiedOverdue(false)
                .build();

        taskService.processTask(task);

        assertEquals(TaskStatus.TODO, task.getStatus());
        assertFalse(task.isNotifiedOverdue());

        verify(eventPublisher, never()).publishEvent(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void whenProcessUpcomingDeadline_andWhenBeforeDeadline_thenSendEventAndSaveAndSetNotifiedUpcomingTrue() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .build();

        LocalDateTime dueDate = LocalDateTime.now().plusHours(24);

        Task task = Task.builder()
                .id(taskId)
                .user(user)
                .dueDate(dueDate)
                .notifiedUpcoming(false)
                .title("test")
                .build();

        taskService.processUpcomingDeadline(task);

        ArgumentCaptor<TaskUpcomingDeadlineEvent> captor =
                ArgumentCaptor.forClass(TaskUpcomingDeadlineEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());
        TaskUpcomingDeadlineEvent event = captor.getValue();

        assertEquals(taskId, event.getTaskId());
        assertEquals(userId, event.getUserId());
        assertEquals("test", event.getTitle());

        assertTrue(task.isNotifiedUpcoming());
        verify(taskRepository).save(task);
    }

    @Test
    void whenDeadlineNotIn23To24HoursWindow_thenDoNothing() {
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).build())
                .dueDate(LocalDateTime.now().plusHours(5))
                .notifiedUpcoming(false)
                .build();

        taskService.processUpcomingDeadline(task);

        verify(eventPublisher, never()).publishEvent(any());
        verify(taskRepository, never()).save(any());
        assertFalse(task.isNotifiedUpcoming());
    }

    @Test
    void whenAlreadyNotifiedUpcoming_thenDoNothing() {
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).build())
                .dueDate(LocalDateTime.now().plusHours(24))
                .notifiedUpcoming(true)
                .build();

        taskService.processUpcomingDeadline(task);

        verify(eventPublisher, never()).publishEvent(any());
        verify(taskRepository, never()).save(any());
        assertTrue(task.isNotifiedUpcoming());
    }

    @Test
    void whenCheckIfTaskHasProjectRedirect_andProjectIsNotNull_thenRedirectToTheProject() {
        Task task = randomTask();
        when(taskRepository.findByIdAndDeletedFalse(task.getId())).thenReturn(Optional.of(task));

        task.setProject(Project.builder().id(UUID.randomUUID()).build());

        String str = taskService.checkIfTaskHasProjectRedirect(task.getId());

        assertEquals("redirect:/projects/" + task.getProject().getId() , str);
    }

    @Test
    void whenCheckIfTaskHasProjectRedirect_andProjectIsNull_thenRedirectToTheProject() {
        Task task = randomTask();
        when(taskRepository.findByIdAndDeletedFalse(task.getId())).thenReturn(Optional.of(task));

        String str = taskService.checkIfTaskHasProjectRedirect(task.getId());

        assertEquals("redirect:/tasks" , str);
    }

    @Test
    void whenCheckDeletedTaskHasProjectRedirect_andProjectIsNotNull_thenRedirectToTheProject() {
        Task task = randomTask();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        task.setProject(Project.builder().id(UUID.randomUUID()).build());

        String str = taskService.checkDeletedTaskHasProjectRedirect(task.getId());

        assertEquals("redirect:/projects/" + task.getProject().getId() , str);
    }

    @Test
    void whenCheckDeletedTaskHasProjectRedirect_andProjectIsNull_thenRedirectToTheProject() {
        Task task = randomTask();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        String str = taskService.checkDeletedTaskHasProjectRedirect(task.getId());

        assertEquals("redirect:/tasks", str);
    }

    @Test
    void whenGetOverdueTasksByUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        List<Task> tasks = List.of(randomTask(), randomTask(), randomTask());
        tasks.get(0).setStatus(TaskStatus.OVERDUE);
        tasks.get(1).setStatus(TaskStatus.OVERDUE);
        tasks.get(2).setStatus(TaskStatus.COMPLETED);

        tasks.get(0).setUser(user);
        tasks.get(1).setUser(user);
        tasks.get(2).setUser(user);

        when(taskService.getAllTasksByUserIdAndProjectNull(user.getId())).thenReturn(tasks);

        List<Task> result = taskService.getOverDueTasksByUser(user);

        assertEquals(2, result.size());
    }

    @Test
    void whenGetRecentTasksByUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        List<Task> tasks = List.of(randomTask(), randomTask(), randomTask());
        tasks.get(0).setStatus(TaskStatus.IN_PROGRESS);
        tasks.get(1).setStatus(TaskStatus.TODO);
        tasks.get(2).setStatus(TaskStatus.TODO);

        tasks.get(0).setUser(user);
        tasks.get(1).setUser(user);
        tasks.get(2).setUser(user);

        when(taskRepository.findAllByUserIdAndStatusNotAndStatusNotAndDeletedFalseAndProjectNullOrderByDueDateAsc(user.getId(), TaskStatus.OVERDUE, TaskStatus.COMPLETED)).thenReturn(tasks);

        List<Task> recentTasksGiven = tasks.stream().sorted(Comparator.comparing(Task::getCreatedOn).reversed()).limit(2).collect(Collectors.toList());

        List<Task> result = taskService.getRecentTasks(user);
        assertEquals(2, result.size());
        assertEquals(recentTasksGiven, result);
    }

    @Test
    void whenGetTaskCompletionRateLast24Hours_andCreatedTasksAre0_thenReturn0() {
        UUID userId = UUID.randomUUID();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                userId, ActivityType.TASK_CREATED, yesterday, now))
                .thenReturn(0L);

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                userId, ActivityType.TASK_COMPLETED, yesterday, now))
                .thenReturn(10L);

        double result = taskService.getTaskCompletionRateLast24Hours(userId, yesterday, now);

        assertEquals(0.0, result);
    }

    @Test
    void whenGetTaskCompletionRateLast24Hours_calculatesCorrectPercentage() {
        UUID userId = UUID.randomUUID();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                userId, ActivityType.TASK_CREATED, yesterday, now))
                .thenReturn(20L);

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                userId, ActivityType.TASK_COMPLETED, yesterday, now))
                .thenReturn(5L);

        double result = taskService.getTaskCompletionRateLast24Hours(userId, yesterday, now);

        assertEquals(25.00, result);
    }

    @Test
    void whenGetTaskCompletionRateLast24Hours_thenRoundsToTwoDecimals() {
        UUID userId = UUID.randomUUID();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                userId, ActivityType.TASK_CREATED, yesterday, now))
                .thenReturn(3L);

        when(activityRepository.countByUserIdAndDeletedFalseAndTypeAndCreatedOnBetween(
                userId, ActivityType.TASK_COMPLETED, yesterday, now))
                .thenReturn(2L);

        double result = taskService.getTaskCompletionRateLast24Hours(userId, yesterday, now);

        assertEquals(66.67, result);
    }

    @Test
    void getTasksDueThisWeek_shouldReturnCorrectCount() {
        UUID userId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfWeek = now
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        LocalDateTime endOfWeek = now
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59).withNano(999_000_000);

        when(taskRepository.findAllByUserIdAndProjectNullAndDeletedFalseAndDueDateBetweenAndStatusNotAndStatusNot(
                userId,
                startOfWeek,
                endOfWeek,
                TaskStatus.OVERDUE,
                TaskStatus.COMPLETED
        )).thenReturn(List.of(new Task(), new Task()));

        int result = taskService.getTasksDueThisWeek(userId);

        assertEquals(2, result);
    }

    @Test
    void getAllTimeCompletionRate_shouldReturnCorrectPercentage() {
        UUID userId = UUID.randomUUID();

        Task t1 = new Task();
        t1.setStatus(TaskStatus.COMPLETED);

        Task t2 = new Task();
        t2.setStatus(TaskStatus.COMPLETED);

        Task t3 = new Task();
        t3.setStatus(TaskStatus.OVERDUE);

        List<Task> tasks = List.of(t1, t2, t3);

        when(taskRepository.findAllByUserIdAndProjectNull(userId)).thenReturn(tasks);

        int result = taskService.getAllTimeCompletionRate(userId);

        assertEquals(67, result);
    }

    @Test
    void whenCalculatePercentage_shouldReturn67() {
        Task t1 = new Task(); t1.setStatus(TaskStatus.COMPLETED);
        Task t2 = new Task(); t2.setStatus(TaskStatus.COMPLETED);
        Task t3 = new Task(); t3.setStatus(TaskStatus.OVERDUE);

        List<Task> tasks = List.of(t1, t2, t3);

        int result = taskService.calculateByStatusTasksPercentageNonDeleted(TaskStatus.COMPLETED, tasks);

        assertEquals(67, result);
    }

    @Test
    void whenAllMatch_andCalculatePercentage_thenShouldReturn100() {
        Task t1 = new Task(); t1.setStatus(TaskStatus.COMPLETED);
        Task t2 = new Task(); t2.setStatus(TaskStatus.COMPLETED);

        List<Task> tasks = List.of(t1, t2);

        int result = taskService.calculateByStatusTasksPercentageNonDeleted(TaskStatus.COMPLETED, tasks);

        assertEquals(100, result);
    }

    @Test
    void whenListIsEmpty_thenCalculatePercentageShouldReturn0() {
        List<Task> tasks = List.of();

        int result = taskService.calculateByStatusTasksPercentageNonDeleted(TaskStatus.COMPLETED, tasks);

        assertEquals(0, result);
    }

    @Test
    void whenNoTasksMatchStatus_thenCalculatePercentageAndShouldReturn0() {
        Task t1 = new Task(); t1.setStatus(TaskStatus.IN_PROGRESS);
        Task t2 = new Task(); t2.setStatus(TaskStatus.OVERDUE);

        List<Task> tasks = List.of(t1, t2);

        int result = taskService.calculateByStatusTasksPercentageNonDeleted(TaskStatus.COMPLETED, tasks);

        assertEquals(0, result);
    }

    @Test
    void buildTasksPageView_shouldReturnCorrectModelAndView() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        Task t1 = new Task(); t1.setStatus(TaskStatus.COMPLETED);
        Task t2 = new Task(); t2.setStatus(TaskStatus.IN_PROGRESS);
        Task t3 = new Task(); t3.setStatus(TaskStatus.TODO);

        List<Task> tasks = List.of(t1, t2, t3);

        when(taskRepository.findAllByUserIdAndDeletedFalseOrderByCreatedOnDesc(userId))
                .thenReturn(tasks);

        Activity a1 = new Activity(); a1.setType(ActivityType.TASK_CREATED);
        Activity a2 = new Activity(); a2.setType(ActivityType.TASK_COMPLETED);

        when(activityService.getActivityByTypeAndUserId(userId, "TASK"))
                .thenReturn(List.of(a1, a2));


        ModelAndView mv = taskService.buildTasksPageView(user);

        assertEquals("tasks", mv.getViewName());
        assertEquals(user, mv.getModel().get("user"));
        assertEquals(tasks, mv.getModel().get("tasks"));

        assertEquals(1, mv.getModel().get("countCompletedTasks"));
        assertEquals(1, mv.getModel().get("countInProgressTasks"));
        assertEquals(1, mv.getModel().get("countTodoTasks"));

        assertEquals(33, mv.getModel().get("completedTasksPercentage"));
        assertEquals(33, mv.getModel().get("inProgressTasksPercentage"));
        assertEquals(0, mv.getModel().get("overdueTasksPercentage"));

        List<Activity> recent = (List<Activity>) mv.getModel().get("recentActivity");
        assertEquals(2, recent.size());
    }

    @Test
    void whenGetUpcomingTasks_shouldReturnNonCompletedNonOverdueSortedTasks() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task();
        task1.setStatus(TaskStatus.TODO);
        task1.setDueDate(now.plusDays(3));

        Task task2 = new Task();
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setDueDate(now.plusDays(1));

        Task task3 = new Task();
        task3.setStatus(TaskStatus.COMPLETED);
        task3.setDueDate(now.plusDays(5));

        Task t4 = new Task();
        t4.setStatus(TaskStatus.OVERDUE);
        t4.setDueDate(now.plusHours(5));

        when(taskRepository.findAllByUserIdAndProjectNullAndDeletedFalse(userId))
                .thenReturn(List.of(task1, task2, task3, t4));

        List<Task> result = taskService.getUpcomingTasks(userId);

        assertEquals(2, result.size());
        assertEquals(task2, result.get(0));
        assertEquals(task1, result.get(1));
    }

    @Test
    void whenGetTasksByProjectIdNotDeleted_shouldReturnTasksFromRepository() {
        UUID projectId = UUID.randomUUID();

        Task task1 = new Task();
        Task task2 = new Task();

        when(taskRepository.findAllByProjectIdAndDeletedFalse(projectId))
                .thenReturn(List.of(task1, task2));

        List<Task> result = taskService.getTasksByProjectIdNotDeleted(projectId);

        assertEquals(2, result.size());
        assertTrue(result.contains(task1));
        assertTrue(result.contains(task2));
    }

    @Test
    void whenInvokeUpcomingDeadlineChecker_shouldCallProcessUpcomingDeadlineForEachTask() {
        Task task1 = mock(Task.class);
        Task task2 = mock(Task.class);

        when(taskRepository.findByStatusNotAndDeletedFalse(TaskStatus.COMPLETED))
                .thenReturn(List.of(task1, task2));

        TaskService spyService = Mockito.spy(taskService);

        doNothing().when(spyService).processUpcomingDeadline(any(Task.class));

        spyService.upcomingDeadlineChecker();

        verify(taskRepository)
                .findByStatusNotAndDeletedFalse(TaskStatus.COMPLETED);

        verify(spyService).processUpcomingDeadline(task1);
        verify(spyService).processUpcomingDeadline(task2);
    }

    @Test
    void checkForOverdueTasks_shouldCallProcessTaskForEachTask() {
        Task t1 = mock(Task.class);
        Task t2 = mock(Task.class);

        when(taskRepository.findByDueDateBeforeAndStatusNotAndDeletedFalse(
                any(LocalDateTime.class),
                eq(TaskStatus.COMPLETED)))
                .thenReturn(List.of(t1, t2));

        TaskService spyService = Mockito.spy(taskService);
        doNothing().when(spyService).processTask(any(Task.class));

        spyService.checkForOverdueTasks();

        verify(taskRepository).findByDueDateBeforeAndStatusNotAndDeletedFalse(
                any(LocalDateTime.class), eq(TaskStatus.COMPLETED));

        verify(spyService).processTask(t1);
        verify(spyService).processTask(t2);
    }

    public Task randomTask() {
        return Task.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).build())
                .dueDate(LocalDateTime.now().plusHours(24))
                .notifiedUpcoming(false)
                .title("test")
                .description("test")
                .status(TaskStatus.TODO)
                .deleted(false)
                .project(null)
                .createdOn(LocalDateTime.now())
                .completedOn(null)
                .notifiedOverdue(false)
                .priority(TaskPriority.LOW)
                .updatedOn(LocalDateTime.now())
                .notifiedUpcoming(false)
                .build();
    }

}
