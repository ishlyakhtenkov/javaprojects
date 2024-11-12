package ru.javaprojects.projector.reference.architectures.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaprojects.projector.common.validation.NoHtml;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class LocalizedFields {
    @NotBlank
    @NoHtml
    @Size(max = 3)
    @Column(name = "locale", nullable = false)
    private String locale;

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
}
