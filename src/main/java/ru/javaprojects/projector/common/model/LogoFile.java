package ru.javaprojects.projector.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaprojects.projector.common.util.validation.NoHtml;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class LogoFile implements File {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    @Column(name = "logo_file_name")
    private String fileName;

    @NotBlank
    @Size(min = 2, max = 512)
    @Column(name = "logo_file_link")
    private String fileLink;
}
