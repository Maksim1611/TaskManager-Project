package com.example.TaskManager.web;

import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.task.service.TaskService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
public class DashboardControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TaskService taskService;
    @MockitoBean
    private ProjectService projectService;
    @MockitoBean
    private ActivityService activityService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDashboardPage_shouldReturnHomeViewWithUserModelAttributeAndStatusCodeIs200() throws Exception {
        User user = randomUser();
        when(userService.getById(any())).thenReturn(user);

        UserData authentication = new UserData(user.getId(), user.getEmail(), user.getPassword(), user.isActive() ,user.getRole(), null);
        MockHttpServletRequestBuilder httpRequest = get("/dashboard")
                .with(user(authentication));

        mockMvc.perform(httpRequest)
                .andExpect(view().name("dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getHomePageSomethingWentWrongInTheServiceLayer_shouldReturnInternalServerErrorView() throws Exception {
        User user = randomUser();
        when(userService.getById(any())).thenThrow(RuntimeException.class);

        UserData authentication = new UserData(user.getId(), user.getEmail(), user.getPassword(), user.isActive() ,user.getRole(), null);
        MockHttpServletRequestBuilder httpRequest = get("/dashboard")
                .with(user(authentication));

        mockMvc.perform(httpRequest)
                .andExpect(view().name("internal-server-error"))
                .andExpect(status().isInternalServerError());
    }

    public User randomUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("Maxim12")
                .firstName("Maxim")
                .lastName("Stefanov")
                .password("123123")
                .email("maxim@gmail.com")
                .createdOn(LocalDateTime.now())
                .modifiedOn(LocalDateTime.now())
                .active(true)
                .role(UserRole.USER)
                .tasks(new ArrayList<>())
                .projects(new ArrayList<>())
                .activities(new ArrayList<>())
                .emailNotificationEnabled(true)
                .deadLineNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .reminderNotificationEnabled(true)
                .build();
    }
}
