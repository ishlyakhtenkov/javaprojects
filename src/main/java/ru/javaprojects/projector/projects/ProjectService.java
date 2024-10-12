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
import ru.javaprojects.projector.projects.model.*;
import ru.javaprojects.projector.projects.repository.CommentCount;
import ru.javaprojects.projector.projects.repository.CommentRepository;
import ru.javaprojects.projector.projects.repository.LikeRepository;
import ru.javaprojects.projector.projects.repository.ProjectRepository;
import ru.javaprojects.projector.projects.to.CommentTo;
import ru.javaprojects.projector.projects.to.DescriptionElementTo;
import ru.javaprojects.projector.projects.to.ProjectTo;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.users.model.User;
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
    private final CommentRepository commentRepository;
    private final UserService userService;

    @Value("${content-path.projects}")
    private String projectFilesPath;

    public Project get(long id) {
        return repository.getExisted(id);
    }

    @Transactional
    public void addViewsToProject(long id) {
        Project project = repository.findForAddViewsById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "notfound.entity", new Object[]{id}));
        project.setViews(project.getViews() + 1);
    }

    public Project getWithAllInformation(long id, Comparator<Technology> technologyComparator) {
        Project project = repository.findWithAllInformationById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "notfound.entity", new Object[]{id}));
        TreeSet<Technology> sortedTechnologies = new TreeSet<>(technologyComparator);
        sortedTechnologies.addAll(project.getTechnologies());
        project.setTechnologies(sortedTechnologies);
        project.setDescriptionElements(new TreeSet<>(project.getDescriptionElements()));
        project.setComments(commentRepository.findAllByProjectIdOrderByCreated(id));
        return project;
    }

    public Project getByName(String name) {
        Assert.notNull(name, "name must not be null");
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Not found project with name =" + name, "notfound.project",
                        new Object[]{name}));
    }

    public List<Project> getAll() {
        return repository.findAllWithArchitectureByOrderByName();
    }

    public List<Project> getAllEnabled() {
        return repository.findAllByEnabledIsTrueOrderByName();
    }

    public List<Project> getAllEnabledWithArchitectureAndTechnologiesAndLikes() {
        List<Project> projects = repository.findAllWithArchAndTechnologiesAndLikesByEnabledIsTrue();
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
        likeRepository.deleteAllByObjectId(id);
        likeRepository.flush();
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

        uploadFile(projectTo.getLogo(), project.getName(), LOGO_DIR, projectTo.getLogo().getRealFileName());
        uploadFile(projectTo.getCardImage(), project.getName(), CARD_IMG_DIR, projectTo.getCardImage().getRealFileName());
        if (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) {
            uploadFile(projectTo.getDockerCompose(), project.getName(), DOCKER_DIR,
                    projectTo.getDockerCompose().getRealFileName());
        }
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && deTo.getImage() != null)
                .forEach(deTo -> uploadDeImage(deTo, project.getName()));
        return project;
    }

    @Transactional
    public Project update(ProjectTo projectTo) {
        Assert.notNull(projectTo, "projectTo must not be null");
        Project project = repository.findWithAllInformationById(projectTo.getId()).orElseThrow(() ->
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
                .forEach(deTo -> uploadDeImage(deTo, project.getName()));
        oldDeImages.values().stream()
                .filter(oldDeImage -> !project.getDescriptionElements().contains(oldDeImage))
                .forEach(oldDe -> FileUtil.deleteFile(oldDe.getImage().getFileLink()));
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && !deTo.isNew())
                .forEach(deTo -> {
                    if (deTo.getImage() != null && !deTo.getImage().isEmpty()) {
                        uploadDeImage(deTo, project.getName());
                        FileUtil.deleteFile(oldDeImages.get(deTo.getId()).getImage().getFileLink());
                    } else if (!project.getName().equalsIgnoreCase(projectOldName)) {
                        FileUtil.moveFile(oldDeImages.get(deTo.getId()).getImage().getFileLink(), projectFilesPath +
                                FileUtil.normalizePath(project.getName() + DESCRIPTION_IMG_DIR));
                    }
                });

        updateProjectFileIfNecessary(projectTo.getLogo(), oldLogoFileLink, project.getLogo().getFileLink(),
                project.getName(), projectOldName, LOGO_DIR);
        updateProjectFileIfNecessary(projectTo.getCardImage(), oldCardImageFileLink, project.getCardImage().getFileLink(),
                project.getName(), projectOldName, CARD_IMG_DIR);
        updateProjectFileIfNecessary(projectTo.getDockerCompose(), oldDockerComposeFileLink,
                projectTo.getDockerCompose() != null ? projectTo.getDockerCompose().getFileLink() : null,
                project.getName(), projectOldName, DOCKER_DIR);
        return project;
    }

    private void updateProjectFileIfNecessary(FileTo fileTo, String oldFileLink, String currentFileLink,
                                              String projectName, String projectOldName, String dirName) {
        if (fileTo != null && !fileTo.isEmpty()) {
            if (oldFileLink != null && !oldFileLink.equalsIgnoreCase(currentFileLink)) {
                FileUtil.deleteFile(oldFileLink);
            }
            FileUtil.upload(fileTo, projectFilesPath + FileUtil.normalizePath(projectName + dirName),
                    FileUtil.normalizePath(fileTo.getRealFileName()));
        } else if (!projectName.equalsIgnoreCase(projectOldName)) {
            FileUtil.moveFile(oldFileLink, projectFilesPath + FileUtil.normalizePath(projectName + dirName));
        }
    }

    private void uploadDeImage(DescriptionElementTo deTo, String projectName) {
        FileTo image = deTo.getImage();
        String uniquePrefixFileName = image.getFileLink().substring(image.getFileLink().lastIndexOf('/') + 1);
        uploadFile(image, projectName, DESCRIPTION_IMG_DIR, uniquePrefixFileName);
    }

    private void uploadFile(FileTo fileTo, String projectName, String dirName, String fileName) {
        FileUtil.upload(fileTo, projectFilesPath + FileUtil.normalizePath(projectName + dirName),
                FileUtil.normalizePath(fileName));
    }

    public void likeProject(long id, boolean liked, long userId) {
        get(id);
        userService.get(userId);
        likeRepository.findByObjectIdAndUserId(id, userId).ifPresentOrElse(like -> {
            if (!liked) {
                likeRepository.delete(like);
            }
        }, () -> {
            if (liked) {
                likeRepository.save(new Like(null, id, userId, ObjectType.PROJECT));
            }
        });
    }

    public Comment createComment(CommentTo commentTo, long userId) {
        Assert.notNull(commentTo, "commentTo must not be null");
        User user = userService.get(userId);
        get(commentTo.getProjectId());
        if (commentTo.getParentId() != null) {
            commentRepository.getExisted(commentTo.getParentId());
        }
        return commentRepository.save(new Comment(null, commentTo.getProjectId(), user, commentTo.getParentId(),
                commentTo.getText()));
    }

    public void likeComment(long commentId, boolean liked, long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Not found comment with id=" + commentId, "notfound.entity", new Object[]{commentId}));
        if (comment.getAuthor().id() == userId) {
            throw new IllegalRequestDataException("Forbidden to like yourself, userId=" + userId +
                    ", commentId=" + commentId, "comment.forbidden-like-yourself", null);
        }
        userService.get(userId);
        likeRepository.findByObjectIdAndUserId(commentId, userId).ifPresentOrElse(like -> {
            if (!liked) {
                likeRepository.delete(like);
            }
        }, () -> {
            if (liked) {
                likeRepository.save(new Like(null, commentId, userId, ObjectType.COMMENT));
            }
        });
    }

    @Transactional
    public void deleteComment(long commentId, long userId, boolean byAdmin) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Not found comment with id=" + commentId, "notfound.entity", new Object[]{commentId}));
        if (comment.getAuthor().id() == userId || byAdmin) {
            comment.setDeleted(true);
        } else {
            throw new IllegalRequestDataException("Forbidden to delete another user comment, commentId=" + commentId +
                    ", userId=" + userId, "comment.forbidden-delete-another", null);
        }
    }

    @Transactional
    public void updateComment(long commentId, String text, long userId) {
        Assert.notNull(text, "text must not be null");
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Not found comment with id=" + commentId, "notfound.entity", new Object[]{commentId}));
        if (comment.getAuthor().id() == userId) {
            comment.setText(text);
        } else {
            throw new IllegalRequestDataException("Forbidden to edit another user comment, commentId=" + commentId +
                    ", userId=" + userId, "comment.forbidden-edit-another", null);
        }
    }

    public Map<Long, Long> getTotalCommentsByProject() {
        return commentRepository.countTotalCommentsByProject().stream()
                .collect(Collectors.toMap(CommentCount::getProjectId, CommentCount::getTotalComment));
    }
}
