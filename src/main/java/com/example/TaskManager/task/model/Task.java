package com.example.TaskManager.task.model;

import com.example.TaskManager.project.model.Project;
import com.example.TaskManager.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Size(min = 3, max = 20)
    private String title;

    @Column
    @Length(max = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime completedOn;

    @Column(nullable = false)
    private boolean deleted;

    private boolean notifiedOverdue;

    @ManyToOne
    private User user;

    @ManyToOne(optional = true)
    private Project project;

    private boolean notifiedUpcoming;

}
