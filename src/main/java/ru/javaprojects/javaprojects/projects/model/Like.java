package ru.javaprojects.javaprojects.projects.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.javaprojects.common.HasId;
import ru.javaprojects.javaprojects.common.model.BaseEntity;

@Entity
@Table(name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"object_id", "user_id"}, name = "likes_unique_object_like_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Like extends BaseEntity implements HasId {
    @NotNull
    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false)
    private ObjectType objectType;

    public Like(Long id, Long objectId, Long userId, ObjectType objectType) {
        super(id);
        this.objectId = objectId;
        this.userId = userId;
        this.objectType = objectType;
    }

    @Override
    public String toString() {
        return String.format("Like[id=%d, objectType=%s, objectId=%d]", id, objectType, objectId);
    }
}
