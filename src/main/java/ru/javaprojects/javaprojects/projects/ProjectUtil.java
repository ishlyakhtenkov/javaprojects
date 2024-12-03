package ru.javaprojects.javaprojects.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.javaprojects.javaprojects.common.HasId;
import ru.javaprojects.javaprojects.common.error.IllegalRequestDataException;
import ru.javaprojects.javaprojects.common.model.BaseEntity;
import ru.javaprojects.javaprojects.common.model.File;
import ru.javaprojects.javaprojects.common.to.BaseTo;
import ru.javaprojects.javaprojects.common.to.FileTo;
import ru.javaprojects.javaprojects.projects.model.*;
import ru.javaprojects.javaprojects.projects.repository.TagRepository;
import ru.javaprojects.javaprojects.projects.to.DescriptionElementTo;
import ru.javaprojects.javaprojects.projects.to.ProjectPreviewTo;
import ru.javaprojects.javaprojects.projects.to.ProjectTo;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyService;
import ru.javaprojects.javaprojects.reference.technologies.model.Technology;
import ru.javaprojects.javaprojects.users.model.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.javaprojects.javaprojects.common.util.AppUtil.createFile;
import static ru.javaprojects.javaprojects.common.util.FileUtil.normalizePath;
import static ru.javaprojects.javaprojects.projects.ProjectService.*;

@Component
@RequiredArgsConstructor
public class ProjectUtil {
    private final TechnologyService technologyService;
    private final TagRepository tagRepository;

    @Value("${content-path.projects}")
    private String projectFilesPath;

    public ProjectTo asTo(Project project) {
        List<DescriptionElementTo> deTos = project.getDescriptionElements().stream()
                .map(de -> {
                    String fileName = de.getImage() != null ? de.getImage().getFileName() : null;
                    String fileLink = de.getImage() != null ? de.getImage().getFileLink() : null;
                    FileTo image = (fileName == null || fileLink == null) ? null :
                            new FileTo(fileName, fileLink, null, null);
                    return new DescriptionElementTo(de.getId(), de.getType(), de.getIndex(), de.getText(), image);
                })
                .toList();

        Set<Long> technologiesIds = project.getTechnologies().stream()
                .map(BaseEntity::getId)
                .collect(Collectors.toSet());

        FileTo logo = new FileTo(project.getLogo().getFileName(), project.getLogo().getFileLink(), null, null);
        String dockerComposeFileName = project.getDockerCompose() != null ? project.getDockerCompose().getFileName() : null;
        String dockerComposeFileLink = project.getDockerCompose() != null ? project.getDockerCompose().getFileLink() : null;
        FileTo dockerCompose = (dockerComposeFileName == null || dockerComposeFileLink == null) ? null :
                new FileTo(dockerComposeFileName, dockerComposeFileLink, null, null);
        FileTo preview = new FileTo(project.getPreview().getFileName(), project.getPreview().getFileLink(), null, null);


        String tags = project.getTags().isEmpty() ? null : project.getTags().stream()
                .map(Tag::getName).collect(Collectors.joining(" "));
        return new ProjectTo(project.getId(), project.getName(), project.getAnnotation(), project.isVisible(),
                project.getPriority(), project.getStarted(), project.getFinished(), project.getArchitecture(),
                logo, dockerCompose, preview, project.getDeploymentUrl(), project.getBackendSrcUrl(),
                project.getFrontendSrcUrl(), project.getOpenApiUrl(), technologiesIds, deTos, tags);
    }

    public ProjectPreviewTo asPreviewTo(Project project, int commentsCount) {
        Set<Long> likesUserIds = project.getLikes().stream()
                .map(Like::getUserId)
                .collect(Collectors.toSet());
        return new ProjectPreviewTo(project.getId(), project.getAuthor(), project.getName(), project.getAnnotation(),
                project.getCreated(), project.isVisible(), project.getArchitecture(), project.getPreview(),
                project.getTechnologies(), project.getViews(), likesUserIds, commentsCount);
    }

    public List<ProjectPreviewTo> asPreviewTos(List<Project> projects, Map<Long, Integer> commentsCount) {
        return projects.stream()
                .map(project -> asPreviewTo(project, commentsCount.get(project.getId())))
                .toList();
    }

