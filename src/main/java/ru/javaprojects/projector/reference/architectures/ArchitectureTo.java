package ru.javaprojects.projector.reference.architectures;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.validation.ImageFile;
import ru.javaprojects.projector.common.util.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class ArchitectureTo extends BaseTo implements HasIdAndName {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String name;

    @NotBlank
    @NoHtml
    @Size(min = 20, max = 400)
    private String description;

    @Nullable
    @ImageFile
    private FileTo logo;

    public ArchitectureTo(Long id, String name, String description, MultipartFile logoMultipartFile) {
        super(id);
        this.name = name;
        this.description = description;
        this.logo = new FileTo(null, null, logoMultipartFile, null);
    }

    public ArchitectureTo(Long id, String name, String description, String logoFileName, String logoFileLink) {
        super(id);
        this.name = name;
        this.description = description;
        this.logo = new FileTo(logoFileName, logoFileLink, null, null);
    }

    @Override
    public String toString() {
        return String.format("ArchitectureTo[id=%d, name=%s]", id, name);
    }
}
