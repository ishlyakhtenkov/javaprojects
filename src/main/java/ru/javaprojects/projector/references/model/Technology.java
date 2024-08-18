package ru.javaprojects.projector.references.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @Size(min = 2, max = 512)
    @Column(name = "href", nullable = false)
    private String href;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    @Column(name = "img_file_name")
    private String imgFileName;

    @NotBlank
    @Size(min = 2, max = 512)
    @Column(name = "img_file_link")
    private String imgFileLink;

    public Technology(Long id, String name, String href, String imgFileName, String imgFileLink) {
        super(id);
        this.name = name;
        this.href = href;
        this.imgFileName = imgFileName;
        this.imgFileLink = imgFileLink;
    }

    @Override
    public String toString() {
        return String.format("Technology[id=%d, name=%s]", id, name);
    }
}
