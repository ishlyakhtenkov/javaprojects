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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;
import static ru.javaprojects.projector.common.util.Util.createFile;
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
        return new ProjectTo(project.getId(), project.getName(), project.getShortDescription(), project.isEnabled(),
                project.getPriority(), project.getStartDate(), project.getEndDate(), project.getArchitecture(),
                project.getLogo().getFileName(), project.getLogo().getFileLink(), dockerComposeFileName, dockerComposeFileLink,
                project.getCardImage().getFileName(), project.getCardImage().getFileLink(), project.getDeploymentUrl(),
                project.getBackendSrcUrl(), project.getFrontendSrcUrl(), project.getOpenApiUrl(), technologiesIds, deTos);
    }

    public Project createNewFromTo(ProjectTo projectTo) {
        String name = projectTo.getName();
        File dockerCompose = (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) ?
                createFile(projectTo::getDockerCompose, projectFilesPath, name + DOCKER_DIR) : null;
        Project project = new Project(null, name, projectTo.getShortDescription(), projectTo.isEnabled(),
                projectTo.getPriority(), projectTo.getStartDate(), projectTo.getEndDate(), projectTo.getArchitecture(),
                createFile(projectTo::getLogo, projectFilesPath, name + LOGO_DIR), dockerCompose,
                createFile(projectTo::getCardImage, projectFilesPath, name + CARD_IMG_DIR), projectTo.getDeploymentUrl(),
                projectTo.getBackendSrcUrl(), projectTo.getFrontendSrcUrl(), projectTo.getOpenApiUrl(), 0);

        technologyService.getAllByIds(projectTo.getTechnologiesIds()).forEach(project::addTechnology);
        projectTo.getDescriptionElementTos().forEach(deTo -> project.addDescriptionElement(createNewFromTo(deTo, project)));
        return project;
    }

    private DescriptionElement createNewFromTo(DescriptionElementTo deTo, Project project) {
        if (deTo.getType() == ElementType.IMAGE) {
            if (deTo.getImage() == null || deTo.getImage().isEmpty()) {
                throw new IllegalRequestDataException("Description element image file is not present",
                        "description-element.image-not-present", null);
            }
            setImageFileAttributes(deTo, project.getName());
        }
        return new DescriptionElement(null, deTo.getType(), deTo.getIndex(), deTo.getText(),
                deTo.getImage() != null ? new File(deTo.getImage().getFileName(), deTo.getImage().getFileLink()) : null);
    }

    public Project updateFromTo(Project project, ProjectTo projectTo) {
        String projectOldName = project.getName();
        project.setName(projectTo.getName());
        project.setShortDescription(projectTo.getShortDescription());
        project.setEnabled(projectTo.isEnabled());
        project.setPriority(projectTo.getPriority());
        project.setStartDate(projectTo.getStartDate());
        project.setEndDate(projectTo.getEndDate());
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
            project.setLogo(createFile(projectTo::getLogo, projectFilesPath, projectTo.getName() + LOGO_DIR));
        }
        if (projectTo.getCardImage() != null && !projectTo.getCardImage().isEmpty()) {
            project.setCardImage(createFile(projectTo::getCardImage, projectFilesPath, projectTo.getName() + CARD_IMG_DIR));
        }
        if (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) {
            project.setDockerCompose(createFile(projectTo::getDockerCompose, projectFilesPath,
                    projectTo.getName() + DOCKER_DIR));
        }

        if (!project.getName().equalsIgnoreCase(projectOldName)) {
            project.getLogo().setFileLink(projectFilesPath + normalizePath(project.getName() + LOGO_DIR +
                    project.getLogo().getFileName()));
            project.getCardImage().setFileLink(projectFilesPath + normalizePath(project.getName() + CARD_IMG_DIR +
                    project.getCardImage().getFileName()));
            if (project.getDockerCompose() != null) {
                project.getDockerCompose().setFileLink(projectFilesPath + normalizePath(project.getName() + DOCKER_DIR +
                        project.getDockerCompose().getFileName()));
            }
            project.getDescriptionElements().stream()
                    .filter(de -> de.getType() == ElementType.IMAGE && !de.isNew())
                    .forEach(de -> {
                        String fileNameWithPrefix = de.getImage().getFileLink()
                                .substring(de.getImage().getFileLink().lastIndexOf('/') + 1);
                        String newFileLink =
                                projectFilesPath + normalizePath(project.getName() + DESCRIPTION_IMG_DIR + fileNameWithPrefix);
                        de.getImage().setFileLink(newFileLink);
                    });
        }
        return project;
    }

    private void updateFromTo(DescriptionElement de, DescriptionElementTo deTo, Project project) {
        de.setIndex(deTo.getIndex());
        if (deTo.getType() == ElementType.IMAGE && deTo.getImage() != null && !deTo.getImage().isEmpty()) {
            setImageFileAttributes(deTo, project.getName());
            de.getImage().setFileName(deTo.getImage().getFileName());
            de.getImage().setFileLink(deTo.getImage().getFileLink());
        } else {
            de.setText(deTo.getText());
        }
    }

    private void setImageFileAttributes(DescriptionElementTo deTo, String projectName) {
        String fileName = deTo.getImage().getRealFileName();
        String uniquePrefix = UUID.randomUUID().toString();
        String fileLink = projectFilesPath + normalizePath(projectName + DESCRIPTION_IMG_DIR + uniquePrefix + "_" +
                fileName);
        deTo.getImage().setFileName(fileName);
        deTo.getImage().setFileLink(fileLink);
    }
}
