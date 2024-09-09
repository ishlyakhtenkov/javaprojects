package ru.javaprojects.projector.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.LogoFile;
import ru.javaprojects.projector.projects.model.*;
import ru.javaprojects.projector.references.technologies.TechnologyService;
import ru.javaprojects.projector.references.technologies.model.Technology;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;
import static ru.javaprojects.projector.projects.ProjectService.*;

@Component
@RequiredArgsConstructor
public class ProjectUtil {
    private final TechnologyService technologyService;

    @Value("${content-path.projects}")
    private String contentPath;

    public ProjectTo asTo(Project project) {
        List<DescriptionElementTo> descriptionElementTos = project.getDescriptionElements().stream()
                .map(de -> new DescriptionElementTo(de.getId(), de.getType(), de.getIndex(), de.getText(),
                        de.getFileName(), de.getFileLink()))
                .toList();

        return new ProjectTo(project.getId(), project.getName(), project.getShortDescription(), project.isEnabled(),
                project.getPriority(), project.getStartDate(), project.getEndDate(), project.getArchitecture(),
                project.getDeploymentUrl(), project.getBackendSrcUrl(), project.getFrontendSrcUrl(), project.getOpenApiUrl(),
                project.getTechnologies().stream().map(BaseEntity::getId).collect(Collectors.toSet()), descriptionElementTos);
    }

    public Project createNewFromTo(ProjectTo projectTo) {
        Project project = new Project(null, projectTo.getName(), projectTo.getShortDescription(), projectTo.isEnabled(),
                projectTo.getPriority(), projectTo.getStartDate(), projectTo.getEndDate(), projectTo.getArchitecture(),
                createLogoFile(projectTo), createDockerComposeFile(projectTo),
                createCardImageFile(projectTo), projectTo.getDeploymentUrl(), projectTo.getBackendSrcUrl(),
                projectTo.getFrontendSrcUrl(), projectTo.getOpenApiUrl());
        technologyService.getAllByIds(projectTo.getTechnologiesIds()).forEach(project::addTechnology);
        projectTo.getDescriptionElementTos().forEach(deTo -> createNewFromTo(deTo, project));
        return project;
    }

    private DescriptionElement createNewFromTo(DescriptionElementTo deTo, Project project) {
        if (deTo.getType() == ElementType.IMAGE) {
            if ((deTo.getImageFile() == null || deTo.getImageFile().isEmpty()) &&
                    (deTo.getImageFileString() == null || deTo.getImageFileString().isEmpty())) {
                throw new IllegalRequestDataException("Description element image file is not present",
                        "description-element.image-not-present", null);
            }
            String fileName;
            if (deTo.getImageFile() != null && !deTo.getImageFile().isEmpty()) {
                fileName = deTo.getImageFile().getOriginalFilename();
            } else if (deTo.getFileName() != null) {
                fileName = deTo.getFileName();
            } else {
                throw new IllegalRequestDataException("Description element image file name is not present",
                        "description-element.image-name-not-present", null);
            }
            String uniquePrefix = UUID.randomUUID().toString();
            String fileLink = contentPath + normalizePath(project.getName() + DESCRIPTION_IMG_DIR + uniquePrefix + "_" +
                    fileName);
            deTo.setFileName(fileName);
            deTo.setFileLink(fileLink);
        }
        DescriptionElement descriptionElement = new DescriptionElement(null, deTo.getType(),
                deTo.getIndex(), deTo.getText(), deTo.getFileName(), deTo.getFileLink());
        project.addDescriptionElement(descriptionElement);
        return descriptionElement;
    }

    public Project updateFromTo(Project project, ProjectTo projectTo) {
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

        if (projectTo.getLogoFile() != null && !projectTo.getLogoFile().isEmpty()) {
            project.setLogoFile(createLogoFile(projectTo));
        } else if (!projectTo.getName().equalsIgnoreCase(project.getName())) {
            project.getLogoFile().setFileLink(contentPath + normalizePath(projectTo.getName()) + LOGO_DIR +
                    project.getLogoFile().getFileName());
        }
        if (projectTo.getCardImageFile() != null && !projectTo.getCardImageFile().isEmpty()) {
            project.setCardImageFile(createCardImageFile(projectTo));
        } else if (!projectTo.getName().equalsIgnoreCase(project.getName())) {
            project.getCardImageFile().setFileLink(contentPath + normalizePath(projectTo.getName()) + CARD_IMG_DIR +
                    project.getCardImageFile().getFileName());
        }
        if (projectTo.getDockerComposeFile() != null && !projectTo.getDockerComposeFile().isEmpty()) {
            project.setDockerComposeFile(createDockerComposeFile(projectTo));
        } else if (!projectTo.getName().equalsIgnoreCase(project.getName()) && project.getDockerComposeFile() != null) {
            project.getDockerComposeFile().setFileLink(contentPath + normalizePath(projectTo.getName()) + DOCKER_DIR +
                    project.getDockerComposeFile().getFileName());
        }
        project.setName(projectTo.getName());
        return project;
    }


    private LogoFile createLogoFile(ProjectTo projectTo) {
        String filename = normalizePath(projectTo.getLogoFile().getOriginalFilename());
        return new LogoFile(filename, contentPath + normalizePath(projectTo.getName()) + LOGO_DIR + filename);
    }

    private CardImageFile createCardImageFile(ProjectTo projectTo) {
        String filename = normalizePath(projectTo.getCardImageFile().getOriginalFilename());
        return new CardImageFile(filename, contentPath + normalizePath(projectTo.getName()) + CARD_IMG_DIR + filename);
    }

    private DockerComposeFile createDockerComposeFile(ProjectTo projectTo) {
        if (projectTo.getDockerComposeFile() == null || projectTo.getDockerComposeFile().isEmpty()) {
            return null;
        }
        String filename = normalizePath(projectTo.getDockerComposeFile().getOriginalFilename());
        return new DockerComposeFile(filename, contentPath + normalizePath(projectTo.getName()) + DOCKER_DIR + filename);
    }
}
