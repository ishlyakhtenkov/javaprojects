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
        project.getDescriptionElements().forEach(de -> FileUtil.deleteFile(de.getFileLink()));
    }


    @Transactional
    public void enable(long id, boolean enabled) {
        Project project = get(id);
        project.setEnabled(enabled);
    }

    @Transactional
    public Project create(ProjectTo projectTo) {
        Assert.notNull(projectTo, "projectTo must not be null");
        if (projectTo.getLogoFile() == null || projectTo.getLogoFile().isEmpty()) {
            throw new IllegalRequestDataException("Project logo file is not present",
                    "project.logo-not-present", null);
        }
        if (projectTo.getCardImageFile() == null || projectTo.getCardImageFile().isEmpty()) {
            throw new IllegalRequestDataException("Project card image file is not present",
                    "project.card-image-not-present", null);
        }
        Project project = repository.saveAndFlush(projectUtil.createNewFromTo(projectTo));
        uploadFile(projectTo.getLogoFile(), project, LOGO_DIR, projectTo.getLogoFile().getOriginalFilename());
        uploadFile(projectTo.getCardImageFile(), project, CARD_IMG_DIR, projectTo.getCardImageFile().getOriginalFilename());
        if (projectTo.getDockerComposeFile() != null && !projectTo.getDockerComposeFile().isEmpty()) {
            uploadFile(projectTo.getDockerComposeFile(), project, DOCKER_DIR,
                    projectTo.getDockerComposeFile().getOriginalFilename());
        }
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE)
                .forEach(deTo -> {
                    String uniquePrefixFileName = deTo.getFileLink().substring(deTo.getFileLink().lastIndexOf('/') + 1);
                    if (deTo.getImageFile() != null && !deTo.getImageFile().isEmpty()) {
                        uploadFile(deTo.getImageFile(), project, DESCRIPTION_IMG_DIR, uniquePrefixFileName);
                    } else if (descriptionElementToHasImageFileString(deTo)) {
                        uploadFile(Base64.getDecoder().decode(deTo.getImageFileString()), project, DESCRIPTION_IMG_DIR,
                                uniquePrefixFileName);
                    }
                });
        return project;
    }

    private boolean descriptionElementToHasImageFileString(DescriptionElementTo deTo) {
        return deTo.getImageFileString() != null && !deTo.getImageFileString().isEmpty() && deTo.getFileName() != null
                && !deTo.getFileName().isEmpty();
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

        //TODO create de images for new des
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && deTo.isNew())
                .forEach(deTo -> {
                    String uniquePrefixFileName = deTo.getFileLink().substring(deTo.getFileLink().lastIndexOf('/') + 1);
                    if (deTo.getImageFile() != null && !deTo.getImageFile().isEmpty()) {
                        uploadFile(deTo.getImageFile(), project, DESCRIPTION_IMG_DIR, uniquePrefixFileName);
                    } else if (descriptionElementToHasImageFileString(deTo)) {
                        uploadFile(Base64.getDecoder().decode(deTo.getImageFileString()), project, DESCRIPTION_IMG_DIR,
                                uniquePrefixFileName);
                    }
                });

        //TODO delete de images for deleted des
        oldDeImages.values().stream()
                .filter(oldDe -> !project.getDescriptionElements().contains(oldDe))
                .forEach(oldDe -> FileUtil.deleteFile(oldDe.getFileLink()));

