package ru.javaprojects.projector.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.projects.model.DescriptionElement;
import ru.javaprojects.projector.projects.model.Project;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.javaprojects.projector.common.util.FileUtil.isMultipartFileEmpty;
import static ru.javaprojects.projector.projects.ProjectUtil.deToHasImageFileString;
import static ru.javaprojects.projector.projects.model.ElementType.IMAGE;


@Service
@RequiredArgsConstructor
public class ProjectService {
    public static final String LOGO_DIR = "/logo/";
    public static final String DOCKER_DIR = "/docker/";
    public static final String CARD_IMG_DIR = "/card_img/";
    public static final String DESCRIPTION_IMG_DIR = "/description/images/";

    private final ProjectRepository repository;
    private final ProjectUtil projectUtil;

    @Value("${content-path.projects}")
    private String contentPath;

    public Project get(long id) {
        return repository.getExisted(id);
    }

    public Project getWithTechnologies(long id, boolean sort) {
        Project project = repository.findWithTechnologiesById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "notfound.entity", new Object[]{id}));
        if (sort) {
            project.setTechnologies(new TreeSet<>(project.getTechnologies()));
        }
        return project;
    }

    public Project getWithTechnologiesAndDescription(long id, boolean sort) {
        Project project = repository.findWithTechnologiesAndDescriptionById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "notfound.entity", new Object[]{id}));
        if (sort) {
            project.setTechnologies(new TreeSet<>(project.getTechnologies()));
            project.setDescriptionElements(new TreeSet<>(project.getDescriptionElements()));
        }
        return project;
    }

    public Project getByName(String name) {
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Not found project with name =" + name, "notfound.project",
                        new Object[]{name}));
    }

    public List<Project> getAll() {
        return repository.findAllByOrderByName();
    }

    @Transactional
    public void delete(long id) {
        Project project = repository.findWithDescriptionByIdAndDescriptionElements_Type(id, IMAGE).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "notfound.entity", new Object[]{id}));
        repository.delete(project);
        repository.flush();
        FileUtil.deleteFile(project.getLogoFile().getFileLink());
        FileUtil.deleteFile(project.getCardImageFile().getFileLink());
        if (project.getDockerComposeFile() != null) {
            FileUtil.deleteFile(project.getDockerComposeFile().getFileLink());
        }
        project.getDescriptionElements().stream()
                .filter(de -> de.getType() == IMAGE)
                .forEach(de -> FileUtil.deleteFile(de.getFileLink()));
    }


    @Transactional
    public void enable(long id, boolean enabled) {
        Project project = get(id);
        project.setEnabled(enabled);
    }

    @Transactional
    public Project create(ProjectTo projectTo) {
        Assert.notNull(projectTo, "projectTo must not be null");
        if (isMultipartFileEmpty( projectTo.getLogoFile())) {
            throw new IllegalRequestDataException("Project logo file is not present",
                    "project.logo-not-present", null);
        }
        if (isMultipartFileEmpty(projectTo.getCardImageFile())) {
            throw new IllegalRequestDataException("Project card image file is not present",
                    "project.card-image-not-present", null);
        }
        Project project = repository.saveAndFlush(projectUtil.createNewFromTo(projectTo));

        uploadFile(projectTo.getLogoFile(), project.getName(), LOGO_DIR, projectTo.getLogoFile().getOriginalFilename());
        uploadFile(projectTo.getCardImageFile(), project.getName(), CARD_IMG_DIR,
                projectTo.getCardImageFile().getOriginalFilename());
        if (!isMultipartFileEmpty(projectTo.getDockerComposeFile())) {
            uploadFile(projectTo.getDockerComposeFile(), project.getName(), DOCKER_DIR,
                    projectTo.getDockerComposeFile().getOriginalFilename());
        }
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE)
                .forEach(deTo -> uploadDeImage(deTo, project.getName()));
        return project;
    }

    private void uploadDeImage(DescriptionElementTo deTo, String projectName) {
        String uniquePrefixFileName = deTo.getFileLink().substring(deTo.getFileLink().lastIndexOf('/') + 1);
        if (!isMultipartFileEmpty(deTo.getImageFile())) {
            uploadFile(deTo.getImageFile(), projectName, DESCRIPTION_IMG_DIR, uniquePrefixFileName);
        } else if (deToHasImageFileString(deTo)) {
            uploadFile(Base64.getDecoder().decode(deTo.getImageFileString()), projectName, DESCRIPTION_IMG_DIR,
                    uniquePrefixFileName);
        }
    }

    @Transactional
    public Project update(ProjectTo projectTo) {
        Assert.notNull(projectTo, "projectTo must not be null");
        Project project = getWithTechnologiesAndDescription(projectTo.getId(), false);
        String projectOldName = project.getName();
        String oldLogoFileLink = project.getLogoFile().getFileLink();
        String oldCardImageFileLink = project.getCardImageFile().getFileLink();
        String oldDockerComposeFileLink =
                project.getDockerComposeFile() != null ? project.getDockerComposeFile().getFileLink() : null;
        Map<Long, DescriptionElement> oldDeImages = project.getDescriptionElements().stream()
                .filter(de -> de.getType() == IMAGE)
                .map(DescriptionElement::new)
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        repository.saveAndFlush(projectUtil.updateFromTo(project, projectTo));

        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && deTo.isNew())
                .forEach(deTo -> uploadDeImage(deTo, project.getName()));
        oldDeImages.values().stream()
                .filter(oldDeImage -> !project.getDescriptionElements().contains(oldDeImage))
                .forEach(oldDe -> FileUtil.deleteFile(oldDe.getFileLink()));
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && !deTo.isNew())
                .forEach(deTo -> {
                    if (!isMultipartFileEmpty(deTo.getImageFile()) || deToHasImageFileString(deTo)) {
                        uploadDeImage(deTo, project.getName());
                        FileUtil.deleteFile(oldDeImages.get(deTo.getId()).getFileLink());
                    } else if (!project.getName().equalsIgnoreCase(projectOldName)) {
                        FileUtil.moveFile(oldDeImages.get(deTo.getId()).getFileLink(), contentPath +
                                FileUtil.normalizePath(project.getName() + DESCRIPTION_IMG_DIR));
                    }
                });

        updateProjectFileIfNecessary(projectTo.getLogoFile(), oldLogoFileLink, project.getName(),
                projectOldName, LOGO_DIR);
        updateProjectFileIfNecessary(projectTo.getCardImageFile(), oldCardImageFileLink, project.getName(),
                projectOldName, CARD_IMG_DIR);
        updateProjectFileIfNecessary(projectTo.getDockerComposeFile(), oldDockerComposeFileLink, project.getName(),
                projectOldName, DOCKER_DIR);
        return project;
    }

    private void updateProjectFileIfNecessary(MultipartFile file, String oldFileFileLink, String projectName,
                                              String projectOldName, String dirName) {
        if (!isMultipartFileEmpty(file)) {
            if (oldFileFileLink != null) {
                FileUtil.deleteFile(oldFileFileLink);
            }
            String newFileName =  FileUtil.normalizePath(file.getOriginalFilename());
            FileUtil.upload(file, contentPath + FileUtil.normalizePath(projectName) + dirName, newFileName);
        } else if (!projectName.equalsIgnoreCase(projectOldName)) {
            FileUtil.moveFile(oldFileFileLink, contentPath + FileUtil.normalizePath(projectName + dirName));
        }
    }

    private void uploadFile(MultipartFile file, String projectName, String dirName, String fileName) {
        FileUtil.upload(file, contentPath + FileUtil.normalizePath(projectName + "/" + dirName + "/"),
                FileUtil.normalizePath(fileName));
    }

    private void uploadFile(byte[] fileBytes, String projectName, String dirName, String fileName) {
        FileUtil.upload(fileBytes, contentPath + FileUtil.normalizePath(projectName + "/" + dirName + "/"),
                FileUtil.normalizePath(fileName));
    }
}
