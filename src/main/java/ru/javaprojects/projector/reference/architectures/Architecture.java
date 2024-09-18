package ru.javaprojects.projector.reference.architectures;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.util.validation.NoHtml;

@Entity
@Table(name = "architectures", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "architectures_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Architecture extends BaseEntity implements HasIdAndName {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank
    @NoHtml
    @Size(min = 20, max = 400)
    @Column(name = "description", nullable = false)
    private String description;

    public Architecture(Long id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("Architecture[id=%d, name=%s]", id, name);
    }
}
