package ru.javaprojects.projector.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.common.BaseTo;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.LogoFile;
import ru.javaprojects.projector.projects.model.*;
import ru.javaprojects.projector.references.technologies.TechnologyService;
import ru.javaprojects.projector.references.technologies.model.Technology;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.javaprojects.projector.common.util.FileUtil.*;
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
        projectTo.getDescriptionElementTos().forEach(deTo -> project.addDescriptionElement(createNewFromTo(deTo, project)));
        return project;
    }

    private DescriptionElement createNewFromTo(DescriptionElementTo deTo, Project project) {
        if (deTo.getType() == ElementType.IMAGE) {
            if (isMultipartFileEmpty(deTo.getImageFile()) && !hasImageFileString(deTo)) {
                throw new IllegalRequestDataException("Description element image file is not present",
                        "description-element.image-not-present", null);
            }
            String fileName;
            if (!isMultipartFileEmpty(deTo.getImageFile())) {
                fileName = deTo.getImageFile().getOriginalFilename();
            } else {
                fileName = deTo.getFileName();
            }
            String uniquePrefix = UUID.randomUUID().toString();
            String fileLink = contentPath + normalizePath(project.getName() + DESCRIPTION_IMG_DIR + uniquePrefix + "_" +
                    fileName);
            deTo.setFileName(fileName);
            deTo.setFileLink(fileLink);
        }
        return new DescriptionElement(null, deTo.getType(), deTo.getIndex(), deTo.getText(), deTo.getFileName(),
                deTo.getFileLink());
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

        if (!isMultipartFileEmpty(projectTo.getLogoFile())) {
            project.setLogoFile(createLogoFile(projectTo));
        }
        if (!isMultipartFileEmpty(projectTo.getCardImageFile())) {
            project.setCardImageFile(createCardImageFile(projectTo));
        }
        if (!isMultipartFileEmpty(projectTo.getDockerComposeFile())) {
            project.setDockerComposeFile(createDockerComposeFile(projectTo));
        }

        if (!project.getName().equalsIgnoreCase(projectOldName)) {
            project.getLogoFile().setFileLink(contentPath + normalizePath(project.getName()) + LOGO_DIR +
                    project.getLogoFile().getFileName());
            project.getCardImageFile().setFileLink(contentPath + normalizePath(project.getName()) + CARD_IMG_DIR +
                    project.getCardImageFile().getFileName());
            if (project.getDockerComposeFile() != null) {
                project.getDockerComposeFile().setFileLink(contentPath + normalizePath(project.getName()) + DOCKER_DIR +
                        project.getDockerComposeFile().getFileName());
            }
            project.getDescriptionElements().stream()
                    .filter(de -> de.getType() == ElementType.IMAGE && !de.isNew())
                    .forEach(de -> {
                        String fileNameWithPrefix = de.getFileLink().substring(de.getFileLink().lastIndexOf('/') + 1);
                        String newFileLink =
                                contentPath + normalizePath(project.getName() + DESCRIPTION_IMG_DIR + fileNameWithPrefix);
                        de.setFileLink(newFileLink);
                    });
        }
        return project;
    }

    private void updateFromTo(DescriptionElement de, DescriptionElementTo deTo, Project project) {
        de.setIndex(deTo.getIndex());
        if (deTo.getType() == ElementType.IMAGE) {
            String fileName = null;
            if (!isMultipartFileEmpty(deTo.getImageFile())) {
                fileName = deTo.getImageFile().getOriginalFilename();
            } else if (hasImageFileString(deTo)) {
                fileName = deTo.getFileName();
            }
            if (fileName != null) {
                String uniquePrefix = UUID.randomUUID().toString();
                String fileLink = contentPath + normalizePath(project.getName() + DESCRIPTION_IMG_DIR + uniquePrefix + "_" +
                        fileName);
                deTo.setFileName(fileName);
                deTo.setFileLink(fileLink);
                de.setFileName(fileName);
                de.setFileLink(fileLink);
            }
        } else {
            de.setText(deTo.getText());
        }
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
        if (isMultipartFileEmpty(projectTo.getDockerComposeFile())) {
            return null;
        }
        String filename = normalizePath(projectTo.getDockerComposeFile().getOriginalFilename());
        return new DockerComposeFile(filename, contentPath + normalizePath(projectTo.getName()) + DOCKER_DIR + filename);
    }
}
