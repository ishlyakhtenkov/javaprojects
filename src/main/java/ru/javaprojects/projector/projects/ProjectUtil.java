package ru.javaprojects.projector.projects;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.common.model.LogoFile;
import ru.javaprojects.projector.projects.model.CardImageFile;
import ru.javaprojects.projector.projects.model.DockerComposeFile;
import ru.javaprojects.projector.projects.model.Project;

import java.util.TreeSet;

import static ru.javaprojects.projector.common.util.FileUtil.normalizeFileName;

@UtilityClass
public class ProjectUtil {

    public static ProjectTo asTo(Project project) {
        return new ProjectTo(project.getId(), project.getName(), project.getShortDescription(), project.isEnabled(),
                project.getPriority(), project.getStartDate(), project.getEndDate(), project.getArchitecture(),
                project.getDeploymentUrl(), project.getBackendSrcUrl(), project.getFrontendSrcUrl(), project.getOpenApiUrl(),
                project.getTechnologies());
    }

    public static Project createNewFromTo(ProjectTo projectTo, String contentPath) {
        return new Project(null, projectTo.getName(), projectTo.getShortDescription(), projectTo.isEnabled(),
                projectTo.getPriority(), projectTo.getStartDate(), projectTo.getEndDate(), projectTo.getArchitecture(),
                createLogoFile(projectTo, contentPath), createDockerComposeFile(projectTo, contentPath),
                createCardImageFile(projectTo, contentPath), projectTo.getDeploymentUrl(), projectTo.getBackendSrcUrl(),
                projectTo.getFrontendSrcUrl(), projectTo.getOpenApiUrl(), new TreeSet<>(projectTo.getTechnologies()));
    }

    public static Project updateFromTo(Project project, ProjectTo projectTo, String contentPath) {
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
        project.setTechnologies(new TreeSet<>(projectTo.getTechnologies()));
        if (projectTo.getLogoFile() != null && !projectTo.getLogoFile().isEmpty()) {
            project.setLogoFile(createLogoFile(projectTo, contentPath));
        } else if (!projectTo.getName().equalsIgnoreCase(project.getName())) {
            project.getLogoFile().setFileLink(contentPath + normalizeFileName(projectTo.getName()) + "/logo/" +
                    project.getLogoFile().getFileName());
        }
        if (projectTo.getCardImageFile() != null && !projectTo.getCardImageFile().isEmpty()) {
            project.setCardImageFile(createCardImageFile(projectTo, contentPath));
        } else if (!projectTo.getName().equalsIgnoreCase(project.getName())) {
            project.getCardImageFile().setFileLink(contentPath + normalizeFileName(projectTo.getName()) + "/card_img/" +
                    project.getCardImageFile().getFileName());
        }
        if (projectTo.getDockerComposeFile() != null && !projectTo.getDockerComposeFile().isEmpty()) {
            project.setDockerComposeFile(createDockerComposeFile(projectTo, contentPath));
        } else if (!projectTo.getName().equalsIgnoreCase(project.getName()) && project.getDockerComposeFile() != null) {
            project.getDockerComposeFile().setFileLink(contentPath + normalizeFileName(projectTo.getName()) + "/docker/" +
                    project.getDockerComposeFile().getFileName());
        }
        project.setName(projectTo.getName());
        return project;
    }


    private LogoFile createLogoFile(ProjectTo projectTo, String contentPath) {
        String filename = normalizeFileName(projectTo.getLogoFile().getOriginalFilename());
        return new LogoFile(filename, contentPath + normalizeFileName(projectTo.getName()) + "/logo/" + filename);
    }

    private CardImageFile createCardImageFile(ProjectTo projectTo, String contentPath) {
        String filename = normalizeFileName(projectTo.getCardImageFile().getOriginalFilename());
        return new CardImageFile(filename, contentPath + normalizeFileName(projectTo.getName()) + "/card_img/" + filename);
    }

    private DockerComposeFile createDockerComposeFile(ProjectTo projectTo, String contentPath) {
        if (projectTo.getDockerComposeFile().isEmpty()) {
            return null;
        }
        String filename = normalizeFileName(projectTo.getDockerComposeFile().getOriginalFilename());
        return new DockerComposeFile(filename, contentPath + normalizeFileName(projectTo.getName()) + "/docker/" + filename);
    }
}
