package ru.javaprojects.projector.projects.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.model.BaseEntity;

@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}, name = "likes_unique_project_like_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Like extends BaseEntity implements HasId {
    @NotNull
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    public Like(Long id, Long projectId, Long userId) {
        super(id);
        this.projectId = projectId;
        this.userId = userId;
    }
}
