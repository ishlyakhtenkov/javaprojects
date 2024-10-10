package ru.javaprojects.projector.reference.technologies.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.validation.NoHtml;

@Entity
@Table(name = "technologies",
        uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "technologies_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Technology extends BaseEntity implements HasIdAndName, Comparable<Technology> {
    @NotBlank
    @NoHtml
    @Size(max = 32)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank
    @NoHtml
    @URL
    @Size(max = 512)
    @Column(name = "url", nullable = false)
    private String url;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "usage")
    private Usage usage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority;

    @NotNull
    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "fileName", column = @Column(name = "logo_file_name")),
            @AttributeOverride(name = "fileLink", column = @Column(name = "logo_file_link"))
    })
    private File logo;

    public Technology(Long id, String name, String url, Usage usage, Priority priority, File logo) {
        super(id);
        this.name = name;
        this.url = url;
        this.usage = usage;
        this.priority = priority;
        this.logo = logo;
    }

    @Override
    public int compareTo(Technology o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }

    @Override
    public String toString() {
        return String.format("Technology[id=%d, name=%s]", id, name);
    }
}
