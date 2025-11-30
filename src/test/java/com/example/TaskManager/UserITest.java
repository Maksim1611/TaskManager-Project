package com.example.TaskManager;

import com.example.TaskManager.notification.service.NotificationService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.user.service.UserService;
import com.example.TaskManager.web.dto.RegisterRequest;
import com.example.TaskManager.web.dto.UserCreateDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class UserITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenRegisterUser_theHappyPath() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("maxim12")
                .firstName("maxim")
                .lastName("stefanov")
                .email("maxim@gmail.com")
                .password("123123")
                .confirmPassword("123123")
                .build();

        userService.registerUser(registerRequest);
        User user = userService.getByUsername("maxim12");

        assertNotNull(user.getId());
        assertEquals("maxim@gmail.com", user.getEmail());
        assertTrue(userRepository.existsByEmail("maxim@gmail.com"));
    }

    @Test
    void whenRegisterUserViaOauth2_withGoogleProvider_theHappyPath() {

        String email = "maxim@gmail.com";
        UserCreateDto dto = UserCreateDto.builder()
                .username("maxim12")
                .firstName("maxim")
                .lastName("stefanov")
                .build();

        User user = userService.registerViaOauth2(dto, email, "Google");

        assertNotNull(user.getId());
        assertEquals("Google", user.getProvider());
        assertTrue(userRepository.existsByEmail(email));
    }
}