    public Project createNewFromTo(ProjectTo projectTo, User author) {
        String name = projectTo.getName();
        File dockerCompose = (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) ?
                createFile(projectTo::getDockerCompose, projectFilesPath, author.getEmail() + "/" + name + DOCKER_DIR) : null;
        Project project = new Project(null, name, projectTo.getAnnotation(), projectTo.isVisible(),
                projectTo.getPriority(), projectTo.getStarted(), projectTo.getFinished(), projectTo.getArchitecture(),
                createFile(projectTo::getLogo, projectFilesPath, author.getEmail() + "/"  + name + LOGO_DIR), dockerCompose,
                createFile(projectTo::getPreview, projectFilesPath, author.getEmail() + "/"  + name + PREVIEW_DIR),
                projectTo.getDeploymentUrl(),
                projectTo.getBackendSrcUrl(), projectTo.getFrontendSrcUrl(), projectTo.getOpenApiUrl(), 0, author);

        technologyService.getAllByIds(projectTo.getTechnologiesIds()).forEach(project::addTechnology);
        projectTo.getDescriptionElementTos().forEach(deTo -> project.addDescriptionElement(createNewDeFromTo(deTo, project)));
        if (projectTo.getTags() != null && !projectTo.getTags().isBlank()) {
            parseTags(projectTo.getTags()).forEach(project::addTag);
        }
        return project;
    }

    public Project updateFromTo(Project project, ProjectTo projectTo) {
        String projectOldName = project.getName();
        String authorEmail = project.getAuthor().getEmail();
        project.setName(projectTo.getName());
        project.setAnnotation(projectTo.getAnnotation());
        project.setVisible(projectTo.isVisible());
        project.setPriority(projectTo.getPriority());
        project.setStarted(projectTo.getStarted());
        project.setFinished(projectTo.getFinished());
        project.setArchitecture(projectTo.getArchitecture());
        project.setDeploymentUrl(projectTo.getDeploymentUrl());
        project.setBackendSrcUrl(projectTo.getBackendSrcUrl());
        project.setFrontendSrcUrl(projectTo.getFrontendSrcUrl());
        project.setOpenApiUrl(projectTo.getOpenApiUrl());

        Set<Technology> technologies = technologyService.getAllByIds(projectTo.getTechnologiesIds());
        technologies.stream()
                .filter(technology -> !project.getTechnologies().contains(technology))
                .forEach(project::addTechnology);
        project.getTechnologies().removeIf(technology -> !technologies.contains(technology));

        Map<Long, DescriptionElementTo> notNewDeTos = projectTo.getDescriptionElementTos().stream()
                .filter(de -> !de.isNew())
                .collect(Collectors.toMap(BaseTo::getId, Function.identity()));
        project.getDescriptionElements().removeIf(de -> !notNewDeTos.containsKey(de.getId()));
        project.getDescriptionElements().forEach(de -> {
            DescriptionElementTo deTo = notNewDeTos.get(de.getId());
            updateDeFromTo(de, deTo, project);
        });
        projectTo.getDescriptionElementTos().stream()
                .filter(HasId::isNew)
                .map(deTo -> createNewDeFromTo(deTo, project))
                .forEach(project::addDescriptionElement);

        if (projectTo.getTags() == null || projectTo.getTags().isBlank()) {
            project.getTags().clear();
        } else {
            Set<Tag> projectToTags = parseTags(projectTo.getTags());
            project.getTags().removeIf(tag -> !projectToTags.contains(tag));
            projectToTags.stream()
                    .filter(tag -> !project.getTags().contains(tag))
                    .forEach(project::addTag);
        }

        if (projectTo.getLogo() != null && !projectTo.getLogo().isEmpty()) {
            project.setLogo(createFile(projectTo::getLogo, projectFilesPath, authorEmail + "/"  + projectTo.getName() +
                    LOGO_DIR));
        }
        if (projectTo.getPreview() != null && !projectTo.getPreview().isEmpty()) {
            project.setPreview(createFile(projectTo::getPreview, projectFilesPath, authorEmail + "/"  +
                    projectTo.getName() + PREVIEW_DIR));
        }
        if (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) {
            project.setDockerCompose(createFile(projectTo::getDockerCompose, projectFilesPath,
                    authorEmail + "/"  + projectTo.getName() + DOCKER_DIR));
        }

        if (!project.getName().equalsIgnoreCase(projectOldName)) {
            project.getLogo().setFileLink(projectFilesPath + normalizePath(authorEmail + "/" + project.getName() +
                    LOGO_DIR + project.getLogo().getFileName()));
            project.getPreview().setFileLink(projectFilesPath + normalizePath(authorEmail + "/" + project.getName() +
                    PREVIEW_DIR + project.getPreview().getFileName()));
            if (project.getDockerCompose() != null) {
                project.getDockerCompose().setFileLink(projectFilesPath + normalizePath(authorEmail + "/" +
                        project.getName() + DOCKER_DIR + project.getDockerCompose().getFileName()));
            }
            project.getDescriptionElements().stream()
                    .filter(de -> de.getType() == ElementType.IMAGE && !de.isNew() && de.getImage() != null)
                    .forEach(de -> {
                        String fileNameWithPrefix = de.getImage().getFileLink()
                                .substring(de.getImage().getFileLink().lastIndexOf('/') + 1);
                        String newFileLink = projectFilesPath + normalizePath(authorEmail + "/" + project.getName() +
                                DESCRIPTION_IMG_DIR + fileNameWithPrefix);
                        de.getImage().setFileLink(newFileLink);
                    });
        }
        return project;
    }

