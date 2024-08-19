package ru.javaprojects.projector.references;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.references.model.Usage;

@Getter
@Setter
@NoArgsConstructor
public class TechnologyTo extends BaseTo {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String name;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 512)
    private String url;

    @NotNull
    private Usage usage;

    @NotNull
    private MultipartFile imageFile;
}
