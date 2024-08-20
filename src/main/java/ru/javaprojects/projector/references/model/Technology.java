package ru.javaprojects.projector.references.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.util.validation.NoHtml;

@Entity
@Table(name = "technologies", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "technologies_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Technology extends BaseEntity implements HasId {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank
    @NoHtml
    @URL
    @Size(min = 2, max = 512)
    @Column(name = "url", nullable = false)
    private String url;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "usage")
    private Usage usage;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "priority")
    private Priority priority;

    @NotNull
    @Embedded
    @Valid
    private LogoFile logoFile;

    public Technology(Long id, String name, String url, Usage usage, Priority priority, LogoFile logoFile) {
        super(id);
        this.name = name;
        this.url = url;
        this.usage = usage;
        this.priority = priority;
        this.logoFile = logoFile;
    }

    @Override
    public String toString() {
        return String.format("Technology[id=%d, name=%s]", id, name);
    }
}
