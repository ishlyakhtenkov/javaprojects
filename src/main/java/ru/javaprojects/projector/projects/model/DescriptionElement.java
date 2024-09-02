package ru.javaprojects.projector.projects.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.util.validation.NoHtml;

@Entity
@Table(name = "description_elements", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "index"}, name = "description_elements_unique_project_index_idx"))
@Getter
@Setter
@NoArgsConstructor
public class DescriptionElement extends BaseEntity implements HasId, Comparable<DescriptionElement> {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ElementType type;

    @NotNull
    @PositiveOrZero
    @Column(name = "index", nullable = false)
    private Byte index;

    @Nullable
    @NoHtml
    @Size(max = 1024)
    @Column(name = "text")
    private String text;

    @Nullable
    @NoHtml
    @Size(max = 128)
    @Column(name = "file_name")
    private String fileName;

    @Nullable
    @NoHtml
    @Size(max = 512)
    @Column(name = "file_link")
    private String fileLink;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public DescriptionElement(Long id, ElementType type, Byte index, String text, String fileName, String fileLink,
                              Project project) {
        super(id);
        this.type = type;
        this.index = index;
        this.text = text;
        this.fileName = fileName;
        this.fileLink = fileLink;
        this.project = project;
    }

    @Override
    public int compareTo(DescriptionElement o) {
        return Integer.compare(this.index, o.index);
    }
}
