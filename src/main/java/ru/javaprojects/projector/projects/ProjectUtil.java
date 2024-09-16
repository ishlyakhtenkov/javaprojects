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
            if (deTo.getImage() == null || isFileToEmpty(deTo.getImage())) {
                throw new IllegalRequestDataException("Description element image file is not present",
                        "description-element.image-not-present", null);
            }
            setFileNameAndLink(deTo, project.getName());
        }
        return new DescriptionElement(null, deTo.getType(), deTo.getIndex(), deTo.getText(),
                deTo.getImage() != null ? deTo.getImage().getFileName() : null,
                deTo.getImage() != null ? deTo.getImage().getFileLink() : null);
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
            project.setLogo(createLogoFile(projectTo));
        }
        if (!isMultipartFileEmpty(projectTo.getCardImageFile())) {
            project.setCardImage(createCardImageFile(projectTo));
        }
        if (!isMultipartFileEmpty(projectTo.getDockerComposeFile())) {
            project.setDockerCompose(createDockerComposeFile(projectTo));
        }

        if (!project.getName().equalsIgnoreCase(projectOldName)) {
            project.getLogo().setFileLink(contentPath + normalizePath(project.getName()) + LOGO_DIR +
                    project.getLogo().getFileName());
            project.getCardImage().setFileLink(contentPath + normalizePath(project.getName()) + CARD_IMG_DIR +
                    project.getCardImage().getFileName());
            if (project.getDockerCompose() != null) {
                project.getDockerCompose().setFileLink(contentPath + normalizePath(project.getName()) + DOCKER_DIR +
                        project.getDockerCompose().getFileName());
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
        if (deTo.getType() == ElementType.IMAGE && deTo.getImage() != null && !isFileToEmpty(deTo.getImage())) {
            setFileNameAndLink(deTo, project.getName());
            de.setFileName(deTo.getImage().getFileName());
            de.setFileLink(deTo.getImage().getFileLink());
        } else {
            de.setText(deTo.getText());
        }
    }

    private void setFileNameAndLink(DescriptionElementTo deTo, String projectName) {
        String fileName = !isMultipartFileEmpty(deTo.getImage().getInputtedFile()) ?
                deTo.getImage().getInputtedFile().getOriginalFilename() : deTo.getImage().getFileName();
        String uniquePrefix = UUID.randomUUID().toString();
        String fileLink = contentPath + normalizePath(projectName + DESCRIPTION_IMG_DIR + uniquePrefix + "_" +
                fileName);
        deTo.getImage().setFileName(fileName);
        deTo.getImage().setFileLink(fileLink);
    }

    private File createLogoFile(ProjectTo projectTo) {
        String filename = normalizePath(projectTo.getLogoFile().getOriginalFilename());
        return new File(filename, contentPath + normalizePath(projectTo.getName()) + LOGO_DIR + filename);
    }

    private File createCardImageFile(ProjectTo projectTo) {
        String filename = normalizePath(projectTo.getCardImageFile().getOriginalFilename());
        return new File(filename, contentPath + normalizePath(projectTo.getName()) + CARD_IMG_DIR + filename);
    }

    private File createDockerComposeFile(ProjectTo projectTo) {
        if (isMultipartFileEmpty(projectTo.getDockerComposeFile())) {
            return null;
        }
        String filename = normalizePath(projectTo.getDockerComposeFile().getOriginalFilename());
        return new File(filename, contentPath + normalizePath(projectTo.getName()) + DOCKER_DIR + filename);
    }
}
