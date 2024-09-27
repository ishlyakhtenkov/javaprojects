package ru.javaprojects.projector.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.projects.model.DescriptionElement;
import ru.javaprojects.projector.projects.model.Like;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.repository.LikeRepository;
import ru.javaprojects.projector.projects.to.DescriptionElementTo;
import ru.javaprojects.projector.projects.to.ProjectTo;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.users.service.UserService;

import java.util.Comparator;
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
    private final LikeRepository likeRepository;
    private final UserService userService;

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

    @Transactional
    public void addViewsToProject(long id) {
        Project project = repository.findForAddViewsById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "notfound.entity", new Object[]{id}));
        project.setViews(project.getViews() + 1);
    }

    public Project getWithTechnologiesAndDescription(long id, Comparator<Technology> technologyComparator) {
        Project project = repository.findWithTechnologiesAndDescriptionById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "notfound.entity", new Object[]{id}));
        TreeSet<Technology> sortedTechnologies = new TreeSet<>(technologyComparator);
        sortedTechnologies.addAll(project.getTechnologies());
        project.setTechnologies(sortedTechnologies);
        project.setDescriptionElements(new TreeSet<>(project.getDescriptionElements()));
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

    public List<Project> getAllEnabled() {
        return repository.findAllByEnabledIsTrueOrderByName();
    }

    public List<Project> getAllEnabledWithArchitectureAndTechnologies() {
        List<Project> projects = repository.findAllWithArchitectureAndTechnologiesByEnabledIsTrue();
        projects.sort(Comparator.comparingInt(p -> p.getPriority().ordinal()));
        projects.forEach(project -> {
            Comparator<Technology> technologyComparator = Comparator
                    .comparingInt((Technology t) -> t.getUsage().ordinal())
                    .thenComparing(t -> t.getPriority().ordinal())
                    .thenComparing(Comparator.naturalOrder());
            TreeSet<Technology> sortedTechnologies = new TreeSet<>(technologyComparator);
            sortedTechnologies.addAll(project.getTechnologies());
            project.setTechnologies(sortedTechnologies);
        });
        return projects;
    }

    @Transactional
    public void delete(long id) {
        Project project = repository.findWithDescriptionById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "notfound.entity", new Object[]{id}));
        repository.delete(project);
        repository.flush();
        FileUtil.deleteFile(project.getLogo().getFileLink());
        FileUtil.deleteFile(project.getCardImage().getFileLink());
        if (project.getDockerCompose() != null) {
            FileUtil.deleteFile(project.getDockerCompose().getFileLink());
        }
        project.getDescriptionElements().stream()
                .filter(de -> de.getType() == IMAGE && de.getImage() != null)
                .forEach(de -> FileUtil.deleteFile(de.getImage().getFileLink()));
    }


    @Transactional
    public void enable(long id, boolean enabled) {
        Project project = get(id);
        project.setEnabled(enabled);
    }

    @Transactional
    public Project create(ProjectTo projectTo) {
        Assert.notNull(projectTo, "projectTo must not be null");
        if (projectTo.getLogo() == null || projectTo.getLogo().isEmpty()) {
            throw new IllegalRequestDataException("Project logo file is not present",
                    "project.logo-not-present", null);
        }
        if (projectTo.getCardImage() == null || projectTo.getCardImage().isEmpty()) {
            throw new IllegalRequestDataException("Project card image file is not present",
                    "project.card-image-not-present", null);
        }
        Project project = repository.saveAndFlush(projectUtil.createNewFromTo(projectTo));

        uploadFile(projectTo.getLogo(), project.getName(), LOGO_DIR);
        uploadFile(projectTo.getCardImage(), project.getName(), CARD_IMG_DIR);
        if (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) {
            uploadFile(projectTo.getDockerCompose(), project.getName(), DOCKER_DIR);
        }
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && deTo.getImage() != null)
                .forEach(deTo -> uploadDescriptionElementImage(deTo, project.getName()));
        return project;
    }

    private void uploadDescriptionElementImage(DescriptionElementTo deTo, String projectName) {
        FileTo image = deTo.getImage();
        String uniquePrefixFileName = image.getFileLink().substring(image.getFileLink().lastIndexOf('/') + 1);
        uploadFile(image, projectName, DESCRIPTION_IMG_DIR, uniquePrefixFileName);
    }

    @Transactional
    public Project update(ProjectTo projectTo) {
        Assert.notNull(projectTo, "projectTo must not be null");
        Project project = repository.findWithTechnologiesAndDescriptionById(projectTo.getId()).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + projectTo.getId(), "notfound.entity",
                        new Object[]{projectTo.getId()}));
        String projectOldName = project.getName();
        String oldLogoFileLink = project.getLogo().getFileLink();
        String oldCardImageFileLink = project.getCardImage().getFileLink();
        String oldDockerComposeFileLink =
                project.getDockerCompose() != null ? project.getDockerCompose().getFileLink() : null;
        Map<Long, DescriptionElement> oldDeImages = project.getDescriptionElements().stream()
                .filter(de -> de.getType() == IMAGE)
                .map(DescriptionElement::new)
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        repository.saveAndFlush(projectUtil.updateFromTo(project, projectTo));

        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && deTo.isNew() && deTo.getImage() != null)
                .forEach(deTo -> uploadDescriptionElementImage(deTo, project.getName()));
        oldDeImages.values().stream()
                .filter(oldDeImage -> !project.getDescriptionElements().contains(oldDeImage))
                .forEach(oldDe -> FileUtil.deleteFile(oldDe.getImage().getFileLink()));
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && !deTo.isNew())
                .forEach(deTo -> {
                    if (deTo.getImage() != null && !deTo.getImage().isEmpty()) {
                        uploadDescriptionElementImage(deTo, project.getName());
                        FileUtil.deleteFile(oldDeImages.get(deTo.getId()).getImage().getFileLink());
                    } else if (!project.getName().equalsIgnoreCase(projectOldName)) {
                        FileUtil.moveFile(oldDeImages.get(deTo.getId()).getImage().getFileLink(), contentPath +
                                FileUtil.normalizePath(project.getName() + DESCRIPTION_IMG_DIR));
                    }
                });

        updateProjectFileIfNecessary(projectTo.getLogo(), oldLogoFileLink, project.getLogo().getFileLink(), project.getName(),
                projectOldName, LOGO_DIR);
        updateProjectFileIfNecessary(projectTo.getCardImage(), oldCardImageFileLink, project.getCardImage().getFileLink(),
                project.getName(), projectOldName, CARD_IMG_DIR);
        updateProjectFileIfNecessary(projectTo.getDockerCompose(), oldDockerComposeFileLink,
                projectTo.getDockerCompose() != null ? projectTo.getDockerCompose().getFileLink() : null,
                project.getName(), projectOldName, DOCKER_DIR);
        return project;
    }

    private void updateProjectFileIfNecessary(FileTo fileTo, String oldFileFileLink, String currentFileLink, String projectName,
                                              String projectOldName, String dirName) {
        if (fileTo != null && !fileTo.isEmpty()) {
            if (oldFileFileLink != null && !oldFileFileLink.equalsIgnoreCase(currentFileLink)) {
                FileUtil.deleteFile(oldFileFileLink);
            }
            String fileName = (fileTo.getInputtedFile() != null && !fileTo.getInputtedFile().isEmpty()) ?
                    fileTo.getInputtedFile().getOriginalFilename() : fileTo.getFileName();
            FileUtil.upload(fileTo, contentPath + FileUtil.normalizePath(projectName) + dirName,
                    FileUtil.normalizePath(fileName));
        } else if (!projectName.equalsIgnoreCase(projectOldName)) {
            FileUtil.moveFile(oldFileFileLink, contentPath + FileUtil.normalizePath(projectName + dirName));
        }
    }

    private void uploadFile(FileTo fileTo, String projectName, String dirName) {
        String fileName = fileTo.getInputtedFile() != null && !fileTo.getInputtedFile().isEmpty() ?
                fileTo.getInputtedFile().getOriginalFilename() : fileTo.getFileName();
        uploadFile(fileTo, projectName, dirName, fileName);
    }

    private void uploadFile(FileTo fileTo, String projectName, String dirName, String fileName) {
        FileUtil.upload(fileTo, contentPath + FileUtil.normalizePath(projectName + "/" + dirName + "/"),
                FileUtil.normalizePath(fileName));
    }


    public void like(long id, boolean liked, long userId) {
        get(id);
        userService.get(userId);
        likeRepository.findByProjectIdAndUserId(id, userId).ifPresentOrElse(like -> {
            if (!liked) {
                likeRepository.delete(like);
            }
        }, () -> {
            if (liked) {
                likeRepository.save(new Like(null, id, userId));
            }
        });
    }
}
