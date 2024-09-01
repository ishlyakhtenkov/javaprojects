package ru.javaprojects.projector.projects.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.URL;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.LogoFile;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.references.architectures.Architecture;
import ru.javaprojects.projector.references.technologies.model.Technology;

import java.time.LocalDate;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "projects_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Project extends BaseEntity implements HasIdAndName {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 64)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Column(name = "enabled", nullable = false, columnDefinition = "bool default true")
    private boolean enabled = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "architecture_id")
    private Architecture architecture;

    @NotNull
    @Embedded
    @Valid
    private LogoFile logoFile;

    @Nullable
    @Embedded
    @Valid
    private DockerComposeFile dockerComposeFile;

    @NotNull
    @Embedded
    @Valid
    private CardImageFile cardImageFile;

    @Nullable
    @NoHtml
    @URL
    @Size(max = 512)
    @Column(name = "deployment_url")
    private String deploymentUrl;

    @NotBlank
    @NoHtml
    @URL
    @Size(max = 512)
    @Column(name = "backend_src_url",nullable = false)
    private String backendSrcUrl;

    @Nullable
    @NoHtml
    @URL
    @Size(max = 512)
    @Column(name = "frontend_src_url")
    private String frontendSrcUrl;

    @Nullable
    @NoHtml
    @URL
    @Size(max = 512)
    @Column(name = "open_api_url")
    private String openApiUrl;

    @NotEmpty
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_technology",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "technology_id")
    )
    @SortNatural
    private SortedSet<Technology> technologies = new TreeSet<>();

    @Valid
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @SortNatural
    private SortedSet<DescriptionElement> descriptionElements = new TreeSet<>();

    public Project(Long id, String name, String shortDescription, boolean enabled, Priority priority, LocalDate startDate,
                   LocalDate endDate, Architecture architecture, LogoFile logoFile, DockerComposeFile dockerComposeFile,
                   CardImageFile cardImageFile, String deploymentUrl, String backendSrcUrl, String frontendSrcUrl,
                   String openApiUrl) {
        super(id);
        this.name = name;
        this.shortDescription = shortDescription;
        this.enabled = enabled;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.architecture = architecture;
        this.logoFile = logoFile;
        this.dockerComposeFile = dockerComposeFile;
        this.cardImageFile = cardImageFile;
        this.deploymentUrl = deploymentUrl;
        this.backendSrcUrl = backendSrcUrl;
        this.frontendSrcUrl = frontendSrcUrl;
        this.openApiUrl = openApiUrl;
    }

    public Project(Long id, String name, String shortDescription, boolean enabled, Priority priority, LocalDate startDate,
                   LocalDate endDate, Architecture architecture, LogoFile logoFile, DockerComposeFile dockerComposeFile,
                   CardImageFile cardImageFile, String deploymentUrl, String backendSrcUrl, String frontendSrcUrl,
                   String openApiUrl, SortedSet<Technology> technologies) {
        this(id, name, shortDescription, enabled, priority, startDate, endDate, architecture, logoFile, dockerComposeFile,
                cardImageFile, deploymentUrl, backendSrcUrl, frontendSrcUrl, openApiUrl);
        this.technologies = technologies;
    }

    public void addTechnology(Technology technology) {
        technologies.add(technology);
    }

    public void addDescriptionElement(DescriptionElement element) {
        descriptionElements.add(element);
        element.setProject(this);
    }

    public void removeDescriptionElement(DescriptionElement element) {
        descriptionElements.remove(element);
        element.setProject(null);
    }

    @Override
    public String toString() {
        return String.format("Project[id=%d, name=%s]", id, name);
    }
}
