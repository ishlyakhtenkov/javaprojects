package ru.javaprojects.projector.projects.to;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.HasIdAndName;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.projects.HasArchitecture;
import ru.javaprojects.projector.reference.architectures.Architecture;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.users.model.User;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ProjectPreviewTo extends BaseTo implements HasIdAndName, HasArchitecture {
    private User author;
    private String name;
    private String annotation;
    private LocalDateTime created;
    private boolean visible;
    private Architecture architecture;
    private File preview;
    private Set<Technology> technologies;
    private int views;
    private Set<Long> likesUserIds;
    private int commentsCount;

    public ProjectPreviewTo(Long id, User author, String name, String annotation, LocalDateTime created, boolean visible,
                            Architecture architecture, File preview, Set<Technology> technologies, int views,
                            Set<Long> likesUserIds, int commentsCount) {
        super(id);
        this.author = author;
        this.name = name;
        this.annotation = annotation;
        this.created = created;
        this.visible = visible;
        this.architecture = architecture;
        this.preview = preview;
        this.technologies = technologies;
        this.views = views;
        this.likesUserIds = likesUserIds;
        this.commentsCount = commentsCount;
    }
}
