package ru.javaprojects.projector.references.technologies;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.BaseTo;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.HasImageFileString;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.util.validation.ImageFile;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.references.technologies.model.Usage;

@Getter
@Setter
@NoArgsConstructor
public class TechnologyTo extends BaseTo implements HasIdAndName, HasImageFileString {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String name;

    @NotBlank
    @NoHtml
    @URL
    @Size(min = 2, max = 512)
    private String url;

    @NotNull
    private Usage usage;

    @NotNull
    private Priority priority;

    @Nullable
    @NoHtml
    @Size(max = 128)
    private String logoFileName;

    @Nullable
    @NoHtml
    @Size(max = 512)
    private String logoFileLink;

    @Nullable
    @ImageFile
    private MultipartFile logoFile;

    @Nullable
    private String logoFileAsString;

    public TechnologyTo(Long id, String name, String url, Usage usage, Priority priority, MultipartFile logoFile) {
        super(id);
        this.name = name;
        this.url = url;
        this.usage = usage;
        this.priority = priority;
        this.logoFile = logoFile;
    }

    public TechnologyTo(Long id, String name, String url, Usage usage, Priority priority, String logoFileName,
                        String logoFileLink) {
        super(id);
        this.name = name;
        this.url = url;
        this.usage = usage;
        this.priority = priority;
        this.logoFileName = logoFileName;
        this.logoFileLink = logoFileLink;
    }

    public String getLogoFileAsStringSrc() {
        if (logoFileAsString == null || logoFileName == null) {
            return null;
        }
        String srcType = logoFileName.endsWith(".svg") ?
                "data:image/svg+xml;base64," : "data:image/*;base64,";
        return srcType + logoFileAsString;
    }

    @Override
    public String getImageFileString() {
        return logoFileAsString;
    }

    @Override
    public String getFileName() {
        return logoFileName;
    }

    @Override
    public String toString() {
        return String.format("TechnologyTo[id=%d, name=%s]", id, name);
    }
}
