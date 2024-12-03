package ru.javaprojects.javaprojects.projects.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.javaprojects.javaprojects.common.HasIdAndParentId;
import ru.javaprojects.javaprojects.common.model.BaseEntity;
import ru.javaprojects.javaprojects.common.validation.NoHtml;
import ru.javaprojects.javaprojects.users.model.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
public class Comment extends BaseEntity implements HasIdAndParentId {
    @NotNull
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Nullable
    @Column(name = "parent_id")
    private Long parentId;

    @NotBlank
    @NoHtml
    @Size(max = 4096)
    @Column(name = "text")
    private String text;

    @CreationTimestamp
    @Column(name = "created", nullable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated", columnDefinition = "timestamp")
    private LocalDateTime updated;

    @Column(name = "deleted", nullable = false, columnDefinition = "bool default false")
    private boolean deleted;

    @OneToMany(mappedBy = "objectId", fetch = FetchType.LAZY)
    private Set<Like> likes = new HashSet<>();

    public Comment(Long id, Long projectId, User author, Long parentId, String text, LocalDateTime created,
                   LocalDateTime updated, boolean deleted, Set<Like> likes) {
        this(id, projectId, author, parentId, text);
        this.created = created;
        this.updated = updated;
        this.deleted = deleted;
        this.likes = likes;
    }

    public Comment(Long id, Long projectId, User author, Long parentId, String text) {
        super(id);
        this.projectId = projectId;
        this.author = author;
        this.parentId = parentId;
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("Comment[id=%d, userId=%d, projectId=%d]", id, author.getId(), projectId);
    }
}
