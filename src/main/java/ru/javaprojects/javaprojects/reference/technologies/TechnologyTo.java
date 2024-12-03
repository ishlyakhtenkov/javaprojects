package ru.javaprojects.javaprojects.reference.technologies;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import ru.javaprojects.javaprojects.common.HasIdAndName;
import ru.javaprojects.javaprojects.common.model.Priority;
import ru.javaprojects.javaprojects.common.to.BaseTo;
import ru.javaprojects.javaprojects.common.to.FileTo;
import ru.javaprojects.javaprojects.common.validation.ImageFile;
import ru.javaprojects.javaprojects.common.validation.NoHtml;
import ru.javaprojects.javaprojects.reference.ReferenceTo;
import ru.javaprojects.javaprojects.reference.technologies.model.Usage;

@Getter
@Setter
@NoArgsConstructor
public class TechnologyTo extends BaseTo implements HasIdAndName, ReferenceTo {
    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String name;

    @NotBlank
    @NoHtml
    @URL
    @Size(max = 512)
    private String url;

    @NotNull
    private Usage usage;

    @NotNull
    private Priority priority;

    @Nullable
    @Valid
    @ImageFile
    private FileTo logo;

    public TechnologyTo(Long id, String name, String url, Usage usage, Priority priority, FileTo logo) {
        super(id);
        this.name = name;
        this.url = url;
        this.usage = usage;
        this.priority = priority;
        this.logo = logo;
    }

    @Override
    public String toString() {
        return String.format("TechnologyTo[id=%d, name=%s]", id, name);
    }
}
