package com.example.TaskManager.user.model;

import com.example.TaskManager.activity.model.Activity;
import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.task.model.Task;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column
    @Size(min = 6)
    private String password;

    @Column(nullable = false)
    @Email
    private String email;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime modifiedOn;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<Task> tasks;

    @ManyToMany(mappedBy = "members")
    private List<Project> projects;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Activity> activities;

    @Column(nullable = false)
    private boolean emailNotificationEnabled;

    @Column(nullable = false)
    private boolean deadLineNotificationEnabled;

    @Column(nullable = false)
    private boolean summaryNotificationEnabled;

    @Column(nullable = false)
    private boolean reminderNotificationEnabled;

    private boolean profileCompleted = false;

    private String provider;
}
