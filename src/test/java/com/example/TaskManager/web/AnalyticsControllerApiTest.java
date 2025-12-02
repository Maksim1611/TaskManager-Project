package com.example.TaskManager.web;

import com.example.TaskManager.analytics.client.AnalyticsClient;
import com.example.TaskManager.analytics.client.dto.ProjectAnalyticsResponse;
import com.example.TaskManager.analytics.client.dto.TaskAnalyticsResponse;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(GlobalUserController.class)
@WebMvcTest(AnalyticsController.class)
public class AnalyticsControllerApiTest {

    @MockitoBean
    private AnalyticsClient analyticsClient;

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenGetAnalytics_shouldReturnAnalyticsAndStatus200Ok() throws Exception {
        User user = UserUtils.randomUser();
        Authentication authentication = UserUtils.generateAuthentication(user, UserRole.USER);

        TaskAnalyticsResponse taskAnalyticsResponse = TaskAnalyticsResponse.builder()
                .totalTasks(1)
                .build();

        ProjectAnalyticsResponse projectAnalyticsResponse = ProjectAnalyticsResponse.builder()
                .totalProjects(1)
                .build();

        when(userService.getById(user.getId())).thenReturn(user);
        when(analyticsClient.getTaskAnalytics(user.getId())).thenReturn(taskAnalyticsResponse);
        when(analyticsClient.getProjectAnalytics(user.getId())).thenReturn(projectAnalyticsResponse);

        MockHttpServletRequestBuilder httpRequest = get("/analytics")
                .with(authentication(authentication))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("analytics"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void whenGetLifetimeAnalytics_shouldReturnAnalyticsAndStatus200Ok() throws Exception {
        User user = UserUtils.randomUser();
        Authentication authentication = UserUtils.generateAuthentication(user, UserRole.USER);

        TaskAnalyticsResponse taskAnalyticsResponse = TaskAnalyticsResponse.builder()
                .totalTasks(1)
                .build();

        ProjectAnalyticsResponse projectAnalyticsResponse = ProjectAnalyticsResponse.builder()
                .totalProjects(1)
                .build();

        when(userService.getById(user.getId())).thenReturn(user);
        when(analyticsClient.getTaskAnalytics(user.getId())).thenReturn(taskAnalyticsResponse);
        when(analyticsClient.getProjectAnalytics(user.getId())).thenReturn(projectAnalyticsResponse);

        MockHttpServletRequestBuilder httpRequest = get("/analytics/lifetime")
                .with(authentication(authentication))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("analytics-lifetime"))
                .andExpect(model().attributeExists("user"));
    }

}
