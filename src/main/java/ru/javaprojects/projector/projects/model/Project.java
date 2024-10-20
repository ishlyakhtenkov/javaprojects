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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.URL;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.validation.NoHtml;
import ru.javaprojects.projector.reference.architectures.Architecture;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.users.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"},
        name = "projects_unique_author_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Project extends BaseEntity implements HasIdAndName {
    @NotBlank
    @NoHtml
    @Size(max = 64)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank
    @NoHtml
    @Size(max = 128)
    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Column(name = "enabled", nullable = false, columnDefinition = "bool default true")
    private boolean enabled = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @CreationTimestamp
    @Column(name = "created", nullable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime created;

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
    @AttributeOverrides({
            @AttributeOverride(name = "fileName", column = @Column(name = "logo_file_name")),
            @AttributeOverride(name = "fileLink", column = @Column(name = "logo_file_link"))
    })
    private File logo;

    @Nullable
    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "fileName", column = @Column(name = "docker_compose_file_name")),
            @AttributeOverride(name = "fileLink", column = @Column(name = "docker_compose_file_link"))
    })
    private File dockerCompose;

    @NotNull
    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "fileName", column = @Column(name = "card_image_file_name")),
            @AttributeOverride(name = "fileLink", column = @Column(name = "card_image_file_link"))
    })
    private File cardImage;

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
    @Column(name = "backend_src_url", nullable = false)
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
    private Set<Technology> technologies = new HashSet<>();

    @Valid
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private Set<DescriptionElement> descriptionElements = new HashSet<>();

    @Column(name = "views", nullable = false, columnDefinition = "integer default 0")
    private int views;

    @OneToMany(mappedBy = "objectId",  fetch = FetchType.LAZY)
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "projectId",  fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    public Project(Long id, String name, String shortDescription, boolean enabled, Priority priority, LocalDate startDate,
                   LocalDate endDate, Architecture architecture, File logo, File dockerCompose, File cardImage,
                   String deploymentUrl, String backendSrcUrl, String frontendSrcUrl, String openApiUrl, int views,
                   User author) {
        super(id);
        this.name = name;
        this.shortDescription = shortDescription;
        this.enabled = enabled;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.architecture = architecture;
        this.logo = logo;
        this.dockerCompose = dockerCompose;
        this.cardImage = cardImage;
        this.deploymentUrl = deploymentUrl;
        this.backendSrcUrl = backendSrcUrl;
        this.frontendSrcUrl = frontendSrcUrl;
        this.openApiUrl = openApiUrl;
        this.views = views;
        this.author = author;
    }

    public Project(Long id, String name, String shortDescription, boolean enabled, Priority priority,
                   LocalDate startDate, LocalDate endDate, Architecture architecture, File logo,
                   File dockerCompose, File cardImage, String deploymentUrl, String backendSrcUrl, String frontendSrcUrl,
                   String openApiUrl, int views, User author, Set<Technology> technologies) {
        this(id, name, shortDescription, enabled, priority, startDate, endDate, architecture, logo, dockerCompose,
                cardImage, deploymentUrl, backendSrcUrl, frontendSrcUrl, openApiUrl, views, author);
        this.technologies = technologies;
    }

    public Project(Long id, String name, String shortDescription, boolean enabled, Priority priority,
                   LocalDate startDate, LocalDate endDate, Architecture architecture, File logo,
                   File dockerCompose, File cardImage, String deploymentUrl, String backendSrcUrl, String frontendSrcUrl,
                   String openApiUrl, int views, User author, Set<Technology> technologies,
                   Set<DescriptionElement> descriptionElements, Set<Like> likes, List<Comment> comments) {
        this(id, name, shortDescription, enabled, priority, startDate, endDate, architecture, logo, dockerCompose,
                cardImage, deploymentUrl, backendSrcUrl, frontendSrcUrl, openApiUrl, views, author, technologies);
        this.descriptionElements = descriptionElements;
        this.likes = likes;
        this.comments = comments;
    }

    public void addTechnology(Technology technology) {
        technologies.add(technology);
    }

    public void addDescriptionElement(DescriptionElement element) {
        descriptionElements.add(element);
        element.setProject(this);
    }

    @Override
    public String toString() {
        return String.format("Project[id=%d, name=%s]", id, name);
    }
}
