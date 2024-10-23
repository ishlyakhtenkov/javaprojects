package ru.javaprojects.projector.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.projects.model.DescriptionElement;
import ru.javaprojects.projector.projects.model.ElementType;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.to.DescriptionElementTo;
import ru.javaprojects.projector.projects.to.ProjectTo;
import ru.javaprojects.projector.reference.technologies.TechnologyService;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.users.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;
import static ru.javaprojects.projector.common.util.AppUtil.createFile;
import static ru.javaprojects.projector.projects.ProjectService.*;

@Component
@RequiredArgsConstructor
public class ProjectUtil {
    private final TechnologyService technologyService;

    @Value("${content-path.projects}")
    private String projectFilesPath;

    public ProjectTo asTo(Project project) {
        List<DescriptionElementTo> deTos = project.getDescriptionElements().stream()
                .map(de -> {
                    String fileName = de.getImage() != null ? de.getImage().getFileName() : null;
                    String fileLink = de.getImage() != null ? de.getImage().getFileLink() : null;
                    return new DescriptionElementTo(de.getId(), de.getType(), de.getIndex(), de.getText(), fileName, fileLink);
                })
                .toList();

        Set<Long> technologiesIds = project.getTechnologies().stream()
                .map(BaseEntity::getId)
                .collect(Collectors.toSet());

        String dockerComposeFileName = project.getDockerCompose() != null ? project.getDockerCompose().getFileName() : null;
        String dockerComposeFileLink = project.getDockerCompose() != null ? project.getDockerCompose().getFileLink() : null;
        return new ProjectTo(project.getId(), project.getName(), project.getAnnotation(), project.isVisible(),
                project.getPriority(), project.getStarted(), project.getFinished(), project.getArchitecture(),
                project.getLogo().getFileName(), project.getLogo().getFileLink(), dockerComposeFileName, dockerComposeFileLink,
                project.getPreview().getFileName(), project.getPreview().getFileLink(), project.getDeploymentUrl(),
                project.getBackendSrcUrl(), project.getFrontendSrcUrl(), project.getOpenApiUrl(), technologiesIds, deTos);
    }

    public Project createNewFromTo(ProjectTo projectTo, User author) {
        String name = projectTo.getName();
        File dockerCompose = (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) ?
                createFile(projectTo::getDockerCompose, projectFilesPath, author.getEmail() + "/" + name + DOCKER_DIR) : null;
        Project project = new Project(null, name, projectTo.getAnnotation(), projectTo.isVisible(),
                projectTo.getPriority(), projectTo.getStarted(), projectTo.getFinished(), projectTo.getArchitecture(),
                createFile(projectTo::getLogo, projectFilesPath, author.getEmail() + "/"  + name + LOGO_DIR), dockerCompose,
                createFile(projectTo::getPreview, projectFilesPath, author.getEmail() + "/"  + name + PREVIEW_DIR), projectTo.getDeploymentUrl(),
                projectTo.getBackendSrcUrl(), projectTo.getFrontendSrcUrl(), projectTo.getOpenApiUrl(), 0, author);

        technologyService.getAllByIds(projectTo.getTechnologiesIds()).forEach(project::addTechnology);
        projectTo.getDescriptionElementTos().forEach(deTo -> project.addDescriptionElement(createNewFromTo(deTo, project)));
        return project;
    }

    private DescriptionElement createNewFromTo(DescriptionElementTo deTo, Project project) {
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
            updateFromTo(de, deTo, project);
        });
        projectTo.getDescriptionElementTos().stream()
                .filter(HasId::isNew)
                .map(deTo -> createNewFromTo(deTo, project))
                .forEach(project::addDescriptionElement);

        if (projectTo.getLogo() != null && !projectTo.getLogo().isEmpty()) {
            project.setLogo(createFile(projectTo::getLogo, projectFilesPath, authorEmail + "/"  + projectTo.getName() + LOGO_DIR));
        }
        if (projectTo.getPreview() != null && !projectTo.getPreview().isEmpty()) {
            project.setPreview(createFile(projectTo::getPreview, projectFilesPath, authorEmail + "/"  + projectTo.getName() + PREVIEW_DIR));
        }
        if (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) {
            project.setDockerCompose(createFile(projectTo::getDockerCompose, projectFilesPath,
                    authorEmail + "/"  + projectTo.getName() + DOCKER_DIR));
        }

        if (!project.getName().equalsIgnoreCase(projectOldName)) {
            project.getLogo().setFileLink(projectFilesPath + normalizePath(authorEmail + "/" + project.getName() + LOGO_DIR +
                    project.getLogo().getFileName()));
            project.getPreview().setFileLink(projectFilesPath + normalizePath(authorEmail + "/" + project.getName() + PREVIEW_DIR +
                    project.getPreview().getFileName()));
            if (project.getDockerCompose() != null) {
                project.getDockerCompose().setFileLink(projectFilesPath + normalizePath(authorEmail + "/" + project.getName() + DOCKER_DIR +
                        project.getDockerCompose().getFileName()));
            }
            project.getDescriptionElements().stream()
                    .filter(de -> de.getType() == ElementType.IMAGE && !de.isNew() && de.getImage() != null)
                    .forEach(de -> {
                        String fileNameWithPrefix = de.getImage().getFileLink()
                                .substring(de.getImage().getFileLink().lastIndexOf('/') + 1);
                        String newFileLink =
                                projectFilesPath + normalizePath(authorEmail + "/" + project.getName() + DESCRIPTION_IMG_DIR + fileNameWithPrefix);
                        de.getImage().setFileLink(newFileLink);
                    });
        }
        return project;
    }

    private void updateFromTo(DescriptionElement de, DescriptionElementTo deTo, Project project) {
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
        String fileLink = projectFilesPath + normalizePath(authorEmail + "/" + projectName + DESCRIPTION_IMG_DIR + uniquePrefix + "_" +
                fileName);
        deTo.getImage().setFileName(fileName);
        deTo.getImage().setFileLink(fileLink);
    }
}
