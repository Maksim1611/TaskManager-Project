package com.example.TaskManager.web;

import com.example.TaskManager.config.MethodSecurityTestConfig;
import com.example.TaskManager.config.WebConfiguration;
import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({GlobalUserController.class, MethodSecurityTestConfig.class})
@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getNotificationPage_shouldReturnStatus200OkAndNotificationsView() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserData(user.getId(), user.getEmail(), user.getPassword(), true, user.getRole(), null),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        MockHttpServletRequestBuilder httpRequest = get("/notifications")
                .with(csrf())
                .with(authentication(authentication));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void patchUpdatePreference_WithoutErrors_shouldRedirectToNotificationsPageAndUpdatePreference() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = createAuthentication(user, UserRole.USER);

        MockHttpServletRequestBuilder httpRequest = patch("/notifications/{id}/preferences", user.getId())
                .with(csrf())
                .with(authentication(authentication))
                .param("emailNotificationEnabled", "true")
                .param("deadLineNotificationEnabled", "true")
                .param("summaryNotificationEnabled", "true")
                .param("reminderNotificationEnabled", "true");

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService).updatePreferences(eq(user), any());
        verify(userService).editPreferences(any(), eq(user.getId()));
    }

    @Test
    void patchUpdatePreference_withErrors_shouldRedirectToNotificationsPageAndUpdatePreference() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = createAuthentication(user, UserRole.USER);

        MockHttpServletRequestBuilder httpRequest = patch("/notifications/{id}/preferences", user.getId())
                .with(csrf())
                .with(authentication(authentication))
                .param("emailNotificationEnabled", "georgi")
                .param("deadLineNotificationEnabled", "true")
                .param("summaryNotificationEnabled", "ivan")
                .param("reminderNotificationEnabled", "");

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verifyNoInteractions(notificationService);
        verify(userService, never()).editPreferences(any(), eq(user.getId()));
    }

    @Test
    void deleteClearHistory_shouldRedirectToNotificationsPageAndClearHistory() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = createAuthentication(user, UserRole.USER);

        MockHttpServletRequestBuilder httpRequest = delete("/notifications/history")
                .with(csrf())
                .with(authentication(authentication));

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService).clearHistory(eq(user.getId()));
    }

    @Test
    void getNotificationSenderPage_withAdminUser_shouldReturnNotificationSenderPageAndOk200Status() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = createAuthentication(user, UserRole.ADMIN);

        MockHttpServletRequestBuilder httpRequest = get("/notifications/sender")
                .with(authentication(auth));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("notification-sender"))
                        .andExpect(model().attributeExists("user"));
    }

    @Test
    void getNotificationSenderPage_withModeratorUser_shouldReturnNotificationSenderPageAndOk200Status() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = createAuthentication(user, UserRole.MODERATOR);

        MockHttpServletRequestBuilder httpRequest = get("/notifications/sender")
                .with(authentication(auth));

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("notification-sender"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getNotificationSenderPage_withNormalUser_shouldReturnNotificationSenderPageAndOk200Status() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = createAuthentication(user, UserRole.USER);

        MockHttpServletRequestBuilder httpRequest = get("/notifications/sender")
                .with(authentication(auth));

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MODERATOR"})
    void sendGlobalNotification_withValidData_shouldRedirect() throws Exception {

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);
        when(userService.getAllUsers()).thenReturn(List.of(user));

        Authentication auth = createAuthentication(user, UserRole.ADMIN);

        mockMvc.perform(post("/notifications/sender")
                        .param("subject", "Announcement")
                        .param("body", "something")
                        .param("type", "EMAIL")
                        .with(csrf())
                        .with(authentication(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService)
                .sendGlobalNotification(any(), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MODERATOR"})
    void sendGlobalNotification_withInvalidData_shouldReturnSenderView() throws Exception {

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication auth = createAuthentication(user, UserRole.ADMIN);

        mockMvc.perform(post("/notifications/sender")
                        .param("subject", "")
                        .param("body", "")
                        .with(csrf())
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("notification-sender"))
                .andExpect(model().attributeExists("user", "globalNotificationRequest"));

        verify(notificationService, never())
                .sendGlobalNotification(any(), any());
    }

    public Authentication createAuthentication(User user, UserRole role) {
        return new UsernamePasswordAuthenticationToken(
                new UserData(user.getId(), user.getEmail(), user.getPassword(), true, role, null),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

}
