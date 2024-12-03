package ru.javaprojects.javaprojects.projects.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.javaprojects.common.HasIdAndName;
import ru.javaprojects.javaprojects.common.model.BaseEntity;
import ru.javaprojects.javaprojects.common.validation.NoHtml;

@Entity
@Table(name = "tags",
        uniqueConstraints = @UniqueConstraint(columnNames = "tag", name = "tags_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Tag extends BaseEntity implements HasIdAndName {
    @NotBlank
    @NoHtml
    @Size(max = 32)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public Tag(Long id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Tag[id=%d, name=%s]", id, name);
    }
}