//        for (DescriptionElement oldDe : oldDeImages.values()) {
//            if (!project.getDescriptionElements().contains(oldDe)) {
//                FileUtil.deleteFile(oldDe.getFileLink());
//            }
//        }

        //TODO add new de images for updated des
        //TODO delete old de images for updated des
        //TODO move old de images when project name changed
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && !deTo.isNew())
                .forEach(deTo -> {
                    if (descriptionElementToHasImage(deTo)) {
                        String uniquePrefixFileName = deTo.getFileLink().substring(deTo.getFileLink().lastIndexOf('/') + 1);
                        if (deTo.getImageFile() != null && !deTo.getImageFile().isEmpty()) {
                            uploadFile(deTo.getImageFile(), project, DESCRIPTION_IMG_DIR, uniquePrefixFileName);
                        } else if (descriptionElementToHasImageFileString(deTo)) {
                            uploadFile(Base64.getDecoder().decode(deTo.getImageFileString()), project, DESCRIPTION_IMG_DIR,
                                    uniquePrefixFileName);
                        }

                        FileUtil.deleteFile(oldDeImages.get(deTo.getId()).getFileLink());
                    } else if (!project.getName().equalsIgnoreCase(projectOldName)) {
                        FileUtil.moveFile(oldDeImages.get(deTo.getId()).getFileLink(), contentPath +
                                FileUtil.normalizePath(project.getName() + DESCRIPTION_IMG_DIR));
                    }
                });

        if (projectTo.getLogoFile() != null && !projectTo.getLogoFile().isEmpty()) {
            FileUtil.deleteFile(oldLogoFileLink);
            String newLogoFileName =  FileUtil.normalizePath(projectTo.getLogoFile().getOriginalFilename());
            FileUtil.upload(projectTo.getLogoFile(), contentPath + FileUtil.normalizePath(project.getName()) +
                    LOGO_DIR, newLogoFileName);
        } else if (!project.getName().equalsIgnoreCase(projectOldName)) {
            FileUtil.moveFile(oldLogoFileLink, contentPath + FileUtil.normalizePath(project.getName() + LOGO_DIR));
        }
        if (projectTo.getCardImageFile() != null && !projectTo.getCardImageFile().isEmpty()) {
            FileUtil.deleteFile(oldCardImageFileLink);
            String newCardImageFileName =  FileUtil.normalizePath(projectTo.getCardImageFile().getOriginalFilename());
            FileUtil.upload(projectTo.getCardImageFile(), contentPath + FileUtil.normalizePath(project.getName()) +
                    CARD_IMG_DIR, newCardImageFileName);
        } else if (!project.getName().equalsIgnoreCase(projectOldName)) {
            FileUtil.moveFile(oldCardImageFileLink, contentPath + FileUtil.normalizePath(project.getName() + CARD_IMG_DIR));
        }
        if (projectTo.getDockerComposeFile() != null && !projectTo.getDockerComposeFile().isEmpty()) {
            if (oldDockerComposeFileLink != null) {
                FileUtil.deleteFile(oldDockerComposeFileLink);
            }
            String newDockerComposeFileName =  FileUtil.normalizePath(projectTo.getDockerComposeFile().getOriginalFilename());
            FileUtil.upload(projectTo.getDockerComposeFile(), contentPath + FileUtil.normalizePath(project.getName()) +
                    DOCKER_DIR, newDockerComposeFileName);
        } else if (!project.getName().equalsIgnoreCase(projectOldName) && oldDockerComposeFileLink != null) {
            FileUtil.moveFile(oldDockerComposeFileLink, contentPath + FileUtil.normalizePath(project.getName() + DOCKER_DIR));
        }
        return project;
    }

    private boolean descriptionElementToHasImage(DescriptionElementTo deTo) {
        return (deTo.getImageFile() != null && !deTo.getImageFile().isEmpty()) ||
                descriptionElementToHasImageFileString(deTo);
    }

    private void uploadFile(MultipartFile file, Project project, String dirName, String fileName) {
        FileUtil.upload(file, contentPath + FileUtil.normalizePath(project.getName() + "/" + dirName + "/"),
                FileUtil.normalizePath(fileName));
    }

    private void uploadFile(byte[] fileBytes, Project project, String dirName, String fileName) {
        FileUtil.upload(fileBytes, contentPath + FileUtil.normalizePath(project.getName() + "/" + dirName + "/"),
                FileUtil.normalizePath(fileName));
    }
}
