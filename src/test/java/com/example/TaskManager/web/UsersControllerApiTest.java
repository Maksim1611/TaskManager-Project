package com.example.TaskManager.web;

import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.utils.UserUtils;
import com.example.TaskManager.web.dto.ChangePasswordRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(GlobalUserController.class)
@WebMvcTest(UsersController.class)
public class UsersControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUsersPage_shouldReturnUsersView() throws Exception {

        User user = UserUtils.randomUser();
        user.setRole(UserRole.ADMIN);

        Authentication auth = generateAuthentication(user);

        when(userService.getById(any())).thenReturn(user);
        when(userService.getSortedUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void getProfilePage_shouldReturnSettingsView() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(any())).thenReturn(user);

        UserDetails userDetails = normalUserAuthentication();
        MockHttpServletRequestBuilder httpRequest = get("/users/" + user.getId() + "/profile")
                .with(user(userDetails))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeExists("user", "editProfileRequest", "changePasswordRequest"));
    }

    @Test
    void deleteUser_shouldRedirectLogout() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(any())).thenReturn(user);

        UserDetails userDetails = normalUserAuthentication();
        MockHttpServletRequestBuilder httpRequest = delete("/users/" + user.getId() + "/user")
                .with(user(userDetails))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout"));
    }

    @Test
    void patchChangeRole_shouldChangeRoleAndRedirectToUsers() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(any())).thenReturn(user);

        UserDetails userDetails = adminAuthentication();
        MockHttpServletRequestBuilder httpRequest = patch("/users/" + UUID.randomUUID() + "/role")
                .with(user(userDetails))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService).changeRole(any());
    }

    @Test
    void blockUserAccount_fromAdminUser_shouldRedirectToUsers() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(any())).thenReturn(user);

        UserDetails userDetails = adminAuthentication();

        MockHttpServletRequestBuilder httpRequest = patch("/users/" + UUID.randomUUID() + "/status")
                .with(user(userDetails))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService).blockAccount(any());
    }

    @Test
    void putChangePassword_shouldChangePasswordAndRedirectToDashboard() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(any())).thenReturn(user);

        UserDetails userDetails = normalUserAuthentication();
        MockHttpServletRequestBuilder httpRequest = put("/users/" + user.getId() + "/password")
                .with(user(userDetails))
                .with(csrf())
                .param("currentPassword", user.getPassword())
                .param("newPassword", "654321")
                .param("confirmPassword", "654321");

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        ArgumentCaptor<ChangePasswordRequest> captor = ArgumentCaptor.forClass(ChangePasswordRequest.class);

        verify(userService).changePassword(captor.capture(), eq(user));

        ChangePasswordRequest changePasswordRequest = captor.getValue();

        assertEquals(changePasswordRequest.getCurrentPassword(), user.getPassword());
        assertEquals("654321", changePasswordRequest.getConfirmPassword());
        assertEquals("654321", changePasswordRequest.getNewPassword());

    }

    @Test
    void putChangePassword_shouldReturnSettingsPage() throws Exception {
        User user = UserUtils.randomUser();
        when(userService.getById(any())).thenReturn(user);

        UserDetails userDetails = normalUserAuthentication();

        MockHttpServletRequestBuilder httpRequest = put("/users/" + user.getId() + "/password")
                .with(user(userDetails))
                .with(csrf())
                .param("currentPassword", user.getPassword())
                .param("newPassword", "654")
                .param("confirmPassword", "6543");

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("settings"));

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_withErrors_shouldReturnSettingsView() throws Exception {

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = generateAuthentication(user);

        MockHttpServletRequestBuilder request = put("/users/" + user.getId() + "/password")
                .with(csrf())
                .param("currentPassword", "123123")
                .param("newPassword", "5566")
                .param("confirmPassword", "6655")
                .with(authentication(authentication));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("changePasswordRequest"))
                .andExpect(model().attributeExists("editProfileRequest"));

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    void editProfile_withErrors_shouldReturnSettingsView() throws Exception {

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        Authentication authentication = generateAuthentication(user);
        mockMvc.perform(multipart("/users/" + user.getId() + "/profile")
                        .file(image)
                        .param("removeImage", "false")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", user.getEmail())
                        .with(csrf())
                        .with(authentication(authentication))
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeExists("user", "changePasswordRequest"));

        verify(userService, never()).update(any());
        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    void editProfile_shouldReturnSettingsView() throws Exception {

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        Authentication authentication = generateAuthentication(user);
        mockMvc.perform(multipart("/users/" + user.getId() + "/profile")
                        .file(image)
                        .param("removeImage", "false")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", user.getEmail())
                        .with(csrf())
                        .with(authentication(authentication))
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeExists("user", "changePasswordRequest"));

        verify(userService, never()).update(any());
        verify(userService, never()).updateProfile(any(), any());
    }


    @Test
    void editProfile_withRemovedImage_shouldEditProfileAndRedirectToDashboard() throws Exception {

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        Authentication authentication = generateAuthentication(user);
        mockMvc.perform(multipart("/users/" + user.getId() + "/profile")
                        .file(image)
                        .param("removeImage", "true")
                        .param("firstName", "Maxim")
                        .param("lastName", "Stefanov")
                        .param("email", user.getEmail())
                        .with(csrf())
                        .with(authentication(authentication))
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userService).update(user);
        verify(userService).updateProfile(eq(user.getId()) ,any());
        assertNull(user.getImage());
    }

    @Test
    void editProfile_withNotRemovedImage_shouldEditProfileAndRedirectToDashboard() throws Exception {

        User user = UserUtils.randomUser();
        when(userService.getById(user.getId())).thenReturn(user);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        Authentication authentication = generateAuthentication(user);
        mockMvc.perform(multipart("/users/" + user.getId() + "/profile")
                        .file(image)
                        .param("removeImage", "false")
                        .param("firstName", "Maxim")
                        .param("lastName", "Stefanov")
                        .param("email", user.getEmail())
                        .with(csrf())
                        .with(authentication(authentication))
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userService).update(user);
        verify(userService).updateProfile(eq(user.getId()) ,any());
        assertNotNull(user.getImage());
    }

    @Test
    void getProfilePage_shouldReturnIsOk200AndSettingsView() throws Exception {
        User user = UserUtils.randomUser();
        user.setImage(new byte[]{1, 2, 3});
        when(userService.getById(user.getId())).thenReturn(user);

        Authentication authentication = generateAuthentication(user);
        MockHttpServletRequestBuilder httpRequest = get("/users/" + user.getId() + "/profile")
                .with(authentication(authentication))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attributeExists("user", "base64Image", "editProfileRequest", "changePasswordRequest"));

        assertNotNull(user.getImage());
    }

    private Authentication generateAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(
                new UserData(user.getId(), user.getEmail(), user.getPassword(), true, user.getRole(), null),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    public UserDetails normalUserAuthentication() {
        return new UserData(UUID.randomUUID(), "Maxim12", "123123", true, UserRole.USER);
    }

    public static UserDetails adminAuthentication() {

        return new UserData(UUID.randomUUID(), "Maxim12", "123123", true, UserRole.ADMIN);
    }

}