    private Set<Tag> parseTags(String tagsString) {
        tagsString = tagsString.toLowerCase();
        Set<String> tagNames;
        if (tagsString.contains(",")) {
            tagNames = Stream.of(tagsString.split(",")).map(this::normalizeTagName).collect(Collectors.toSet());
        } else {
            tagNames = Stream.of(tagsString.split(" ")).map(this::normalizeTagName).collect(Collectors.toSet());
        }
        Set<Tag> dbTags = tagRepository.findAllByNameIn(tagNames);
        Set<String> dbTagsNames = dbTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
        List<Tag> newTags = tagNames.stream()
                .filter(tag -> !dbTagsNames.contains(tag))
                .map(tag -> new Tag(null, tag))
                .toList();
        Set<Tag> tags = new HashSet<>();
        tags.addAll(dbTags);
        tags.addAll(newTags);
        return tags;
    }

    private String normalizeTagName(String tagName) {
        tagName = tagName.trim();
        if (tagName.startsWith("#")) {
            tagName = tagName.substring(1);
        }
        return tagName;
    }

    private DescriptionElement createNewDeFromTo(DescriptionElementTo deTo, Project project) {
        if (deTo.getType() == ElementType.IMAGE) {
            if (deTo.getImage() == null || deTo.getImage().isEmpty()) {
                throw new IllegalRequestDataException("Description element image file is not present",
                        "project.description-elements.image-not-present", null);
            }
            setImageFileAttributes(deTo, project.getName(), project.getAuthor().getEmail());
        }
        return new DescriptionElement(null, deTo.getType(), deTo.getIndex(), deTo.getText(),
                deTo.getImage() != null ? new File(deTo.getImage().getFileName(), deTo.getImage().getFileLink()) : null);
    }

    private void updateDeFromTo(DescriptionElement de, DescriptionElementTo deTo, Project project) {
        de.setIndex(deTo.getIndex());
        if (deTo.getType() == ElementType.IMAGE && deTo.getImage() != null && !deTo.getImage().isEmpty()) {
            setImageFileAttributes(deTo, project.getName(), project.getAuthor().getEmail());
            de.getImage().setFileName(deTo.getImage().getFileName());
            de.getImage().setFileLink(deTo.getImage().getFileLink());
        } else {
            de.setText(deTo.getText());
        }
    }

    private void setImageFileAttributes(DescriptionElementTo deTo, String projectName, String authorEmail) {
        String fileName = deTo.getImage().getRealFileName();
        String uniquePrefix = UUID.randomUUID().toString();
        String fileLink = projectFilesPath + normalizePath(authorEmail + "/" + projectName + DESCRIPTION_IMG_DIR +
                uniquePrefix + "_" + fileName);
        deTo.getImage().setFileName(fileName);
        deTo.getImage().setFileLink(fileLink);
    }
}
