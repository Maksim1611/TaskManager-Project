package com.example.TaskManager.web;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.security.ProjectSecurity;
import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.task.model.TaskPriority;
import com.example.TaskManager.task.service.TaskService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.utils.ProjectUtils;
import com.example.TaskManager.utils.TaskUtils;
import com.example.TaskManager.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(GlobalUserController.class)
@WebMvcTest(TaskController.class)
public class TaskControllerApiTest {

    @MockitoBean
    private TaskService taskService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ProjectService projectService;
    @MockitoBean
    private ProjectSecurity projectSecurity;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCreateTaskPage_shouldReturnCreateTaskPageAndStatus200Ok() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = createAuthentication(user, UserRole.USER);
        MockHttpServletRequestBuilder request = get("/tasks/new-task")
                .with(csrf())
                .with(authentication(authentication));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-task"))
                .andExpect(model().attributeExists("user", "createTaskRequest"));
    }

    @Test
    void getCreateTaskPageForProject_shouldReturnCreateTaskPageAndStatus200Ok() throws Exception {
        UUID projectId = UUID.randomUUID();

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        when(projectSecurity.isOwner(eq(projectId), any())).thenReturn(true);

        Authentication authentication = createAuthentication(user, UserRole.USER);
        MockHttpServletRequestBuilder request = get("/tasks/new-task/" + projectId)
                .with(csrf())
                .with(authentication(authentication));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-task-project"))
                .andExpect(model().attributeExists("createTaskRequest"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("projectId", projectId));
    }

    @Test
    void postCreateTaskWithProject_withNoErrors() throws  Exception {
        UUID projectId = UUID.randomUUID();
        User user = UserUtils.randomUser();

        Project project = Project.builder()
                .id(projectId)
                .title("Project")
                .build();

        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Task Title")
                .project(project)
                .build();

        when(userService.getById(user.getId())).thenReturn(user);
        when(projectService.getByIdNotDeleted(projectId)).thenReturn(project);
        when(taskService.createTask(any(), eq(user.getId()), eq(project))).thenReturn(task);

        Authentication authentication = createAuthentication(user, UserRole.USER);
        MockHttpServletRequestBuilder request = post("/tasks/new-task/{projectId}", projectId)
                .formField("title", "title")
                .formField("description", "desc")
                .formField("priority", TaskPriority.LOW.name())
                .formField("dueDate", LocalDateTime.now().plusDays(1).toString())
                .with(csrf())
                .with(authentication(authentication));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/" + projectId));

    }

    @Test
    void postCreateTaskWithProject_withErrors() throws  Exception {
        UUID projectId = UUID.randomUUID();
        User user = UserUtils.randomUser();

        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = createAuthentication(user, UserRole.USER);
        MockHttpServletRequestBuilder request = post("/tasks/new-task/{projectId}", projectId)
                .formField("title", "ti")
                .formField("description", "d")
                .formField("priority", TaskPriority.LOW.name())
                .formField("dueDate", LocalDateTime.now().plusDays(1).toString())
                .with(csrf())
                .with(authentication(authentication));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-task-project"))
                .andExpect(model().attributeExists("user" ,"createTaskRequest"));
    }

    @Test
    void postCreateTaskWithoutProject_withNoErrors() throws  Exception {
        UUID projectId = UUID.randomUUID();
        User user = UserUtils.randomUser();

        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Task Title")
                .project(null)
                .build();

        when(userService.getById(user.getId())).thenReturn(user);
        when(taskService.createTask(any(), eq(user.getId()), eq(null))).thenReturn(task);

        Authentication authentication = createAuthentication(user, UserRole.USER);
        MockHttpServletRequestBuilder request = post("/tasks/new-task")
                .formField("title", "title")
                .formField("description", "desc")
                .formField("priority", TaskPriority.LOW.name())
                .formField("dueDate", LocalDateTime.now().plusDays(1).toString())
                .with(csrf())
                .with(authentication(authentication));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

    }

    @Test
    void postCreateTaskWithoutProject_withErrors() throws  Exception {
        User user = UserUtils.randomUser();

        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Task Title")
                .project(null)
                .build();

        when(userService.getById(user.getId())).thenReturn(user);
        when(taskService.createTask(any(), eq(user.getId()), eq(null))).thenReturn(task);

        Authentication authentication = createAuthentication(user, UserRole.USER);
        MockHttpServletRequestBuilder request = post("/tasks/new-task")
                .formField("title", "t")
                .formField("description", "d")
                .formField("priority", TaskPriority.LOW.name())
                .formField("dueDate", LocalDateTime.now().plusDays(1).toString())
                .with(csrf())
                .with(authentication(authentication));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-task"))
                .andExpect(model().attributeExists("user" ,"createTaskRequest"));
    }

    @Test
    void whenGetEditTaskPage_shouldReturn200OkAndEditPage() throws Exception {
        User user = UserUtils.randomUser();

        Task task = TaskUtils.generateTask(user);
        task.setId(UUID.randomUUID());
        when(taskService.getByIdNotDeleted(task.getId())).thenReturn(task);

        Authentication authentication = createAuthentication(user, UserRole.USER);
        when(userService.getById(user.getId())).thenReturn(user);

        MockHttpServletRequestBuilder request = get("/tasks/{id}/task", task.getId())
                .with(authentication(authentication))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "editTaskRequest", "taskId"))
                .andExpect(view().name("edit-task"));
    }

    @Test
    void whenPutEditTask_withoutErrorsWithoutProject_shouldEditTaskAndRedirect() throws Exception {
        User user = UserUtils.randomUser();
        Task task = TaskUtils.generateTask(user);
        task.setId(UUID.randomUUID());

        when(taskService.getByIdNotDeleted(task.getId())).thenReturn(task);
        when(userService.getById(user.getId())).thenReturn(user);
        when(taskService.checkIfTaskHasProjectRedirect(task.getId())).thenReturn("redirect:/tasks");

        Authentication authentication = createAuthentication(user, UserRole.USER);

        MockHttpServletRequestBuilder request = put("/tasks/{id}/task", task.getId())
                .with(authentication(authentication))
                .with(csrf())
                .param("title", "title")
                .param("description", "description")
                .param("priority", TaskPriority.LOW.name())
                .param("dueDate", LocalDateTime.now().plusDays(1).toString());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
    }

    @Test
    void whenPutEditTask_withErrorsNoMatterWithOrWithoutProject_shouldEditTaskAndRedirect() throws Exception {
        User user = UserUtils.randomUser();
        Task task = TaskUtils.generateTask(user);
        task.setId(UUID.randomUUID());

        when(taskService.getByIdNotDeleted(task.getId())).thenReturn(task);
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = createAuthentication(user, UserRole.USER);

        MockHttpServletRequestBuilder request = put("/tasks/{id}/task", task.getId())
                .with(authentication(authentication))
                .with(csrf())
                .param("title", "t")
                .param("description", "de")
                .param("priority", TaskPriority.LOW.name())
                .param("dueDate", LocalDateTime.now().plusDays(1).toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-task"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void whenPutEditTask_withoutErrorsWithProject_shouldEditTaskAndRedirect() throws Exception {
        User user = UserUtils.randomUser();
        Task task = TaskUtils.generateTask(user);
        task.setId(UUID.randomUUID());

        Project project = ProjectUtils.generateProject(user);

        when(taskService.getByIdNotDeleted(task.getId())).thenReturn(task);
        when(userService.getById(user.getId())).thenReturn(user);
        when(projectService.getByIdNotDeleted(project.getId())).thenReturn(project);

        when(taskService.checkIfTaskHasProjectRedirect(task.getId())).thenReturn("redirect:/projects/%s".formatted(project.getId()));

        Authentication authentication = createAuthentication(user, UserRole.USER);

        MockHttpServletRequestBuilder request = put("/tasks/{id}/task", task.getId())
                .with(authentication(authentication))
                .with(csrf())
                .param("title", "title")
                .param("description", "description")
                .param("priority", TaskPriority.LOW.name())
                .param("dueDate", LocalDateTime.now().plusDays(1).toString());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/%s".formatted(project.getId())));
    }


    public Authentication createAuthentication(User user, UserRole role) {
        return new UsernamePasswordAuthenticationToken(
                new UserData(user.getId(), user.getEmail(), user.getPassword(), true, role, null),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

}
