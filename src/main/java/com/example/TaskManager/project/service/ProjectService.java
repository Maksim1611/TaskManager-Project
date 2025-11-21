package com.example.TaskManager.project.service;

import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.analytics.service.ProjectAnalyticsService;
import com.example.TaskManager.project.event.ProjectOverdueEvent;
import com.example.TaskManager.project.event.ProjectUpcomingDeadlineEvent;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.repository.ProjectRepository;
import com.example.TaskManager.tag.model.Tag;
import com.example.TaskManager.tag.repository.TagRepository;
import com.example.TaskManager.tag.service.TagService;
import com.example.TaskManager.task.event.TaskOverdueEvent;
import com.example.TaskManager.task.event.TaskUpcomingDeadlineEvent;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.repository.TaskRepository;
import com.example.TaskManager.task.service.TaskService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.CreateProjectRequest;
import com.example.TaskManager.web.dto.EditProjectRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TagService tagService;
    private final ActivityService activityService;
    private final TaskService taskService;
    private final ProjectAnalyticsService projectAnalyticsService;
    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, TagService tagService, ActivityService activityService, TaskService taskService, ProjectAnalyticsService projectAnalyticsService, TaskRepository taskRepository, TagRepository tagRepository, ApplicationEventPublisher eventPublisher, UserService userService) {
        this.projectRepository = projectRepository;
        this.tagService = tagService;
        this.activityService = activityService;
        this.taskService = taskService;
        this.projectAnalyticsService = projectAnalyticsService;
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;
        this.eventPublisher = eventPublisher;
        this.userService = userService;
    }

    @Transactional
    public void createProject(@Valid CreateProjectRequest createProjectRequest, User user) {
        Optional<Project> optional = this.projectRepository.findByTitle(createProjectRequest.getTitle());

        if (optional.isPresent()) {
            throw new RuntimeException("Project [%s] already exists".formatted(createProjectRequest.getTitle()));
        }

        List<Tag> tags = tagService.parseTags(createProjectRequest.getTagNames());

        Project project = Project.builder()
                .title(createProjectRequest.getTitle())
                .description(createProjectRequest.getDescription())
                .createdOn(LocalDateTime.now())
                .dueDate(createProjectRequest.getDueDate())
                .status(createProjectRequest.getProjectStatus())
                .user(user)
                .tags(tags)
                .completionPercent(0)
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .projectVisibility(createProjectRequest.getProjectVisibility())
                .members(new ArrayList<>())
                .build();

        this.projectRepository.save(project);
        tagService.saveTags(project.getTags(), project);
        project.getMembers().add(user);

        activityService.createActivity(ActivityType.PROJECT_CREATED, user, project);
        projectAnalyticsService.upsertProjects(user.getId());
    }

    public List<Project> getRecentProjects(User user) {

        List<Project> projects = projectRepository.findAllByUserIdAndDeletedOrderByDueDateAsc(user.getId(), false);
        List<Project> recentProjects = new ArrayList<>();

        if (projects.size() > 2) {
            for (int i = 0; i < 2; i++) {
                recentProjects.add(projects.get(i));
            }
        } else {
            recentProjects = projects;
        }

        return recentProjects;
    }

    public void calculateCompletionPercent(Project project) {
        int tasks = taskService.getTasksByProjectIdNotDeleted(project.getId()).size();
        int completedTasksCount = Math.toIntExact(project.getTasks().stream().filter(task -> task.getStatus().equals(TaskStatus.COMPLETED)).count());

        if (tasks == 0) {
            project.setCompletionPercent(0);
            return;
        }

        double percent = ((double)completedTasksCount / tasks) * 100;

        project.setCompletionPercent(Math.toIntExact(Math.round(percent)));
        update(project);
    }

    public void update(Project project) {
        project.setUpdatedOn(LocalDateTime.now());
        this.projectRepository.save(project);
        projectAnalyticsService.upsertProjects(project.getUser().getId());
    }

    public Project getByIdNotDeleted(UUID id) {
        return this.projectRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new RuntimeException("Project [%s] does not exist".formatted(id)));
    }

    public void delete(UUID projectId) {
        Project project = getByIdNotDeleted(projectId);
        if (project.isDeleted()) {
            throw new RuntimeException("Project [%s] is already deleted.".formatted(projectId));
        }

        taskRepository.deleteAll(project.getTasks());
        assert project.getTags() != null;
        tagRepository.deleteAll(project.getTags());
        activityService.createActivity(ActivityType.PROJECT_DELETED, project.getUser(), project);
        projectRepository.delete(project);
    }

    public List<Project> getAllByUserIdAndDeletedFalse(UUID id) {
        return projectRepository.findAllByUserIdAndDeletedFalse(id);
    }

    public List<Project> getAllByUserIdAndDeletedFalseAndStatus(UUID id, ProjectStatus projectStatus) {
        return projectRepository.findAllByUserIdAndDeletedFalseAndStatus(id, projectStatus);
    }

    public void editProject(@Valid EditProjectRequest editProjectRequest, UUID id) {
        Project project = getByIdNotDeleted(id);
        project.setTitle(editProjectRequest.getTitle());
        project.setDescription(editProjectRequest.getDescription());
        project.setProjectVisibility(editProjectRequest.getProjectVisibility());
        project.setStatus(editProjectRequest.getProjectStatus());

        List<Tag> tags = tagService.parseTags(editProjectRequest.getTagNames());;
        tagService.saveTags(tags, project);

        project.setTags(tags);

        if (editProjectRequest.getDueDate() == null) {
            update(project);
            activityService.createActivity(ActivityType.PROJECT_UPDATED, userService.getById(project.getUser().getId()), project);
        } else {
            project.setDueDate(editProjectRequest.getDueDate());
            activityService.createActivity(ActivityType.PROJECT_UPDATED, userService.getById(project.getUser().getId()), project);
            update(project);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void checkForOverdueProjects() {
        LocalDateTime now = LocalDateTime.now();

        List<Project> projects = projectRepository
                .findByDueDateBeforeAndStatusNotAndDeletedFalse(now, ProjectStatus.COMPLETED);

        for (Project project : projects) {
            processProject(project);
        }
    }

    private void processProject(Project project) {
        LocalDateTime now = LocalDateTime.now();

        if (project.getDueDate().isBefore(now)
                && project.getStatus() != ProjectStatus.COMPLETED
                && !project.isDeleted()) {

            project.setStatus(ProjectStatus.OVERDUE);

            if (!project.isNotifiedOverdue()) {
                eventPublisher.publishEvent(new ProjectOverdueEvent(
                        project.getId(),
                        project.getUser().getId(),
                        project.getTitle(),
                        project.getDueDate()
                ));
                project.setNotifiedOverdue(true);
            }

            projectRepository.save(project);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void upcomingDeadlineChecker() {
        List<Project> projects = projectRepository.findByStatusNotAndDeletedFalse(ProjectStatus.COMPLETED);

        for (Project project : projects) {
            processUpcomingDeadline(project);
        }
    }

    private void processUpcomingDeadline(Project project) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration;

        duration = Duration.between(now, project.getDueDate());

        if (!project.isNotifiedUpcoming()) {
            if (duration.toHours() >= 23 && duration.toHours() <= 24) {
                eventPublisher.publishEvent(new ProjectUpcomingDeadlineEvent(
                        project.getId(),
                        project.getUser().getId(),
                        project.getTitle(),
                        project.getDueDate()
                ));
                project.setNotifiedUpcoming(true);
                projectRepository.save(project);
            }
        }

    }

    public String getMembersToString(UUID id) {
        Project project = getByIdNotDeleted(id);
        List<String> list = project.getMembers().stream().map(User::getUsername).toList();

        return String.join(", ", list);
    }

    public String getProjectTagsAsString(Project project) {
        List<Tag> tags = project.getTags();
        List<String> parsed = tags.stream().map(Tag::getTitle).toList();
        return String.join(", ", parsed);
    }

    public void completeProject(UUID id) {
        Project project = getByIdNotDeleted(id);

        if (project.getStatus() != ProjectStatus.COMPLETED) {
            project.setStatus(ProjectStatus.COMPLETED);
            update(project);
        }

    }
}
