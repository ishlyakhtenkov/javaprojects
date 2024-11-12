package ru.javaprojects.projector.reference.architectures.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.validation.NoHtml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "architectures",
        uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "architectures_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Architecture extends BaseEntity implements HasIdAndName {
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

    @Valid
    @CollectionTable(name = "architecture_localized_fields", joinColumns = @JoinColumn(name = "architecture_id", nullable = false))
    @ElementCollection(fetch = FetchType.LAZY)
    private Set<LocalizedFields> localizedFields;

    public Architecture(Long id, String name, String description, File logo, Set<LocalizedFields> localizedFields) {
        super(id);
        this.name = name;
        this.description = description;
        this.logo = logo;
        this.localizedFields = localizedFields;
    }

    public Architecture(Architecture a) {
        this(a.id, a.name, a.description, a.logo, new HashSet<>(a.localizedFields));
    }

    @Override
    public String toString() {
        return String.format("Architecture[id=%d, name=%s]", id, name);
    }
}
