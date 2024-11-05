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
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @NotBlank
    @NoHtml
    @Size(max = 64)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank
    @NoHtml
    @Size(max = 128)
    @Column(name = "annotation", nullable = false)
    private String annotation;

    @CreationTimestamp
    @Column(name = "created", nullable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime created;

    @Column(name = "visible", nullable = false, columnDefinition = "bool default true")
    private boolean visible = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "architecture_id")
    private Architecture architecture;

    @NotNull
    @Column(name = "started", nullable = false)
    private LocalDate started;

    @NotNull
    @Column(name = "finished", nullable = false)
    private LocalDate finished;

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
            @AttributeOverride(name = "fileName", column = @Column(name = "preview_file_name")),
            @AttributeOverride(name = "fileLink", column = @Column(name = "preview_file_link"))
    })
    private File preview;

    @Nullable
    @NoHtml
    @URL
    @Size(max = 512)
    @Column(name = "deployment_url")
    private String deploymentUrl;

    @Nullable
    @NoHtml
    @URL
    @Size(max = 512)
    @Column(name = "backend_src_url")
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

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "project_tag",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public Project(Long id, String name, String annotation, boolean visible, Priority priority, LocalDate started,
                   LocalDate finished, Architecture architecture, File logo, File dockerCompose, File preview,
                   String deploymentUrl, String backendSrcUrl, String frontendSrcUrl, String openApiUrl, int views,
                   User author) {
        super(id);
        this.name = name;
        this.annotation = annotation;
        this.visible = visible;
        this.priority = priority;
        this.started = started;
        this.finished = finished;
        this.architecture = architecture;
        this.logo = logo;
        this.dockerCompose = dockerCompose;
        this.preview = preview;
        this.deploymentUrl = deploymentUrl;
        this.backendSrcUrl = backendSrcUrl;
        this.frontendSrcUrl = frontendSrcUrl;
        this.openApiUrl = openApiUrl;
        this.views = views;
        this.author = author;
    }

    public Project(Long id, String name, String annotation, boolean visible, Priority priority,
                   LocalDate started, LocalDate finished, Architecture architecture, File logo,
                   File dockerCompose, File preview, String deploymentUrl, String backendSrcUrl, String frontendSrcUrl,
                   String openApiUrl, int views, User author, Set<Technology> technologies) {
        this(id, name, annotation, visible, priority, started, finished, architecture, logo, dockerCompose,
                preview, deploymentUrl, backendSrcUrl, frontendSrcUrl, openApiUrl, views, author);
        this.technologies = technologies;
    }

    public Project(Long id, String name, String annotation, boolean visible, Priority priority,
                   LocalDate started, LocalDate finished, Architecture architecture, File logo,
                   File dockerCompose, File preview, String deploymentUrl, String backendSrcUrl, String frontendSrcUrl,
                   String openApiUrl, int views, User author, Set<Technology> technologies,
                   Set<DescriptionElement> descriptionElements, Set<Like> likes, List<Comment> comments, Set<Tag> tags) {
        this(id, name, annotation, visible, priority, started, finished, architecture, logo, dockerCompose,
                preview, deploymentUrl, backendSrcUrl, frontendSrcUrl, openApiUrl, views, author, technologies);
        this.descriptionElements = descriptionElements;
        this.likes = likes;
        this.comments = comments;
        this.tags = tags;
    }

    public void addTechnology(Technology technology) {
        technologies.add(technology);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
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
