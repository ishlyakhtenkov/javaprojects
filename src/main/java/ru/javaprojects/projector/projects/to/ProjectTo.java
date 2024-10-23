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
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.validation.ImageFile;
import ru.javaprojects.projector.common.validation.NoHtml;
import ru.javaprojects.projector.common.validation.YamlFile;
import ru.javaprojects.projector.reference.architectures.Architecture;

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
    @Size(max = 64)
    private String name;

    @NotBlank
    @NoHtml
    @Size(max = 128)
    private String annotation;

    private boolean visible;

    @NotNull
    private Priority priority;

    @NotNull
    private LocalDate started;

    @NotNull
    private LocalDate finished;

    @NotNull
    private Architecture architecture;

    @Nullable
    @Valid
    @ImageFile
    private FileTo logo;

    @Nullable
    @Valid
    @YamlFile
    private FileTo dockerCompose;

    @Nullable
    @Valid
    @ImageFile
    private FileTo preview;

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

    public ProjectTo(Long id, String name, String annotation, boolean visible, Priority priority, LocalDate started,
                     LocalDate finished, Architecture architecture, String logoFileName, String logoFileLink,
                     String dockerComposeFileName, String dockerComposeFileLink, String previewFileName,
                     String previewFileLink, String deploymentUrl, String backendSrcUrl, String frontendSrcUrl,
                     String openApiUrl, Set<Long> technologiesIds, List<DescriptionElementTo> descriptionElementTos) {
        super(id);
        this.name = name;
        this.annotation = annotation;
        this.visible = visible;
        this.priority = priority;
        this.started = started;
        this.finished = finished;
        this.architecture = architecture;
        this.logo = new FileTo(logoFileName, logoFileLink, null, null);
        this.dockerCompose = new FileTo(dockerComposeFileName, dockerComposeFileLink, null, null);
        this.preview = new FileTo(previewFileName, previewFileLink, null, null);
        this.deploymentUrl = deploymentUrl;
        this.backendSrcUrl = backendSrcUrl;
        this.frontendSrcUrl = frontendSrcUrl;
        this.openApiUrl = openApiUrl;
        this.technologiesIds = technologiesIds;
        this.descriptionElementTos = descriptionElementTos;
    }

    public ProjectTo(Long id, String name, String annotation, boolean visible, Priority priority, LocalDate started,
                     LocalDate finished, Architecture architecture, MultipartFile logoMultipartFile,
                     MultipartFile dockerComposeMultipartFile, MultipartFile previewMultipartFile,
                     String deploymentUrl, String backendSrcUrl, String frontendSrcUrl, String openApiUrl,
                     Set<Long> technologiesIds, List<DescriptionElementTo> descriptionElementTos) {
        super(id);
        this.name = name;
        this.annotation = annotation;
        this.visible = visible;
        this.priority = priority;
        this.started = started;
        this.finished = finished;
        this.architecture = architecture;
        this.logo = new FileTo(null, null, logoMultipartFile, null);
        this.dockerCompose = new FileTo(null, null, dockerComposeMultipartFile, null);;
        this.preview = new FileTo(null, null, previewMultipartFile, null);;
        this.deploymentUrl = deploymentUrl;
        this.backendSrcUrl = backendSrcUrl;
        this.frontendSrcUrl = frontendSrcUrl;
        this.openApiUrl = openApiUrl;
        this.technologiesIds = technologiesIds;
        this.descriptionElementTos = descriptionElementTos;
    }

    @Override
    public String toString() {
        return String.format("ProjectTo[id=%d, name=%s]", id, name);
    }
}
