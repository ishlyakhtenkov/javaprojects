package ru.javaprojects.projector.projects.to;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.util.validation.ImageFile;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.common.util.validation.YamlFile;
import ru.javaprojects.projector.references.architectures.Architecture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ProjectTo extends BaseTo implements HasIdAndName {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 64)
    private String name;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    private String shortDescription;

    private boolean enabled;

    @NotNull
    private Priority priority;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private Architecture architecture;

    @Nullable
    @ImageFile
    private MultipartFile logoFile;

    @Nullable
    @YamlFile
    private MultipartFile dockerComposeFile;

    @Nullable
    @ImageFile
    private MultipartFile cardImageFile;

    @Nullable
    @NoHtml
    @URL
    @Size(max = 512)
    private String deploymentUrl;

    @NotBlank
    @NoHtml
    @URL
    @Size(max = 512)
    private String backendSrcUrl;

    @Nullable
    @NoHtml
    @URL
    @Size(max = 512)
    private String frontendSrcUrl;

    @Nullable
    @NoHtml
    @URL
    @Size(max = 512)
    private String openApiUrl;

    @NotEmpty
    private Set<Long> technologiesIds;

    @Valid
    private List<DescriptionElementTo> descriptionElementTos = new ArrayList<>();

    public ProjectTo(Long id, String name, String shortDescription, boolean enabled, Priority priority, LocalDate startDate,
                     LocalDate endDate, Architecture architecture, String deploymentUrl, String backendSrcUrl,
                     String frontendSrcUrl, String openApiUrl, Set<Long> technologiesIds,
                     List<DescriptionElementTo> descriptionElementTos) {
        super(id);
        this.name = name;
        this.shortDescription = shortDescription;
        this.enabled = enabled;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.architecture = architecture;
        this.deploymentUrl = deploymentUrl;
        this.backendSrcUrl = backendSrcUrl;
        this.frontendSrcUrl = frontendSrcUrl;
        this.openApiUrl = openApiUrl;
        this.technologiesIds = technologiesIds;
        this.descriptionElementTos = descriptionElementTos;
    }

    public ProjectTo(Long id, String name, String shortDescription, boolean enabled, Priority priority, LocalDate startDate,
                     LocalDate endDate, Architecture architecture, MultipartFile logoFile, MultipartFile dockerComposeFile,
                     MultipartFile cardImageFile, String deploymentUrl, String backendSrcUrl, String frontendSrcUrl,
                     String openApiUrl, Set<Long> technologiesIds, List<DescriptionElementTo> descriptionElementTos) {
        this(id, name, shortDescription, enabled, priority, startDate, endDate, architecture, deploymentUrl, backendSrcUrl,
                frontendSrcUrl, openApiUrl, technologiesIds, descriptionElementTos);
        this.logoFile = logoFile;
        this.dockerComposeFile = dockerComposeFile;
        this.cardImageFile = cardImageFile;
    }

    @Override
    public String toString() {
        return String.format("ProjectTo[id=%d, name=%s]", id, name);
    }
}
