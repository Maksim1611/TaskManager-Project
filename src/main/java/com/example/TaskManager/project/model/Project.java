package com.example.TaskManager.project.model;

import com.example.TaskManager.tag.model.Tag;
import com.example.TaskManager.task.model.Task;
import com.example.TaskManager.user.model.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Size(min = 3, max = 30)
    private String title;

    @Column(nullable = false)
    @Length(max = 300)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime completedOn;

    @Column(nullable = false)
    private int completionPercent;

    @Column(nullable = false)
    private boolean deleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectVisibility projectVisibility;

    @ManyToOne
    private User user;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "project")
    private List<Task> tasks;

    @Nullable
    @OneToMany(mappedBy = "project")
    private List<Tag> tags;

    @ManyToMany
    @JoinTable(name = "projects_users", joinColumns = @JoinColumn(name = "project_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> members;

    private boolean notifiedUpcoming;

    private boolean notifiedOverdue;
}
