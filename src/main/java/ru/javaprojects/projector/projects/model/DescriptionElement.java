package ru.javaprojects.projector.projects.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.validation.NoHtml;

@Entity
@Table(name = "description_elements")
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
    @Embedded
    @Valid
    private File image;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public DescriptionElement(DescriptionElement de) {
        this(de.id, de.type, de.index, de.text,
                de.image == null ? null : new File(de.image.getFileName(), de.image.getFileLink()));
    }

    public DescriptionElement(Long id, ElementType type, Byte index, String text, File image) {
        super(id);
        this.type = type;
        this.index = index;
        this.text = text;
        this.image = image;
    }

    public DescriptionElement(Long id, ElementType type, Byte index, String text, File image, Project project) {
        this(id, type, index, text, image);
        this.project = project;
    }

    @Override
    public int compareTo(DescriptionElement o) {
        return Integer.compare(this.index, o.index);
    }
}
