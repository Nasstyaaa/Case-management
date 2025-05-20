package org.example.casemanagement.model.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "board")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @Builder.Default
    @Column(name = "public")
    private boolean isPublic = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private User owner;

    @ManyToMany
    @JoinTable(
        name = "board_members",
        joinColumns = @JoinColumn(name = "board_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    @JsonIdentityReference(alwaysAsId = true)
    private Set<User> members = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board board)) return false;
        return id != null && id.equals(board.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Add custom getters for serialization
    @JsonProperty("ownerUsername")
    public String getOwnerUsername() {
        return owner != null ? owner.getUsername() : null;
    }

    @JsonProperty("memberUsernames")
    public Set<String> getMemberUsernames() {
        return members.stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
    }
} 