package com.example.TaskManager.project;

import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.analytics.service.ProjectAnalyticsService;
import com.example.TaskManager.exception.MemberAlreadyExistException;
import com.example.TaskManager.exception.user.MemberNotFoundException;
import com.example.TaskManager.project.event.ProjectOverdueEvent;
import com.example.TaskManager.project.event.ProjectUpcomingDeadlineEvent;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.model.ProjectVisibility;
import com.example.TaskManager.project.repository.ProjectRepository;
import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.tag.model.Tag;
import com.example.TaskManager.tag.repository.TagRepository;
import com.example.TaskManager.tag.service.TagService;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskStatus;
import com.example.TaskManager.task.repository.TaskRepository;
import com.example.TaskManager.task.service.TaskService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.CreateProjectRequest;
import com.example.TaskManager.web.dto.EditProjectRequest;
import com.example.TaskManager.web.dto.InviteMemberRequest;
import com.example.TaskManager.web.dto.RemoveMemberRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceUTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TagService tagService;
    @Mock
    private ActivityService activityService;
    @Mock
    private TaskService taskService;
    @Mock
    private ProjectAnalyticsService projectAnalyticsService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void whenCreateProject_andRepositoryReturnsOptionalPresent_thenThrowException() {
        UUID id = UUID.randomUUID();
        CreateProjectRequest request = CreateProjectRequest.builder()
                .title("Title")
                .build();

        Project project = Project.builder().id(id).build();
        when(projectRepository.findByTitle(request.getTitle())).thenReturn(Optional.of(project));

        assertThrows(RuntimeException.class, () -> projectService.createProject(request ,null));
    }

    @Test
    void whenCreateProject_andRepositoryReturnsOptionalEmpty_thenCreateProjectAndPersistInDatabase() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .title("Title")
                .description("Description")
                .dueDate(LocalDateTime.now())
                .projectStatus(ProjectStatus.ON_HOLD)
                .projectVisibility(ProjectVisibility.PUBLIC)
                .tags("")
                .build();

        when(projectRepository.findByTitle(request.getTitle())).thenReturn(Optional.empty());

        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);

        projectService.createProject(request ,user);

        verify(projectRepository).save(captor.capture());
        Project project = captor.getValue();

        List<Tag> tags = tagService.parseTags(request.getTagNames());

        assertEquals(request.getTitle(), project.getTitle());
        assertEquals(request.getDescription(), project.getDescription());
        assertNotNull(project.getCreatedOn());
        assertNotNull(project.getUpdatedOn());
        assertThat(request.getDueDate()).isCloseTo(project.getDueDate(), within(1, ChronoUnit.SECONDS));
        assertEquals(request.getProjectStatus(), project.getStatus());
        assertEquals(tags, project.getTags());
        assertEquals(request.getProjectVisibility(), project.getProjectVisibility());
    }

    @Test
    void whenEditProject_andDueDateIsNull_thenEditProjectAndPersistInDatabase() {
        EditProjectRequest request = EditProjectRequest.builder()
                .title("Title")
                .description("Description")
                .dueDate(null)
                .projectStatus(ProjectStatus.ON_HOLD)
                .projectVisibility(ProjectVisibility.PUBLIC)
                .tags("Tag")
                .build();

        UUID id = UUID.randomUUID();
        Project project = Project.builder()
                .id(id)
                .user(User.builder().id(UUID.randomUUID()).build())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(project));

        List<Tag> tags = tagService.parseTags(request.getTagNames());

        projectService.editProject(request , id);

        verify(tagService).saveTags(tags, project);

        assertNull(request.getDueDate());
        assertNotNull(project.getDueDate());
        assertNotNull(project.getUpdatedOn());
        assertEquals(request.getTitle(), project.getTitle());
        assertEquals(request.getDescription(), project.getDescription());
        assertEquals(request.getProjectStatus(), project.getStatus());
        assertEquals(request.getProjectVisibility(), project.getProjectVisibility());
        assertEquals(tags, project.getTags());

        verify(projectRepository).save(project);
    }

    @Test
    void whenEditProject_andDueDateIsNotNull_thenEditProjectAndPersistInDatabase() {
        EditProjectRequest request = EditProjectRequest.builder()
                .title("Title")
                .description("Description")
                .dueDate(LocalDateTime.now().plusDays(2))
                .projectStatus(ProjectStatus.ON_HOLD)
                .projectVisibility(ProjectVisibility.PUBLIC)
                .tags("Tag")
                .build();

        UUID id = UUID.randomUUID();
        Project project = Project.builder()
                .id(id)
                .user(User.builder().id(UUID.randomUUID()).build())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(project));

        List<Tag> tags = tagService.parseTags(request.getTagNames());

        projectService.editProject(request , id);

        verify(tagService).saveTags(tags, project);

        assertNotNull(project.getDueDate());
        assertNotNull(project.getUpdatedOn());
        assertEquals(request.getTitle(), project.getTitle());
        assertEquals(request.getDescription(), project.getDescription());
        assertEquals(request.getProjectStatus(), project.getStatus());
        assertEquals(request.getProjectVisibility(), project.getProjectVisibility());
        assertEquals(tags, project.getTags());

        verify(projectRepository).save(project);
    }

    @Test
    void overdueProject_andNotNotified_shouldBecomeOverdue_andSendEvent() {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Test");
        project.setUser(User.builder().id(UUID.randomUUID()).build());
        project.setDueDate(LocalDateTime.now().minusDays(1));
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setDeleted(false);
        project.setNotifiedOverdue(false);

        projectService.processProject(project);

        assertEquals(ProjectStatus.OVERDUE, project.getStatus());
        assertTrue(project.isNotifiedOverdue());

        verify(eventPublisher, times(1))
                .publishEvent(any(ProjectOverdueEvent.class));

        verify(projectRepository).save(project);
    }

    @Test
    void overdueProject_andAlreadyNotified_shouldNotSendEvent() {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setUser(User.builder().id(UUID.randomUUID()).build());
        project.setTitle("Test");
        project.setDueDate(LocalDateTime.now().minusDays(1));
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setDeleted(false);
        project.setNotifiedOverdue(true);

        projectService.processProject(project);

        verify(eventPublisher, never()).publishEvent(any());
        verify(projectRepository).save(project);
    }

    @Test
    void notOverdueProject_shouldNotBecomeOverdue() {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setUser(User.builder().id(UUID.randomUUID()).build());
        project.setTitle("Test");
        project.setDueDate(LocalDateTime.now().plusDays(2));
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setDeleted(false);
        project.setNotifiedOverdue(false);

        projectService.processProject(project);

        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void overdueProject_shouldBecomeOverdue_andSendEvent() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Project project = Project.builder()
                .id(UUID.randomUUID())
                .title("Test")
                .user(user)
                .dueDate(LocalDateTime.now().minusDays(1))
                .status(ProjectStatus.IN_PROGRESS)
                .deleted(false)
                .notifiedOverdue(false)
                .build();

        projectService.processProject(project);

        assertEquals(ProjectStatus.OVERDUE, project.getStatus());
        assertTrue(project.isNotifiedOverdue());

        verify(eventPublisher).publishEvent(any(ProjectOverdueEvent.class));
        verify(projectRepository).save(project);
    }

    @Test
    void inviteMember_userDoesNotExist_shouldThrow() {

        UUID projectId = UUID.randomUUID();

        Project project = Project.builder()
                .id(projectId)
                .members(new ArrayList<>())
                .build();

        when(projectRepository.findByIdAndDeletedFalse(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        InviteMemberRequest req = new InviteMemberRequest("missing");
        User owner = User.builder().username("owner").build();

        assertThrows(MemberNotFoundException.class,
                () -> projectService.inviteMember(req, projectId, owner));
    }

    @Test
    void whenInviteMember_andUserIsAlreadyInProject_thenThrowException() {
        UUID projectId = UUID.randomUUID();
        User existing = User.builder().id(UUID.randomUUID()).username("exist").build();

        Project project = Project.builder()
                .id(projectId)
                .members(List.of(existing))
                .build();

        when(projectRepository.findByIdAndDeletedFalse(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findByUsername("exist"))
                .thenReturn(Optional.of(existing));

        InviteMemberRequest req = InviteMemberRequest.builder().username("exist").build();
        User owner = User.builder().username("owner").build();

        assertThrows(MemberAlreadyExistException.class,
                () -> projectService.inviteMember(req, projectId, owner));
    }

    @Test
    void inviteMember_invitingYourself_shouldThrow() {

        UUID projectId = UUID.randomUUID();

        Project project = Project.builder()
                .id(projectId)
                .members(new ArrayList<>())
                .build();

        User owner = User.builder().username("ivan").build();

        when(projectRepository.findByIdAndDeletedFalse(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(owner));

        InviteMemberRequest req = new InviteMemberRequest("ivan");

        assertThrows(MemberAlreadyExistException.class,
                () -> projectService.inviteMember(req, projectId, owner));
    }

    @Test
    void inviteMember_success_shouldAddMember() {

        UUID projectId = UUID.randomUUID();

        User owner = User.builder().username("owner").build();
        User invited = User.builder().username("newUser").build();

        Project project = Project.builder()
                .id(projectId)
                .members(new ArrayList<>())
                .user(owner)
                .build();

        when(projectRepository.findByIdAndDeletedFalse(projectId))
                .thenReturn(Optional.of(project));

        when(userRepository.findByUsername("newUser"))
                .thenReturn(Optional.of(invited));

        InviteMemberRequest req = new InviteMemberRequest("newUser");

        projectService.inviteMember(req, projectId, owner);

        assertTrue(project.getMembers().contains(invited));

        verify(projectRepository).save(project);
    }

    @Test
    void getRecentProjects_whenMoreThanTwo_thenReturnTwo() {

        User user = User.builder().id(UUID.randomUUID()).build();

        Project p1 = Project.builder().status(ProjectStatus.IN_PROGRESS).build();
        Project p2 = Project.builder().status(ProjectStatus.IN_PROGRESS).build();
        Project p3 = Project.builder().status(ProjectStatus.IN_PROGRESS).build();

        List<Project> all = List.of(p1, p2, p3);

        when(projectRepository.findAllByUserIdAndDeletedOrderByDueDateAsc(user.getId(), false))
                .thenReturn(all);

        List<Project> result = projectService.getRecentProjects(user);

        assertEquals(2, result.size());
        assertEquals(p1, result.get(0));
        assertEquals(p2, result.get(1));
    }

    @Test
    void getRecentProjects_whenTwoOrLess_thenReturnAll() {

        User user = User.builder().id(UUID.randomUUID()).build();

        Project p1 = Project.builder().status(ProjectStatus.IN_PROGRESS).build();
        Project p2 = Project.builder().status(ProjectStatus.IN_PROGRESS).build();

        List<Project> all = List.of(p1, p2);

        when(projectRepository.findAllByUserIdAndDeletedOrderByDueDateAsc(user.getId(), false))
                .thenReturn(all);

        List<Project> result = projectService.getRecentProjects(user);

        assertEquals(2, result.size());
        assertEquals(all, result);
    }

    @Test
    void getRecentProjects_thenFilterOverdueAndCompleted() {

        User user = User.builder().id(UUID.randomUUID()).build();

        Project project1 = Project.builder().status(ProjectStatus.IN_PROGRESS).build();
        Project project2 = Project.builder().status(ProjectStatus.IN_PROGRESS).build();

        Project overdue = Project.builder().status(ProjectStatus.OVERDUE).build();
        Project completed = Project.builder().status(ProjectStatus.COMPLETED).build();

        List<Project> all = List.of(project1, overdue, project2, completed);

        when(projectRepository.findAllByUserIdAndDeletedOrderByDueDateAsc(user.getId(), false))
                .thenReturn(all);

        List<Project> result = projectService.getRecentProjects(user);

        assertEquals(2, result.size());
        assertTrue(result.contains(project1));
        assertTrue(result.contains(project2));
    }

    @Test
    void whenDeleteProject_andProjectAlreadyDeleted_thenThrowException() {
        UUID id = UUID.randomUUID();

        Project project = Project.builder()
                .id(id)
                .deleted(true)
                .tasks(new ArrayList<>())
                .tags(new ArrayList<>())
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(project));

        assertThrows(RuntimeException.class,
                () -> projectService.delete(id));
    }

    @Test
    void whenDeleteProject_andDeleteIsSuccessful_thenDeleteTasksTagsActivityAndProject() {

        UUID id = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();

        Project project = Project.builder()
                .id(id)
                .deleted(false)
                .tasks(new ArrayList<>())
                .tags(new ArrayList<>())
                .user(owner)
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(project));

        projectService.delete(id);

        verify(taskRepository).deleteAll(project.getTasks());
        verify(tagRepository).deleteAll(project.getTags());
        verify(activityService).createActivity(ActivityType.PROJECT_DELETED, owner, project);
        verify(projectRepository).delete(project);
    }

    @Test
    void whenRemoveMember_andUserDoesNotExist_thenThrowException() {

        UUID id = UUID.randomUUID();

        Project project = Project.builder()
                .id(id)
                .members(new ArrayList<>())
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(project));

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.empty());

        RemoveMemberRequest req = new RemoveMemberRequest("user");
        User owner = User.builder().username("owner").build();

        assertThrows(MemberNotFoundException.class,
                () -> projectService.removeMember(id, owner, req));
    }

    @Test
    void whenRemoveMember_andUserNotInProject_thenThrowException() {

        UUID id = UUID.randomUUID();

        User owner = User.builder().username("owner").build();
        User someone = User.builder().username("someone").build();

        Project project = Project.builder()
                .id(id)
                .members(new ArrayList<>())
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(project));

        when(userRepository.findByUsername("someone"))
                .thenReturn(Optional.of(someone));

        RemoveMemberRequest req = new RemoveMemberRequest("someone");

        assertThrows(MemberNotFoundException.class,
                () -> projectService.removeMember(id, owner, req));
    }


    @Test
    void whenRemoveMember_andUserIsThemself_thenThrowException() {

        UUID id = UUID.randomUUID();

        User owner = User.builder().username("john").build();

        Project project = Project.builder()
                .id(id)
                .members(new ArrayList<>(List.of(owner)))
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(project));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(owner));

        RemoveMemberRequest req = new RemoveMemberRequest("john");

        assertThrows(MemberNotFoundException.class,
                () -> projectService.removeMember(id, owner, req));
    }

    @Test
    void whenRemoveMember_isSuccessful_thenRemoveAndUpdate() {

        UUID id = UUID.randomUUID();

        User owner = User.builder().username("owner").build();
        User member = User.builder().username("bob").build();

        List<User> members = new ArrayList<>(List.of(member));

        Project project = Project.builder()
                .id(id)
                .members(members)
                .user(owner)
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(project));

        when(userRepository.findByUsername("bob"))
                .thenReturn(Optional.of(member));

        RemoveMemberRequest req = new RemoveMemberRequest("bob");

        projectService.removeMember(id, owner, req);

        assertFalse(project.getMembers().contains(member));
        verify(projectRepository).save(project);
    }

    @Test
    void processUpcomingDeadline_shouldDoNothing_whenOutside23To24Hours() {

        User user = User.builder().id(UUID.randomUUID()).build();

        Project project = Project.builder()
                .user(user)
                .notifiedUpcoming(false)
                .dueDate(LocalDateTime.now().plusHours(10))
                .build();

        projectService.upcomingDeadlineChecker();

        verify(eventPublisher, never()).publishEvent(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void processUpcomingDeadline_shouldSendEvent_whenBetween23And24Hours() {

        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Project project = Project.builder()
                .user(user)
                .notifiedUpcoming(false)
                .dueDate(LocalDateTime.now().plusHours(23).plusMinutes(1))
                .build();

        when(projectRepository.findByStatusNotAndDeletedFalse(ProjectStatus.COMPLETED))
                .thenReturn(List.of(project));

        projectService.upcomingDeadlineChecker();

        verify(eventPublisher).publishEvent(any(ProjectUpcomingDeadlineEvent.class));
        verify(projectRepository).save(project);
        assertTrue(project.isNotifiedUpcoming());
    }

    @Test
    void checkForOverdueProjects_whenNoProjects_thenDoNothing() {

        when(projectRepository.findByDueDateBeforeAndStatusNotAndDeletedFalse(any(), eq(ProjectStatus.COMPLETED)))
                .thenReturn(Collections.emptyList());

        projectService.checkForOverdueProjects();

        verify(projectRepository).findByDueDateBeforeAndStatusNotAndDeletedFalse(any(), eq(ProjectStatus.COMPLETED));
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void checkForOverdueProjects_shouldCallProcessProject_forEachProject() {

        Project p1 = Project.builder().dueDate(LocalDateTime.now().plusDays(2)).build();
        Project p2 = Project.builder().dueDate(LocalDateTime.now().plusDays(3)).build();

        when(projectRepository.findByDueDateBeforeAndStatusNotAndDeletedFalse(any(), eq(ProjectStatus.COMPLETED)))
                .thenReturn(List.of(p1, p2));

        ProjectService spyService = Mockito.spy(projectService);

        spyService.checkForOverdueProjects();

        verify(spyService).processProject(p1);
        verify(spyService).processProject(p2);
    }

    @Test
    void whenCompleteProject_andStatusIsNotCompleted_thenDoNothing() {
        UUID projectId = UUID.randomUUID();

        Project project = Project.builder()
                .id(projectId)
                .status(ProjectStatus.IN_PROGRESS)
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(projectRepository.findByIdAndDeletedFalse(projectId)).thenReturn(Optional.of(project));

        projectService.completeProject(projectId);

        assertEquals(ProjectStatus.COMPLETED, project.getStatus());
    }

    @Test
    void whenCompleteProject_andStatusIsCompleted_thenDoNothing() {
        UUID projectId = UUID.randomUUID();

        Project project = Project.builder()
                .id(projectId)
                .status(ProjectStatus.COMPLETED)
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(projectRepository.findByIdAndDeletedFalse(projectId)).thenReturn(Optional.of(project));

        projectService.completeProject(projectId);

        assertEquals(ProjectStatus.COMPLETED, project.getStatus());
    }

    @Test
    void getProjectsIncludedIn_shouldReturnOwnedProjects() {
        User user = User.builder().id(UUID.randomUUID()).build();

        Project owned = Project.builder()
                .user(user)
                .members(new ArrayList<>())
                .build();

        Project notIncluded = Project.builder()
                .user(User.builder().id(UUID.randomUUID()).build())
                .members(new ArrayList<>())
                .build();

        when(projectRepository.findAllByDeletedFalse())
                .thenReturn(List.of(owned, notIncluded));

        List<Project> result = projectService.getProjectsIncludedIn(user);

        assertEquals(1, result.size());
        assertTrue(result.contains(owned));
    }

    @Test
    void getProjectsIncludedIn_shouldReturnMemberProjects() {
        User user = User.builder().id(UUID.randomUUID()).build();

        Project memberProject = Project.builder()
                .user(User.builder().id(UUID.randomUUID()).build())
                .members(new ArrayList<>(List.of(user)))
                .build();

        Project notIncluded = Project.builder()
                .user(User.builder().id(UUID.randomUUID()).build())
                .members(new ArrayList<>())
                .build();

        when(projectRepository.findAllByDeletedFalse())
                .thenReturn(List.of(memberProject, notIncluded));

        List<Project> result = projectService.getProjectsIncludedIn(user);

        assertEquals(1, result.size());
        assertTrue(result.contains(memberProject));
    }

    @Test
    void getMembersToString_thenReturnCommaSeparatedUsernames() {
        UUID id = UUID.randomUUID();

        User u1 = User.builder().username("ivan").build();
        User u2 = User.builder().username("georgi").build();

        Project project = Project.builder()
                .members(List.of(u1, u2))
                .build();

        when(projectRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(project));

        String result = projectService.getMembersToString(id);

        assertEquals("ivan, georgi", result);
    }

    @Test
    void getProjectTagsAsString_shouldReturnCommaSeparatedTagTitles() {
        Tag t1 = Tag.builder().title("Test").build();
        Tag t2 = Tag.builder().title("Design").build();

        Project project = Project.builder()
                .tags(List.of(t1, t2))
                .build();

        String result = projectService.getProjectTagsAsString(project);

        assertEquals("Test, Design", result);
    }

    @Test
    void getAllByUserIdAndDeletedFalse_shouldReturnRepositoryResult() {
        UUID userId = UUID.randomUUID();

        List<Project> projects = List.of(
                Project.builder().title("Test1").build(),
                Project.builder().title("Test2").build()
        );

        when(projectRepository.findAllByUserIdAndDeletedFalse(userId))
                .thenReturn(projects);

        List<Project> result = projectService.getAllByUserIdAndDeletedFalse(userId);

        assertEquals(projects, result);
        verify(projectRepository).findAllByUserIdAndDeletedFalse(userId);
    }

    @Test
    void getAllByUserIdAndDeletedFalseAndStatus_shouldReturnRepositoryResult() {
        UUID userId = UUID.randomUUID();
        ProjectStatus status = ProjectStatus.IN_PROGRESS;

        List<Project> projects = List.of(
                Project.builder().title("A").status(status).build(),
                Project.builder().title("B").status(status).build()
        );

        when(projectRepository.findAllByUserIdAndDeletedFalseAndStatus(userId, status))
                .thenReturn(projects);

        List<Project> result =
                projectService.getAllByUserIdAndDeletedFalseAndStatus(userId, status);

        assertEquals(projects, result);
        verify(projectRepository).findAllByUserIdAndDeletedFalseAndStatus(userId, status);
    }

    @Test
    void calculateCompletionPercent_whenNoTasks_shouldSetZeroAndReturn() {
        Project project = Project.builder().tasks(List.of()).build();

        UUID id = UUID.randomUUID();
        project.setId(id);

        when(taskService.getTasksByProjectIdNotDeleted(id)).thenReturn(List.of());

        projectService.calculateCompletionPercent(project);

        assertEquals(0, project.getCompletionPercent());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void calculateCompletionPercent_shouldCalculateCorrectly() {
        UUID id = UUID.randomUUID();

        Task t1 = Task.builder().status(TaskStatus.COMPLETED).build();
        Task t2 = Task.builder().status(TaskStatus.IN_PROGRESS).build();
        Task t3 = Task.builder().status(TaskStatus.COMPLETED).build();

        Project project = Project.builder()
                .id(id)
                .tasks(List.of(t1, t2, t3))
                .user(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(taskService.getTasksByProjectIdNotDeleted(id))
                .thenReturn(List.of(t1, t2, t3));

        projectService.calculateCompletionPercent(project);

        assertEquals(67, project.getCompletionPercent());
        verify(projectRepository).save(project);
    }

}
