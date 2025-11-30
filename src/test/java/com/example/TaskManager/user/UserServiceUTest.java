package com.example.TaskManager.user;

import com.example.TaskManager.exception.user.EmailAlreadyExistException;
import com.example.TaskManager.exception.user.UserNotFoundException;
import com.example.TaskManager.exception.user.UsernameAlreadyExistException;
import com.example.TaskManager.notification.NotificationMessages;
import com.example.TaskManager.notification.NotificationType;
import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;


    @Test
    void whenRegisterNewUser_andRepositoryReturnsOptionalPresentWithExistingUsername_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Maxim")
                .build();

        User userFromDB = User.builder()
                .id(userId)
                .username("Maxim")
                .build();
        when(userRepository.findByEmailOrUsername(null, userFromDB.getUsername())).thenReturn(Optional.of(userFromDB));

        assertThrows(UsernameAlreadyExistException.class, () -> userService.registerUser(registerRequest));
    }

    @Test
    void whenRegisterNewUser_andRepositoryReturnsOptionalPresentWithExistingEmail_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("maxim@gmail.com")
                .username("Maxim5")
                .build();

        User userFromDB = User.builder()
                .id(userId)
                .email("maxim@gmail.com")
                .username("Maxim")
                .build();
        when(userRepository.findByEmailOrUsername(any(), any())).thenReturn(Optional.of(userFromDB));

        assertThrows(EmailAlreadyExistException.class, () -> userService.registerUser(registerRequest));
    }

    @Test
    void whenRegisterNewUser_andPasswordsDoNotMatch_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("maxim12@gmail.com")
                .username("Maxim5")
                .password("12345")
                .confirmPassword("123456")
                .build();

        User userFromDB = User.builder()
                .id(userId)
                .email("maxim@gmail.com")
                .username("Maxim")
                .build();

        when(userRepository.findByEmailOrUsername(registerRequest.getEmail(), registerRequest.getUsername())).thenReturn(Optional.empty());

        assertNotEquals(registerRequest.getPassword(), registerRequest.getConfirmPassword());
        assertThrows(RuntimeException.class, () -> userService.registerUser(registerRequest));
    }

    @Test
    void whenAllDataIsValid_thenRegisterUserSuccessfully() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Maxim12")
                .firstName("Maxim")
                .lastName("Stefanov")
                .email("maxim@gmail.com")
                .password("12345")
                .confirmPassword("12345")
                .build();

        when(userRepository.findByEmailOrUsername(any(), any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345")).thenReturn("ENCODED_PASSWORD");

        userService.registerUser(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User user = captor.getValue();

        assertEquals(registerRequest.getUsername(), user.getUsername());
        assertEquals("ENCODED_PASSWORD", user.getPassword());
        assertEquals(registerRequest.getEmail(), user.getEmail());
        assertEquals(registerRequest.getFirstName(), user.getFirstName());
        assertThat(user.getCreatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        assertEquals(registerRequest.getLastName(), user.getLastName());
        assertEquals(UserRole.USER, user.getRole());
        assertNull(user.getImage());
        assertThat(user.getModifiedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        assertTrue(user.isActive());
        assertTrue(user.isEmailNotificationEnabled());
        assertTrue(user.isDeadLineNotificationEnabled());
        assertTrue(user.isSummaryNotificationEnabled());
        assertTrue(user.isReminderNotificationEnabled());
        assertTrue(user.isProfileCompleted());

        verify(notificationService).upsertPreferences(user.getId(), true, true, true, true, user.getEmail());
        verify(notificationService).sendNotification(user.getId(), NotificationMessages.USER_REGISTER_SUBJECT,
                NotificationMessages.USER_REGISTER_BODY.formatted(user.getUsername()), NotificationType.EMAIL);
    }

    @Test
    void whenChangeUserRole_andRepositoryReturnsUser_thenUserIsUpdatedWithRoleModeratorAndUpdatedOnNow_andPersistedInTheDatabase() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .role(UserRole.USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId);

        assertEquals(UserRole.MODERATOR, user.getRole());
        assertThat(user.getModifiedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        verify(userRepository).save(user);
    }

    @Test
    void whenChangeUserRole_andRepositoryReturnsModerator_thenUserIsUpdatedWithRoleAdminAndUpdatedOnNow_andPersistedInTheDatabase() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .role(UserRole.MODERATOR)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId);

        assertEquals(UserRole.ADMIN, user.getRole());
        assertThat(user.getModifiedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        verify(userRepository).save(user);
    }

    @Test
    void whenChangeUserRole_andRepositoryReturnsAdmin_thenUserIsUpdatedWithRoleUserAndUpdatedOnNow_andPersistedInTheDatabase() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId);

        assertEquals(UserRole.USER, user.getRole());
        assertThat(user.getModifiedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        verify(userRepository).save(user);
    }

    @Test
    void whenChangeUserRole_andRepositoryReturnsOptionalEmpty_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.changeRole(userId));
    }

    @Test
    void whenUpdateUserProfile_andRepositoryReturnsOptionalEmpty_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateProfile(userId, null));
    }

    @Test
    void whenUpdateUserProfile_andRepositoryReturnsUserFromDatabase_thenUpdateTheUserAndSaveToDatabase() {
        UUID userId = UUID.randomUUID();
        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .username("Ivan2")
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan@gmail.com")
                .build();

        User fromDB = User.builder()
                .id(userId)
                .username("Petar2")
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar@gmail.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(fromDB));

        userService.updateProfile(userId, editProfileRequest);

        assertEquals("Ivan2", fromDB.getUsername());
        assertEquals("Ivan", fromDB.getFirstName());
        assertEquals("Ivanov", fromDB.getLastName());
        assertEquals("ivan@gmail.com", fromDB.getEmail());
        verify(userRepository).save(fromDB);
    }

    @Test
    void whenUpdateUserProfile_andRepositoryReturnsUserAndProfileRequestComesWithNonEmptyEmail_thenInvokeUpsertNotificationPreferenceWithTrue() {
        UUID userId = UUID.randomUUID();

        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .email("ivan@gmail.com")
                .build();

        User user = User.builder()
                .id(userId)
                .deadLineNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .reminderNotificationEnabled(false)
                .build();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        userService.updateProfile(userId, editProfileRequest);

        verify(notificationService).upsertPreferences(userId, true, true,
                true, false, "ivan@gmail.com");
    }

    @Test
    void whenUpdateUserProfile_andRepositoryReturnsUserAndProfileRequestComesWithEmptyEmail_thenInvokeUpsertNotificationPreferenceWithFalse() {
        UUID userId = UUID.randomUUID();

        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .email(null)
                .build();

        User user = User.builder()
                .id(userId)
                .deadLineNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .reminderNotificationEnabled(false)
                .build();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        userService.updateProfile(userId, editProfileRequest);

        verify(notificationService).upsertPreferences(userId, false, true,
                true, false, null);
    }

    @Test
    void whenBlockUser_andRepositoryReturnsOptionalEmpty_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.blockAccount(userId));
    }

    @Test
    void whenBlockUser_andRepositoryReturnsUserFromDatabaseAndIsActive_thenUserIsSwitchedToInactive_andPersistedToDatabase() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .active(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.blockAccount(userId);

        assertFalse(user.isActive());
    }

    @Test
    void whenBlockUser_andRepositoryReturnsUserFromDatabaseAndIsInactive_thenUserIsSwitchedToActive_andPersistedToDatabase() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .active(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.blockAccount(userId);

        assertTrue(user.isActive());
    }

    @Test
    void whenUserRegisterViaOAuth2_andEmailIsNull_thenThrowsException() {
        UserCreateDto dto = new UserCreateDto();

        assertThrows(IllegalStateException.class, () -> userService.registerViaOauth2(dto, null, null));
    }

    @Test
    void whenUserRegisterViaOAuth2_andUserWithEmailExistsInDatabase_thenThrowsException() {
        UserCreateDto dto = new UserCreateDto();
        Object email = "ivan@gmail.com";

        User user = User.builder()
                .email(email.toString())
                .build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.registerViaOauth2(dto, email, null));
    }

    @Test
    void whenUserRegisterViaOAuth2_andRegistrationIsSuccessful_thenPersistUserToTheDatabase() {
        UserCreateDto dto = UserCreateDto.builder()
                .username("Ivan5")
                .firstName("Ivan")
                .lastName("Petrov")
                .build();

        Object email = "ivan@gmail.com";
        String provider = "Google";

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        userService.registerViaOauth2(dto, email, provider);

        verify(userRepository).save(captor.capture());

        User user = captor.getValue();

        assertEquals("Ivan5", user.getUsername());
        assertEquals("ivan@gmail.com", user.getEmail());
        assertThat(user.getCreatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        assertEquals("Ivan", user.getFirstName());
        assertEquals("Petrov", user.getLastName());
        assertEquals(UserRole.USER, user.getRole());
        assertNull(user.getImage());
        assertThat(user.getModifiedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        assertTrue(user.isActive());
        assertTrue(user.isEmailNotificationEnabled());
        assertTrue(user.isDeadLineNotificationEnabled());
        assertTrue(user.isSummaryNotificationEnabled());
        assertTrue(user.isReminderNotificationEnabled());
        assertTrue(user.isProfileCompleted());
        assertEquals(provider, user.getProvider());
    }

    @Test
    void whenUserEditPreferences_andRepositoryReturnsOptionalEmpty_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.editPreferences(null, userId));
    }

    @Test
    void whenUserEditPreferences_andRepositoryReturnsUser_thenUpdateUserPreferences() {
        UUID userId = UUID.randomUUID();
        EditPreferenceRequest editPreferenceRequest = EditPreferenceRequest.builder()
                .userId(userId)
                .emailNotificationEnabled(true)
                .deadLineNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .reminderNotificationEnabled(true)
                .build();

        User user = User.builder()
                .id(userId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.editPreferences(editPreferenceRequest, userId);

        assertEquals(userId, user.getId());
        assertEquals(editPreferenceRequest.isEmailNotificationEnabled(), user.isEmailNotificationEnabled());
        assertEquals(editPreferenceRequest.isDeadLineNotificationEnabled(), user.isDeadLineNotificationEnabled());
        assertEquals(editPreferenceRequest.isSummaryNotificationEnabled(), user.isSummaryNotificationEnabled());
        assertEquals(editPreferenceRequest.isReminderNotificationEnabled(), user.isReminderNotificationEnabled());
        assertThat(user.getModifiedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        verify(userRepository).save(user);
    }

    @Test
    void whenDeleteUser_andRepositoryReturnsOptionalEmpty_thenThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
    }

    @Test
    void whenDeleteUser_andRepositoryReturnsUser_thenDeleteUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void whenUserChangePassword_andCurrentPasswordDontMatchUserPassword_thenThrowsException() {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("password")
                .build();

        User user = User.builder()
                .password("password2")
                .build();

        when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.changePassword(changePasswordRequest, user));
    }

    @Test
    void whenUserChangePassword_andNewPasswordDontMatchConfirmPassword_thenThrowsException() {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("password")
                .newPassword("newPassword2")
                .confirmPassword("newPassword")
                .build();

        User user = User.builder()
                .password("password")
                .build();

        when(passwordEncoder.matches("password", "password")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.changePassword(changePasswordRequest, user));
    }

    @Test
    void whenUserChangePassword_andChangePasswordIsSuccessful_thenThrowsException() {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("password")
                .confirmPassword("newPassword")
                .newPassword("newPassword")
                .build();

        User user = User.builder()
                .password("encodedOldPassword")
                .build();

        when(passwordEncoder.matches("password", "encodedOldPassword"))
                .thenReturn(true);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("ENCODED_NEW_PASSWORD");

        userService.changePassword(changePasswordRequest, user);

        assertEquals("ENCODED_NEW_PASSWORD", user.getPassword());
        assertThat(user.getModifiedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(notificationService).sendNotification(
                eq(user.getId()),
                anyString(),
                anyString(),
                eq(NotificationType.EMAIL)
        );

        verify(userRepository).save(user);
    }

    @Test
    void whenLoadUserByUsername_andRepositoryReturnsOptionalEmpty_thenThrowsException() {
        String email = "email";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(email));
    }

    @Test
    void whenLoadUserByUsername_andRepositoryReturnsUser_thenLoadUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("email")
                .password("password")
                .active(true)
                .role(UserRole.USER)
                .build();
        when(userRepository.findByEmail("email")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("email");

        assertEquals("email", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void whenGetAllUsers_andRepositoryReturnsNonEmptyList_thenReturnUsersFromDatabase() {
        List<User> users = List.of(User.builder().username("Ivan").build(), User.builder().username("Petar").build());

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(users.size(), result.size());
        assertEquals(users, result);
        verify(userRepository).findAll();
    }

    @Test
    void whenGetAllUsers_andRepositoryReturnsEmptyList_thenReturnEmptyList() {
        List<User> users = List.of();

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userRepository.findAll();

        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void whenGetByUsername_andRepositoryReturnsOptionalEmpty_thenThrowsException() {
        User user =  User.builder().username("Ivan").build();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getByUsername(user.getUsername()));
    }

    @Test
    void whenGetByUsername_andRepositoryReturnsUser_thenGetUser() {
        User user =  User.builder().username("Ivan").build();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        User result = userService.getByUsername(user.getUsername());
        assertEquals(user, result);
    }

    @Test
    void whenGetSortedUsers_andRepositoryReturnsEmptyList_thenThrowsException() {
        List<User> users = Collections.emptyList();
        when(userRepository.findAll()).thenReturn(users);
        List<User> sortedUsers = userService.getSortedUsers();
        assertTrue(sortedUsers.isEmpty());
    }
}