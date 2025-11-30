package com.example.TaskManager.user.service;

import com.example.TaskManager.exception.user.EmailAlreadyExistException;
import com.example.TaskManager.exception.user.UserNotFoundException;
import com.example.TaskManager.exception.user.UsernameAlreadyExistException;
import com.example.TaskManager.notification.NotificationType;
import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.notification.NotificationMessages;
import com.example.TaskManager.user.model.UserRole;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.web.dto.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        return UserData.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .isActive(user.isActive())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        Optional<User> userOptional = this.userRepository.findByEmailOrUsername(registerRequest.getEmail(), registerRequest.getUsername());

        boolean present = userOptional.isPresent();

        if (present && userOptional.get().getUsername().equals(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        if (present && userOptional.get().getEmail().equals(registerRequest.getEmail())) {
            throw new EmailAlreadyExistException("Account with this email already exist.");
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match!");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .createdOn(LocalDateTime.now())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .role(UserRole.USER)
                .image(null)
                .modifiedOn(LocalDateTime.now())
                .active(true)
                .emailNotificationEnabled(true)
                .deadLineNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .reminderNotificationEnabled(true)
                .profileCompleted(true)
                .provider("")
                .build();

        this.userRepository.save(user);
        log.info("New user [%s] profile was successfully registered!".formatted(user.getUsername()));
        notificationService.upsertPreferences(user.getId(), true, true, true, true, user.getEmail());
        notificationService.sendNotification(user.getId(), NotificationMessages.USER_REGISTER_SUBJECT,
                NotificationMessages.USER_REGISTER_BODY.formatted(user.getUsername()), NotificationType.EMAIL);
    }

    public User getById(UUID userId) {
        return this.userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with id: [%s] does not exist".formatted(userId.toString())));
    }

    public void update(User user) {
        user.setModifiedOn(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public void updateProfile(UUID id, @Valid EditProfileRequest editProfileRequest) {
        User user = getById(id);

        if (editProfileRequest.getEmail() != null && !editProfileRequest.getEmail().isBlank()) {
            notificationService.upsertPreferences(id, true, user.isDeadLineNotificationEnabled(),
                    user.isSummaryNotificationEnabled(), user.isReminderNotificationEnabled(), editProfileRequest.getEmail());
        } else {
            notificationService.upsertPreferences(id, false, user.isDeadLineNotificationEnabled(),
                    user.isSummaryNotificationEnabled(), user.isReminderNotificationEnabled(), editProfileRequest.getEmail());
        }


        user.setUsername(editProfileRequest.getUsername());
        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());
        user.setEmail(editProfileRequest.getEmail());

        update(user);
        log.info("User with id: [%s] has updated its profile.".formatted(id));
    }

    @Transactional
    public void changePassword(@Valid ChangePasswordRequest changePasswordRequest, User user) {
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Passwords do not match!");
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match!");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        update(user);
        notificationService.sendNotification(user.getId(), NotificationMessages.PASSWORD_CHANGE_BODY.formatted(user.getUsername()),
                NotificationMessages.PASSWORD_CHANGE_SUBJECT, NotificationType.EMAIL);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = getById(id);

        userRepository.delete(user);
        notificationService.sendNotification(user.getId(), NotificationMessages.USER_DELETE_SUBJECT,
                NotificationMessages.USER_DELETE_BODY.formatted(user.getUsername()), NotificationType.EMAIL);

    }

    public void editPreferences(@Valid EditPreferenceRequest editPreferenceRequest, UUID id) {
        User user = getById(id);
        user.setEmailNotificationEnabled(editPreferenceRequest.isEmailNotificationEnabled());
        user.setDeadLineNotificationEnabled(editPreferenceRequest.isDeadLineNotificationEnabled());
        user.setSummaryNotificationEnabled(editPreferenceRequest.isSummaryNotificationEnabled());
        user.setReminderNotificationEnabled(editPreferenceRequest.isReminderNotificationEnabled());

        update(user);
        log.info("User with id: [%s] has updated its preferences.".formatted(id));
    }

    @Transactional
    public User registerViaOauth2(UserCreateDto userCreateDto, Object email, String provider) {
        if (email == null) {
            throw new IllegalStateException(provider + " email not found in session!");
        }

        Optional<User> optional = userRepository.findByEmail(email.toString());

        if (optional.isPresent()) {
            throw new RuntimeException("User with email [%s] already exists!".formatted(email.toString()));
        }

        User user = User.builder()
                .username(userCreateDto.getUsername())
                .email(email.toString())
                .password(UUID.randomUUID().toString())
                .createdOn(LocalDateTime.now())
                .firstName(userCreateDto.getFirstName())
                .lastName(userCreateDto.getLastName())
                .role(UserRole.USER)
                .image(null)
                .modifiedOn(LocalDateTime.now())
                .active(true)
                .emailNotificationEnabled(true)
                .deadLineNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .reminderNotificationEnabled(true)
                .profileCompleted(true)
                .provider(provider)
                .build();

        User save = userRepository.save(user);
        log.info("New user [%s] profile was successfully registered!".formatted(user.getUsername()));
        notificationService.upsertPreferences(user.getId(), true, true, true, true, user.getEmail());
        notificationService.sendNotification(user.getId(), NotificationMessages.USER_REGISTER_SUBJECT,
                NotificationMessages.USER_REGISTER_BODY.formatted(user.getUsername()), NotificationType.EMAIL);

        return save;
    }

    public void changeRole(UUID userId) {
        User user = getById(userId);
        UserRole role = user.getRole();

        if (role == UserRole.USER) {
            user.setRole(UserRole.MODERATOR);
        } else if (role == UserRole.MODERATOR) {
            user.setRole(UserRole.ADMIN);
        } else if (role == UserRole.ADMIN) {
            user.setRole(UserRole.USER);
        }

        update(user);
    }

    public void blockAccount(UUID userId) {
        User user = getById(userId);
        boolean active = user.isActive();

        user.setActive(!active);

        update(user);
    }

    public List<User> getSortedUsers() {
        return getAllUsers().stream().sorted(Comparator.comparingInt(u ->
                u.getRole() == UserRole.ADMIN ? 0 : u.getRole() == UserRole.MODERATOR ? 1 : 2)).toList();
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }
}
