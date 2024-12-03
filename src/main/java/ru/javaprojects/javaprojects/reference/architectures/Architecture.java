package ru.javaprojects.javaprojects.reference.architectures;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.javaprojects.common.HasIdAndName;
import ru.javaprojects.javaprojects.common.model.BaseEntity;
import ru.javaprojects.javaprojects.common.model.File;
import ru.javaprojects.javaprojects.common.validation.NoHtml;
import ru.javaprojects.javaprojects.reference.Reference;

@Entity
@Table(name = "architectures",
        uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "architectures_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Architecture extends BaseEntity implements HasIdAndName, Reference {
    @NotBlank
    @NoHtml
    @Size(max = 32)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank
    @NoHtml
    @Size(max = 400)
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "fileName", column = @Column(name = "logo_file_name")),
            @AttributeOverride(name = "fileLink", column = @Column(name = "logo_file_link"))
    })
    private File logo;

    public Architecture(Long id, String name, String description, File logo) {
        super(id);
        this.name = name;
        this.description = description;
        this.logo = logo;
    }

    @Override
    public String toString() {
        return String.format("Architecture[id=%d, name=%s]", id, name);
    }
}
