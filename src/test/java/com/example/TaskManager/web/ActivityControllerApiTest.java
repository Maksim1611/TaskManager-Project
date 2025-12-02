package com.example.TaskManager.web;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.service.ActivityService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(GlobalUserController.class)
@WebMvcTest(ActivityController.class)
public class ActivityControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ActivityService activityService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenGetActivityPage_shouldReturn200Ok() throws Exception {
        User user = UserUtils.randomUser();
        Authentication authentication = UserUtils.generateAuthentication(user, UserRole.USER);

        List<Activity> activities = List.of(
                Activity.builder().type(ActivityType.TASK_CREATED).user(user).build(),
                Activity.builder().type(ActivityType.PROJECT_CREATED).user(user).build(),
                Activity.builder().type(ActivityType.PROJECT_TASK_CREATED).user(user).build()
        );

        when(userService.getById(user.getId())).thenReturn(user);
        when(activityService.getByUserId(user.getId())).thenReturn(activities);

        MockHttpServletRequestBuilder httpRequest = get("/activity")
                .with(authentication(authentication))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "activities", "taskCount", "projectCount"));
    }

    @Test
    void whenDeleteClearActivity_shouldReturn301Redirected() throws Exception {
        User user = UserUtils.randomUser();
        Authentication authentication = UserUtils.generateAuthentication(user, UserRole.USER);

        when(userService.getById(user.getId())).thenReturn(user);

        MockHttpServletRequestBuilder httpRequest = delete("/activity/{id}", user.getId())
                .with(authentication(authentication))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activity"));
    }

}
