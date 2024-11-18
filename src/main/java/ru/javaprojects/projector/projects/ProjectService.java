package ru.javaprojects.projector.projects;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;
import ru.javaprojects.projector.projects.to.ProjectTo;
import ru.javaprojects.projector.reference.architectures.Architecture;
import ru.javaprojects.projector.reference.architectures.ArchitectureService;
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
    public static final String PREVIEW_DIR = "/preview/";
    public static final String DESCRIPTION_IMG_DIR = "/description/images/";

    private final ProjectRepository repository;
    private final ProjectUtil projectUtil;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ArchitectureService architectureService;
    private UserService userService;

    @Value("${content-path.projects}")
    private String projectFilesPath;

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    public Project get(long id) {
        return repository.getExisted(id);
    }

    public Project getWithAllInformation(long id, Comparator<Technology> technologyComparator) {
        Project project = repository.findWithAllInformationAndDescriptionById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "error.notfound.entity", new Object[]{id}));
        localizeArchitecture(project);
        TreeSet<Technology> sortedTechnologies = new TreeSet<>(technologyComparator);
        sortedTechnologies.addAll(project.getTechnologies());
        project.setTechnologies(sortedTechnologies);
        project.setDescriptionElements(new TreeSet<>(project.getDescriptionElements()));
        project.setComments(commentRepository.findAllByProjectId(id, Sort.by("created")));
        return project;
    }

    private void localizeArchitecture(HasArchitecture project) {
        Architecture localizedArchitecture = architectureService.getLocalized(project.getArchitecture().getId(),
                LocaleContextHolder.getLocale().getLanguage());
        if (localizedArchitecture != null) {
            project.setArchitecture(localizedArchitecture);
        }
    }

    public Page<ProjectPreviewTo> getAll(Pageable pageable) {
        Assert.notNull(pageable, "pageable must not be null");
        return getAll(repository.findAllIds(pageable), pageable);
    }

    public Page<ProjectPreviewTo> getAllByKeyword(String keyword, Pageable pageable) {
        Assert.notNull(keyword, "keyword must not be null");
        Assert.notNull(pageable, "pageable must not be null");
        return getAll(repository.findAllIdsByKeyword(keyword, pageable), pageable);
    }

    private Page<ProjectPreviewTo> getAll(Page<Long> projectsIds, Pageable pageable) {
        List<Project> projects = repository.findAllWithArchitectureAndAuthorAndLikesByIdIn(projectsIds.getContent(),
                Sort.by("name"));
        projects.forEach(this::localizeArchitecture);
        Map<Long, Integer> commentsCountByProjects = getCommentsCountByProjects(projects);
        List<ProjectPreviewTo> projectPreviewTos = projectUtil.asPreviewTos(projects, commentsCountByProjects);
        return new PageImpl<>(projectPreviewTos, pageable, projectsIds.getTotalElements());
    }

    public List<ProjectPreviewTo> getAllByAuthor(long userId, boolean visibleOnly) {
        List<Project> projects = visibleOnly ? repository.findAllWithAllInformationByAuthor_IdAndVisibleIsTrue(userId) :
                repository.findAllWithAllInformationByAuthor_Id(userId);
        projects.forEach(this::localizeArchitecture);
        projects.sort(Comparator.comparingInt((Project p) -> p.getPriority().ordinal())
                .thenComparing(Comparator.comparing(Project::getCreated).reversed()));
        sortTechnologies(projects);
        Map<Long, Integer> commentsCountByProjects = getCommentsCountByProjects(projects);
        return projectUtil.asPreviewTos(projects, commentsCountByProjects);
    }

    public Page<ProjectPreviewTo> getAllVisible(Pageable pageable) {
        Assert.notNull(pageable, "pageable must not be null");
        Page<Long> projectsIds = repository.findAllIdsByVisibleIsTrue(pageable);
        List<Project> projects =
                repository.findAllWithArchitectureAndAuthorAndTechnologiesAndLikesByIdIn(projectsIds.getContent(),
                        pageable.getSort());
        projects.forEach(this::localizeArchitecture);
        sortTechnologies(projects);
        Map<Long, Integer> commentsCountByProjects = getCommentsCountByProjects(projects);
        List<ProjectPreviewTo> projectPreviewTos = projectUtil.asPreviewTos(projects, commentsCountByProjects);
        return new PageImpl<>(projectPreviewTos, pageable, projectsIds.getTotalElements());
    }

    public Page<ProjectPreviewTo> getAllVisibleByKeyword(String keyword, Pageable pageable) {
        Assert.notNull(keyword, "keyword must not be null");
        Assert.notNull(pageable, "pageable must not be null");
        Page<Long> projectsIds = repository.findAllVisibleIdsByKeyword(keyword, pageable);
        List<Project> projects =
                repository.findAllWithArchitectureAndAuthorAndTechnologiesAndLikesByIdIn(projectsIds.getContent(),
                        pageable.getSort());
        projects.forEach(this::localizeArchitecture);
        Map<Long, Integer> commentsCountByProjects = getCommentsCountByProjects(projects);
        List<ProjectPreviewTo> projectPreviewTos = projectUtil.asPreviewTos(projects, commentsCountByProjects);
        return new PageImpl<>(projectPreviewTos, pageable, projectsIds.getTotalElements());
    }

    public Page<ProjectPreviewTo> getAllVisibleOrderByPopularity(Pageable pageable) {
        Assert.notNull(pageable, "pageable must not be null");
        Page<Long> projectsIds = repository.findAllIdsOrderByPopularity(pageable);
        Map<Long, Project> projectsByIds = repository
                .findAllWithArchitectureAndAuthorAndTechnologiesAndLikesByIdIn(projectsIds.getContent(), Sort.unsorted())
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        List<Project> projects = projectsIds.stream()
                .map(projectsByIds::get)
                .toList();
        projects.forEach(this::localizeArchitecture);
        sortTechnologies(projects);
        Map<Long, Integer> commentsCountByProjects = getCommentsCountByProjects(projects);
        List<ProjectPreviewTo> projectPreviewTos = projectUtil.asPreviewTos(projects, commentsCountByProjects);
        return new PageImpl<>(projectPreviewTos, pageable, projectsIds.getTotalElements());
    }

    public Page<ProjectPreviewTo> getAllVisibleByTag(String tag, Pageable pageable) {
        Assert.notNull(tag, "tag must not be null");
        Assert.notNull(pageable, "pageable must not be null");
        Page<Long> projectsIds = repository.findAllIdsByTag(tag, pageable);
        List<Project> projects =
                repository.findAllWithArchitectureAndAuthorAndTechnologiesAndLikesByIdIn(projectsIds.getContent(),
                        pageable.getSort());
        projects.forEach(this::localizeArchitecture);
        sortTechnologies(projects);
        Map<Long, Integer> commentsCountByProjects = getCommentsCountByProjects(projects);
        List<ProjectPreviewTo> projectPreviewTos = projectUtil.asPreviewTos(projects, commentsCountByProjects);
        return new PageImpl<>(projectPreviewTos, pageable, projectsIds.getTotalElements());
    }

    private void sortTechnologies(List<Project> projects) {
        projects.forEach(project -> {
            Comparator<Technology> usageThenPriorityThenNaturalComparator = Comparator
                    .comparingInt((Technology t) -> t.getUsage().ordinal())
                    .thenComparing(t -> t.getPriority().ordinal())
                    .thenComparing(Comparator.naturalOrder());
            TreeSet<Technology> sortedTechnologies = new TreeSet<>(usageThenPriorityThenNaturalComparator);
            sortedTechnologies.addAll(project.getTechnologies());
            project.setTechnologies(sortedTechnologies);
        });
    }

    @Transactional
    public void addViewsToProject(long id) {
        Project project = repository.findForAddViewsById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "error.notfound.entity", new Object[]{id}));
        project.setViews(project.getViews() + 1);
    }

    @Transactional
    public void delete(long id, long userId, boolean byAdmin) {
        Project project = repository.findWithDescriptionAndAuthorById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "error.notfound.entity", new Object[]{id}));
        if (project.getAuthor().id() == userId || byAdmin) {
            repository.delete(project);
            repository.flush();
            likeRepository.deleteAllByObjectId(id);
            likeRepository.flush();
            FileUtil.deleteFile(project.getLogo().getFileLink());
            FileUtil.deleteFile(project.getPreview().getFileLink());
            if (project.getDockerCompose() != null) {
                FileUtil.deleteFile(project.getDockerCompose().getFileLink());
            }
            project.getDescriptionElements().stream()
                    .filter(de -> de.getType() == IMAGE && de.getImage() != null)
                    .forEach(de -> FileUtil.deleteFile(de.getImage().getFileLink()));
        } else {
            throw new IllegalRequestDataException("Forbidden to delete another user project, projectId=" + id +
                    ", userId=" + userId, "project.forbidden-delete-not-belong", null);
        }
    }


    @Transactional
    public void hide(long id, boolean visible, long userId, boolean byAdmin) {
        Project project = getWithAuthor(id);
        if (project.getAuthor().id() == userId || byAdmin) {
            project.setVisible(visible);
        } else {
            throw new IllegalRequestDataException("Forbidden to reveal/hide another user project, projectId=" + id +
                    ", userId=" + userId, "project.forbidden-hide-not-belong", null);
        }
    }

    @Transactional
    public Project create(ProjectTo projectTo, long userId) {
        Assert.notNull(projectTo, "projectTo must not be null");
        if (projectTo.getLogo() == null || projectTo.getLogo().isEmpty()) {
            throw new IllegalRequestDataException("Project logo file is not present",
                    "project.logo-not-present", null);
        }
        if (projectTo.getPreview() == null || projectTo.getPreview().isEmpty()) {
            throw new IllegalRequestDataException("Project preview file is not present",
                    "project.preview-not-present", null);
        }
        User author = userService.get(userId);
        Project project = repository.saveAndFlush(projectUtil.createNewFromTo(projectTo, author));

        uploadFile(projectTo.getLogo(), author.getEmail(), project.getName(), LOGO_DIR, projectTo.getLogo().getRealFileName());
        uploadFile(projectTo.getPreview(), author.getEmail(), project.getName(), PREVIEW_DIR, projectTo.getPreview().getRealFileName());
        if (projectTo.getDockerCompose() != null && !projectTo.getDockerCompose().isEmpty()) {
            uploadFile(projectTo.getDockerCompose(), author.getEmail(), project.getName(), DOCKER_DIR,
                    projectTo.getDockerCompose().getRealFileName());
        }
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && deTo.getImage() != null)
                .forEach(deTo -> uploadDeImage(deTo, author.getEmail(), project.getName()));
        return project;
    }

    @Transactional
    public Project update(ProjectTo projectTo, long userId, boolean byAdmin) {
        Assert.notNull(projectTo, "projectTo must not be null");
        Project project = repository.findWithAllInformationAndDescriptionById(projectTo.getId()).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + projectTo.getId(), "error.notfound.entity",
                        new Object[]{projectTo.getId()}));
        if (project.getAuthor().id() != userId && !byAdmin) {
            throw new IllegalRequestDataException("Forbidden to edit another user project, projectId=" + projectTo.getId() +
                    ", userId=" + userId, "project.forbidden-edit-not-belong", null);
        }
        String authorEmail = project.getAuthor().getEmail();
        String projectOldName = project.getName();
        String oldLogoFileLink = project.getLogo().getFileLink();
        String oldPreviewFileLink = project.getPreview().getFileLink();
        String oldDockerComposeFileLink =
                project.getDockerCompose() != null ? project.getDockerCompose().getFileLink() : null;
        Map<Long, DescriptionElement> oldDeImages = project.getDescriptionElements().stream()
                .filter(de -> de.getType() == IMAGE)
                .map(DescriptionElement::new)
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        repository.saveAndFlush(projectUtil.updateFromTo(project, projectTo));

        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && deTo.isNew() && deTo.getImage() != null)
                .forEach(deTo -> uploadDeImage(deTo, authorEmail, project.getName()));
        oldDeImages.values().stream()
                .filter(oldDeImage -> !project.getDescriptionElements().contains(oldDeImage))
                .forEach(oldDeImage -> FileUtil.deleteFile(oldDeImage.getImage().getFileLink()));
        projectTo.getDescriptionElementTos().stream()
                .filter(deTo -> deTo.getType() == IMAGE && !deTo.isNew())
                .forEach(deTo -> {
                    if (deTo.getImage() != null && !deTo.getImage().isEmpty()) {
                        uploadDeImage(deTo, authorEmail, project.getName());
                        FileUtil.deleteFile(oldDeImages.get(deTo.getId()).getImage().getFileLink());
                    } else if (!project.getName().equalsIgnoreCase(projectOldName)) {
                        FileUtil.moveFile(oldDeImages.get(deTo.getId()).getImage().getFileLink(), projectFilesPath +
                                FileUtil.normalizePath(authorEmail + "/" + project.getName() + DESCRIPTION_IMG_DIR));
                    }
                });

        updateProjectFileIfNecessary(projectTo.getLogo(), oldLogoFileLink, project.getLogo().getFileLink(),
                authorEmail, project.getName(), projectOldName, LOGO_DIR);
        updateProjectFileIfNecessary(projectTo.getPreview(), oldPreviewFileLink, project.getPreview().getFileLink(),
                authorEmail, project.getName(), projectOldName, PREVIEW_DIR);
        updateProjectFileIfNecessary(projectTo.getDockerCompose(), oldDockerComposeFileLink,
                projectTo.getDockerCompose() != null ? projectTo.getDockerCompose().getFileLink() : null,
                authorEmail, project.getName(), projectOldName, DOCKER_DIR);
        return project;
    }

    private void updateProjectFileIfNecessary(FileTo fileTo, String oldFileLink, String currentFileLink,
                                              String authorEmail, String projectName, String projectOldName, String dirName) {
        if (fileTo != null && !fileTo.isEmpty()) {
            if (oldFileLink != null && !oldFileLink.equalsIgnoreCase(currentFileLink)) {
                FileUtil.deleteFile(oldFileLink);
            }
            FileUtil.upload(fileTo, projectFilesPath + FileUtil.normalizePath(authorEmail + "/" + projectName + dirName),
                    FileUtil.normalizePath(fileTo.getRealFileName()));
        } else if (!projectName.equalsIgnoreCase(projectOldName)) {
            FileUtil.moveFile(oldFileLink, projectFilesPath + FileUtil.normalizePath(authorEmail + "/" + projectName + dirName));
        }
    }

    private void uploadDeImage(DescriptionElementTo deTo, String authorEmail, String projectName) {
        FileTo image = deTo.getImage();
        String uniquePrefixFileName = image.getFileLink().substring(image.getFileLink().lastIndexOf('/') + 1);
        uploadFile(image, authorEmail, projectName, DESCRIPTION_IMG_DIR, uniquePrefixFileName);
    }

    private void uploadFile(FileTo fileTo, String authorEmail, String projectName, String dirName, String fileName) {
        FileUtil.upload(fileTo, projectFilesPath + FileUtil.normalizePath(authorEmail + "/" + projectName + dirName),
                FileUtil.normalizePath(fileName));
    }

    public void likeProject(long id, boolean liked, long userId) {
        Project project = getWithAuthor(id);
        if (project.getAuthor().id() == userId) {
            throw new IllegalRequestDataException("Forbidden to like yourself project, userId=" + userId +
                    ", projectId=" + id, "project.forbidden-like-yourself", null);
        }
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
        User author = userService.get(userId);
        get(commentTo.getProjectId());
        if (commentTo.getParentId() != null) {
            commentRepository.getExisted(commentTo.getParentId());
        }
        return commentRepository.save(new Comment(null, commentTo.getProjectId(), author, commentTo.getParentId(),
                commentTo.getText()));
    }

    public void likeComment(long commentId, boolean liked, long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Not found comment with id=" + commentId, "error.notfound.entity", new Object[]{commentId}));
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
                new NotFoundException("Not found comment with id=" + commentId, "error.notfound.entity", new Object[]{commentId}));
        if (comment.getAuthor().id() == userId || byAdmin) {
            comment.setDeleted(true);
        } else {
            throw new IllegalRequestDataException("Forbidden to delete another user comment, commentId=" + commentId +
                    ", userId=" + userId, "comment.forbidden-delete-not-belong", null);
        }
    }

    @Transactional
    public void updateComment(long commentId, String text, long userId) {
        Assert.notNull(text, "text must not be null");
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Not found comment with id=" + commentId, "error.notfound.entity", new Object[]{commentId}));
        if (comment.getAuthor().id() == userId) {
            comment.setText(text);
        } else {
            throw new IllegalRequestDataException("Forbidden to edit another user comment, commentId=" + commentId +
                    ", userId=" + userId, "comment.forbidden-edit-not-belong", null);
        }
    }

    public Map<Long, Integer> getCommentsCountByProjects(List<Project> projects) {
        List<Long> projectsIds = projects.stream()
                .map(BaseEntity::getId)
                .toList();
        Map<Long, Integer> commentsCountByProjects = commentRepository.countCommentsByProjects(projectsIds).stream()
                .collect(Collectors.toMap(CommentCount::getProjectId, CommentCount::getCommentsCount));
        projectsIds.forEach(projectId -> commentsCountByProjects.computeIfAbsent(projectId, k -> 0));
        return commentsCountByProjects;
    }

    public int countLikesForAuthor(long authorId) {
        return likeRepository.countLikesForAuthor(authorId);
    }

    private Project getWithAuthor(long id) {
        return repository.findWithAuthorById(id).orElseThrow(() ->
                new NotFoundException("Not found project with id=" + id, "error.notfound.entity", new Object[]{id}));
    }
}
