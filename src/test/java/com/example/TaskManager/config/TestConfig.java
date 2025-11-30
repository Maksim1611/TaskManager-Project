package com.example.TaskManager.config;

import com.example.TaskManager.project.security.ProjectSecurity;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    ProjectSecurity projectSecurity() {
        return Mockito.mock(ProjectSecurity.class);
    }

}
