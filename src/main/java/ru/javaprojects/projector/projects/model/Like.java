package ru.javaprojects.projector.projects.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.users.model.User;

@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}, name = "likes_unique_project_like_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Like extends BaseEntity implements HasId {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Like(Long id, Project project, User user) {
        super(id);
        this.project = project;
        this.user = user;
    }
}
