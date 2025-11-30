package com.example.TaskManager.web;

import com.example.TaskManager.exception.user.UserNotFoundException;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.security.ProjectSecurity;
import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.utils.ProjectUtils;
import com.example.TaskManager.utils.TaskUtils;
import com.example.TaskManager.utils.UserUtils;
import com.example.TaskManager.web.dto.CreateProjectRequest;
import com.example.TaskManager.web.dto.EditProjectRequest;
import com.example.TaskManager.web.dto.InviteMemberRequest;
import com.example.TaskManager.web.dto.RemoveMemberRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(GlobalUserController.class)
@WebMvcTest(ProjectController.class)
public class ProjectControllerApiTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProjectSecurity projectSecurity;

    private Authentication auth(User user) {
        return new UsernamePasswordAuthenticationToken(
                new UserData(user.getId(), user.getEmail(), user.getPassword(), true, UserRole.USER, null),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void getProjects_withoutStatus_shouldReturnProjectsIncludedIn() throws Exception {
        User user = UserUtils.randomUser();
        List<Project> projects = List.of(ProjectUtils.generateProject(user), ProjectUtils.generateProject(user));

        when(userService.getById(user.getId())).thenReturn(user);
        when(projectService.getProjectsIncludedIn(user)).thenReturn(projects);

        when(projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.ACTIVE))
                .thenReturn(List.of());
        when(projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.IN_PROGRESS))
                .thenReturn(List.of());
        when(projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.COMPLETED))
                .thenReturn(List.of());

        mockMvc.perform(get("/projects")
                        .with(authentication(auth(user)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("projects"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("projects", projects));
    }

    @Test
    void getProjects_withStatus_shouldReturnFilteredProjects() throws Exception {
        User user = UserUtils.randomUser();
        List<Project> active = List.of(ProjectUtils.generateProject(user));

        when(userService.getById(user.getId())).thenReturn(user);
        when(projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.ACTIVE))
                .thenReturn(active);

        when(projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.IN_PROGRESS))
                .thenReturn(List.of());
        when(projectService.getAllByUserIdAndDeletedFalseAndStatus(user.getId(), ProjectStatus.COMPLETED))
                .thenReturn(List.of());

        mockMvc.perform(get("/projects")
                        .param("status", "ACTIVE")
                        .with(authentication(auth(user)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("projects"))
                .andExpect(model().attribute("projects", active))
                .andExpect(model().attribute("status", "ACTIVE"));
    }

    @Test
    void getProjectPage_shouldReturnProjectPageAndStatus200() throws Exception {
        UUID projectId = UUID.randomUUID();
        User user = UserUtils.randomUser();

        Project project = ProjectUtils.generateProject(user);
        project.setTasks(List.of(TaskUtils.generateTask(user), TaskUtils.generateTask(user)));


        List<Task> sortedTasks = project.getTasks().stream()
                .sorted(Comparator.comparing(Task::getCreatedOn))
                .toList();

        when(userService.getById(user.getId())).thenReturn(user);
        when(projectService.getByIdNotDeleted(projectId)).thenReturn(project);
        when(projectService.getMembersToString(projectId)).thenReturn("Maxim, Ivan");

        Authentication authentication = auth(user);

        mockMvc.perform(get("/projects/" + projectId)
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("project"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("project", project))
                .andExpect(model().attribute("members", "Maxim, Ivan"))
                .andExpect(model().attribute("tasks", sortedTasks))
                .andExpect(model().attribute("isOwner", true));
    }

    @Test
    void addProjectPage_shouldReturnAddProjectViewAndModel() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = auth(user);

        mockMvc.perform(get("/projects/new-project")
                        .with(authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("add-project"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attributeExists("createProjectRequest"));
    }

    @Test
    void postCreateProject_withValidData_shouldRedirect() throws Exception {
        User user = UserUtils.randomUser();

        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(post("/projects/new-project")
                        .with(authentication(auth))
                        .with(csrf())
                        .formField("title", "My Project")
                        .formField("description", "Some description")
                        .formField("dueDate", LocalDateTime.now().plusDays(2).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService).createProject(any(CreateProjectRequest.class), eq(user));
    }

    @Test
    void postCreateProject_withErrors_shouldReturnAddProjectView() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(post("/projects/new-project")
                        .with(authentication(auth))
                        .with(csrf())
                        .formField("title", "")
                        .formField("description", "d")
                        .formField("dueDate", LocalDateTime.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("add-project"))
                .andExpect(model().attributeExists("createProjectRequest", "user"));
    }

    @Test
    void getEditProjectPage_shouldReturnCorrectModelAndView() throws Exception {
        UUID projectId = UUID.randomUUID();

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Project project = ProjectUtils.generateProject(user);

        when(projectService.getByIdNotDeleted(projectId)).thenReturn(project);
        when(projectService.getProjectTagsAsString(project)).thenReturn("tag1,tag2");

        when(projectSecurity.isOwner(eq(projectId), any())).thenReturn(true);

        Authentication auth = auth(user);


        mockMvc.perform(get("/projects/{id}/project", projectId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-project"))
                .andExpect(model().attributeExists("user", "editProjectRequest"));

        verify(projectService).getByIdNotDeleted(projectId);
        verify(projectService).getProjectTagsAsString(project);
    }

    @Test
    void putEditProject_withErrors_shouldReturnEditProjectView() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Project project = ProjectUtils.generateProject(user);
        when(projectService.getByIdNotDeleted(project.getId())).thenReturn(project);

        Authentication auth = auth(user);

        mockMvc.perform(put("/projects/{id}/project", project.getId())
                        .with(authentication(auth))
                        .with(csrf())
                        .param("title", "")
                        .param("description", "")
                        .param("tags", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-project"))
                .andExpect(model().attributeExists("user"));

    }

    @Test
    void putEditProject_validRequest_shouldRedirectAndCallService() throws Exception {
        UUID projectId = UUID.randomUUID();

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(put("/projects/{id}/project", projectId)
                        .with(authentication(auth))
                        .with(csrf())
                        .param("title", "Updated Title")
                        .param("description", "Updated Desc")
                        .param("tags", "test,spring")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService).editProject(any(EditProjectRequest.class), eq(projectId));
    }

    @Test
    void patchCompleteProject_shouldCallServiceAndRedirect() throws Exception {
        UUID projectId = UUID.randomUUID();
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(patch("/projects/{id}/project", projectId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService).completeProject(projectId);
    }

    @Test
    void postRemoveMember_withErrors_shouldReturnRemoveMemberView() throws Exception {
        UUID projectId = UUID.randomUUID();
        User user = UserUtils.randomUser();

        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(post("/projects/{id}/member", projectId)
                .with(authentication(auth))
                .with(csrf())
                .param("username", "a"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "removeMemberRequest"))
                .andExpect(view().name("remove-member"));
    }

    @Test
    void postRemoveMember_noErrors_shouldCallServiceAndRedirect() throws Exception {
        UUID projectId = UUID.randomUUID();
        User user = UserUtils.randomUser();

        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(post("/projects/{id}/member", projectId)
                        .with(authentication(auth))
                        .with(csrf())
                        .param("username", "Maxim12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/%s".formatted(projectId)));

        verify(projectService).removeMember(eq(projectId), eq(user), any(RemoveMemberRequest.class));
    }

    @Test
    void getRemoveMemberPage_shouldReturnRemoveMemberView() throws Exception {
        UUID projectId = UUID.randomUUID();

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(get("/projects/{id}/member", projectId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("remove-member"))
                .andExpect(model().attributeExists("removeMemberRequest", "user"));

    }

    @Test
    void postAddMember_withErrors_shouldReturnInviteMemberView() throws Exception {
        UUID projectId = UUID.randomUUID();

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(post("/projects/{id}/invitation", projectId)
                        .with(authentication(auth))
                        .with(csrf())
                        .param("username", "a"))
                .andExpect(status().isOk())
                .andExpect(view().name("invite-member"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void postAddMember_withValidData_shouldRedirect() throws Exception {
        UUID projectId = UUID.randomUUID();

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(post("/projects/{id}/invitation", projectId)
                        .with(authentication(auth))
                        .with(csrf())
                        .param("username", "Maxim12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/" + projectId));

        verify(projectService).inviteMember(any(InviteMemberRequest.class), eq(projectId), eq(user));
    }

    @Test
    void getMemberPage_shouldReturnInviteMemberView() throws Exception {
        UUID projectId = UUID.randomUUID();

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = auth(user);

        mockMvc.perform(get("/projects/{id}/invitation", projectId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("invite-member"))
                .andExpect(model().attributeExists("user", "inviteMemberRequest"));

    }


}
