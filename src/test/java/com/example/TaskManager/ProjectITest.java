package com.example.TaskManager;

import com.example.TaskManager.activity.model.ActivityType;
import com.example.TaskManager.activity.service.ActivityService;
import com.example.TaskManager.analytics.service.ProjectAnalyticsService;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.project.model.ProjectStatus;
import com.example.TaskManager.project.model.ProjectVisibility;
import com.example.TaskManager.project.repository.ProjectRepository;
import com.example.TaskManager.project.service.ProjectService;
import com.example.TaskManager.tag.model.Tag;
import com.example.TaskManager.tag.service.TagService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.repository.UserRepository;
import com.example.TaskManager.utils.UserUtils;
import com.example.TaskManager.web.dto.CreateProjectRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProjectITest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private ProjectAnalyticsService projectAnalyticsService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Test
    void createProject_whenValidRequest_persistsProjectCorrectly() {
        User user = UserUtils.randomUser();
        user.setId(null);

        userRepository.save(user);

        List<Tag> parsedTags = List.of(
                Tag.builder().title("Work").build(),
                Tag.builder().title("Java").build()
        );

        when(tagService.parseTags(List.of("Work", "Java"))).thenReturn(parsedTags);

        CreateProjectRequest request = CreateProjectRequest.builder()
                .title("My Project")
                .description("Desc...")
                .projectStatus(ProjectStatus.ACTIVE)
                .dueDate(LocalDateTime.now().plusDays(5))
                .projectVisibility(ProjectVisibility.PUBLIC)
                .tags("Work, Java")
                .build();

        projectService.createProject(request, user);

        List<Project> projects = projectRepository.findAll();
        assertEquals(1, projects.size());

        Project saved = projects.get(0);

        assertEquals("My Project", saved.getTitle());
        assertEquals("Desc...", saved.getDescription());
        assertEquals(ProjectStatus.ACTIVE, saved.getStatus());
        assertEquals(user.getId(), saved.getUser().getId());
        assertFalse(saved.isDeleted());
        assertEquals(0, saved.getCompletionPercent());
        assertEquals(2, saved.getTags().size());
        assertEquals(1, saved.getMembers().size());
        assertTrue(saved.getMembers().contains(user));

        verify(tagService).saveTags(parsedTags, saved);
        verify(activityService).createActivity(ActivityType.PROJECT_CREATED, user, saved);
        verify(projectAnalyticsService).upsertProjects(user.getId());
    }

}
