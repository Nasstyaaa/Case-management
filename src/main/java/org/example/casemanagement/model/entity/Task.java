package org.example.casemanagement.model.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Board board;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_assignees",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    @JsonIdentityReference(alwaysAsId = true)
    private Set<User> assignees = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TaskDependency> dependencies = new HashSet<>();

    @OneToMany(mappedBy = "dependentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TaskDependency> dependentTasks = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Add custom getters for serialization
    @JsonProperty("assigneeUsernames")
    public Set<String> getAssigneeUsernames() {
        return assignees.stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
    }

    @JsonProperty("creatorUsername")
    public String getCreatorUsername() {
        return creator != null ? creator.getUsername() : null;
    }

    @JsonProperty("boardId")
    public Long getBoardId() {
        return board != null ? board.getId() : null;
    }

    public String getCompletionTime() {
        if (status != TaskStatus.DONE || completedAt == null) {
            return null;
        }

        Duration duration = Duration.between(createdAt, completedAt);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        StringBuilder time = new StringBuilder();
        if (days > 0) {
            time.append(days).append(days == 1 ? " day" : " days");
        }
        if (hours > 0) {
            if (time.length() > 0) time.append(", ");
            time.append(hours).append(hours == 1 ? " hour" : " hours");
        }
        if (minutes > 0 || (days == 0 && hours == 0)) {
            if (time.length() > 0) time.append(", ");
            time.append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }

        return time.toString();
    }

    public boolean isBlocked() {
        return dependentTasks.stream()
                .anyMatch(dep -> dep.getTask().getStatus() != TaskStatus.DONE);
    }

    public Set<Task> getBlockingTasks() {
        return dependentTasks.stream()
                .filter(dep -> dep.getTask().getStatus() != TaskStatus.DONE)
                .map(TaskDependency::getTask)
                .collect(Collectors.toSet());
    }

    public Set<Task> getDependentTasks() {
        return dependencies.stream()
                .map(TaskDependency::getDependentTask)
                .collect(Collectors.toSet());
    }
} 