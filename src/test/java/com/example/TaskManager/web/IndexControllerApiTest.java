package com.example.TaskManager.web;

import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
@AutoConfigureMockMvc(addFilters = false)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIndexPoint_shouldReturnStatus200OkAndIndexView() throws Exception {
        MockHttpServletRequestBuilder httpRequest = get("/");

        mockMvc.perform(httpRequest)
                .andExpect(view().name("index"))
                .andExpect(status().isOk());
    }

    @Test
    void postRegister_shouldReturn302RedirectToLoginPageAndInvokeRegisterServiceMethod() throws Exception {
        MockHttpServletRequestBuilder httpRequest = post("/register")
                .formField("username", "Maxim12")
                .formField("firstName", "Maxim")
                .formField("lastName", "Stefanov")
                .formField("email", "maxim@gmail.com")
                .formField("password", "123123")
                .formField("confirmPassword", "123123")
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(redirectedUrl("/login"))
                .andExpect(status().is3xxRedirection());

        verify(userService).registerUser(any());
    }


    @Test
    void postRegisterWithInvalidFormArguments_shouldReturn200OkAndRedirectToLoginPageAndInvokeRegisterServiceMethod() throws Exception {
        MockHttpServletRequestBuilder httpRequest = post("/register")
                .formField("username", "Ma")
                .formField("firstName", "Ma")
                .formField("lastName", "Ste")
                .formField("email", "maxim@gmail.com")
                .formField("password", "123123")
                .formField("confirmPassword", "123123")
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(view().name("register"))
                .andExpect(status().isOk());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void getRegisterPageShouldReturnRegisterViewAndStatusCode200Ok() throws Exception {
        MockHttpServletRequestBuilder httpRequest = get("/register");

        mockMvc.perform(httpRequest)
                .andExpect(view().name("register"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void getLoginPageShouldReturnLoginViewAndStatusCode200Ok() throws Exception {
        MockHttpServletRequestBuilder httpRequest = get("/login");

        mockMvc.perform(httpRequest)
                .andExpect(view().name("login"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getLoginPageWithBlockedUserShouldReturnLoginViewAndStatusCode200OkWithErrorMessage() throws Exception {
        MockHttpServletRequestBuilder httpRequest = get("/login")
                .param("blocked", "1");

        mockMvc.perform(httpRequest)
                .andExpect(view().name("login"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attribute("errorInactiveMessage",
                        "Your account has been blocked"));
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
