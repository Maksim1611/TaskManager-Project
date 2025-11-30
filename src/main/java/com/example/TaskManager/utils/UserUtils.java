package com.example.TaskManager.utils;

import com.example.TaskManager.security.UserData;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.model.UserRole;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@UtilityClass
public class UserUtils {

    public static User randomUser() {
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
                .provider("")
                .build();
    }
}
