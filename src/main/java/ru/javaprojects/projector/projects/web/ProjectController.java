package ru.javaprojects.projector.projects.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.projector.app.AuthUser;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.AppUtil;
import ru.javaprojects.projector.common.util.TreeNode;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.ProjectUtil;
import ru.javaprojects.projector.projects.model.Comment;
import ru.javaprojects.projector.projects.model.Like;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.to.ProjectTo;
import ru.javaprojects.projector.reference.architectures.ArchitectureService;
import ru.javaprojects.projector.reference.technologies.TechnologyService;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.reference.technologies.model.Usage;

import java.util.*;
import java.util.stream.Collectors;

import static ru.javaprojects.projector.projects.model.ElementType.IMAGE;

@Controller
@RequestMapping(value = ProjectController.PROJECTS_URL)
@AllArgsConstructor
@Slf4j
public class ProjectController {
    static final String PROJECTS_URL = "/projects";

    private final ProjectService projectService;
    private final ArchitectureService architectureService;
    private final TechnologyService technologyService;
    private final UniqueProjectNameValidator nameValidator;
    private final MessageSource messageSource;
    private final ProjectUtil projectUtil;

    @InitBinder("projectTo")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/{id}/data")
    public String showProjectData(@PathVariable long id, Model model) {
        log.info("show data for project with id={}", id);
        Project project = projectService.getWithAllInformation(id, Comparator.naturalOrder());
        if (project.getAuthor().getId() != AuthUser.authId() && !AuthUser.isAdmin()) {
            throw new IllegalRequestDataException("Forbidden to view another user project data, projectId=" + id +
                    ", userId=" + AuthUser.authId(), "project.forbidden-view-data-not-belong", null);
        }
        model.addAttribute("project", projectService.getWithAllInformation(id, Comparator.naturalOrder()));
        return "projects/project-data";
    }

    @GetMapping("/{id}/view")
    public String showProject(@PathVariable long id, Model model) {
        log.info("show project with id={}", id);
        Comparator<Technology> technologyComparator = Comparator
                .comparingInt((Technology t) -> t.getPriority().ordinal())
                .thenComparing(Technology::getName);
        Project project = projectService.getWithAllInformation(id, technologyComparator);
        AuthUser authUser = AuthUser.safeGet();
        if (!project.isVisible() && (authUser == null || project.getAuthor().getId() != AuthUser.authId() && !AuthUser.isAdmin())) {
            throw new IllegalRequestDataException("Forbidden to view disabled project, projectId=" + id,
                    "project.forbidden-view-hided", null);
        }
        projectService.addViewsToProject(id);
        project.setViews(project.getViews() + 1);

        boolean hasFrontendTechnologies = project.getTechnologies().stream()
                .anyMatch(technology -> technology.getUsage() == Usage.FRONTEND);
        if (authUser != null) {
            long authId = authUser.id();
            Set<Long> likedCommentsIds = project.getComments().stream()
                    .flatMap(comment -> comment.getLikes().stream())
                    .filter(like -> like.getUserId() == authId)
                    .map(Like::getObjectId)
                    .collect(Collectors.toSet());
            model.addAttribute("likedCommentsIds", likedCommentsIds);
            boolean liked = project.getLikes().stream()
                    .anyMatch(like -> like.getUserId() == authId);
            model.addAttribute("liked", liked);
        }
        model.addAttribute("project", project);
        model.addAttribute("hasFrontendTechnologies", hasFrontendTechnologies);
        model.addAttribute("comments", sortCommentsAsTreeWithIndents(project.getComments()));
        return "projects/project";
    }

    private Map<Comment, Integer> sortCommentsAsTreeWithIndents(List<Comment> comments) {
        List<Comment> sortedComments = sortCommentsAsTree(comments);
        Map<Comment, Integer> commentIndents = new LinkedHashMap<>();
        Map<Long, Integer> parentIndents = new HashMap<>();
        sortedComments.forEach(comment -> {
            if (comment.getParentId() == null) {
                commentIndents.put(comment, 0);
                parentIndents.put(comment.getId(), 0);
            } else {
                Integer parentIndent = parentIndents.get(comment.getParentId());
                int indent = parentIndent != null ? parentIndent + 1 : 0;
                commentIndents.put(comment, indent);
                parentIndents.put(comment.getId(), indent);
            }
        });
        return commentIndents;
    }

    private List<Comment> sortCommentsAsTree(List<Comment> comments) {
        List<CommentTreeNode> roots = AppUtil.makeTree(comments, CommentTreeNode::new);
        List<Comment> sortedComments = new ArrayList<>();
        roots.forEach(root -> {
            sortedComments.add(root.comment);
            List<CommentTreeNode> subNodes = root.subNodes();
            LinkedList<CommentTreeNode> stack = new LinkedList<>(subNodes);
            while (!stack.isEmpty()) {
                CommentTreeNode node = stack.poll();
                sortedComments.add(node.comment);
                node.subNodes().forEach(stack::addFirst);
            }
        });
        return sortedComments;
    }

    private record CommentTreeNode(Comment comment, List<CommentTreeNode> subNodes) implements TreeNode<Comment, CommentTreeNode> {
        public CommentTreeNode(Comment comment) {
            this(comment, new LinkedList<>());
        }
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show project add form");
        model.addAttribute("projectTo", new ProjectTo());
        addAttributesToModel(model);
        return "projects/project-form";
    }

    private void addAttributesToModel(Model model) {
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("architectures", architectureService.getAll());
        model.addAttribute("technologies", technologyService.getAll());
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for project with id={}", id);
        Project project = projectService.getWithAllInformation(id, Comparator.naturalOrder());
        if (project.getAuthor().getId() != AuthUser.authId() && !AuthUser.isAdmin()) {
            throw new IllegalRequestDataException("Forbidden to edit another user project, projectId=" + id +
                    ", userId=" + AuthUser.authId(), "project.forbidden-edit-not-belong", null);
        }
        model.addAttribute("projectTo", projectUtil.asTo(project));
        addAttributesToModel(model);
        return "projects/project-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid ProjectTo projectTo, BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        boolean isNew = projectTo.isNew();
        if (result.hasErrors()) {
            addAttributesToModel(model);
            if (projectTo.getLogo() != null) {
                projectTo.getLogo().keepInputtedFile(FileTo.IS_IMAGE_FILE, () -> projectTo.setLogo(null));
            }
            if (projectTo.getPreview() != null) {
                projectTo.getPreview().keepInputtedFile(FileTo.IS_IMAGE_FILE, () -> projectTo.setPreview(null));
            }
            if (projectTo.getDockerCompose() != null) {
                projectTo.getDockerCompose().keepInputtedFile(FileTo.IS_YAML_FILE, () -> projectTo.setDockerCompose(null));
            }
            projectTo.getDescriptionElementTos().stream()
                    .filter(deTo -> deTo.getType() == IMAGE && deTo.getImage() != null)
                    .forEach(deTo -> deTo.getImage().keepInputtedFile(FileTo.IS_IMAGE_FILE, () -> deTo.setImage(null)));
            if (!isNew) {
                model.addAttribute("projectName", projectService.get(projectTo.getId()).getName());
            }
            return "projects/project-form";
        }
        log.info("{} {}", isNew ? "create" : "update", projectTo);
        Project project = isNew ? projectService.create(projectTo, AuthUser.authId()) :
                projectService.update(projectTo, AuthUser.authId(), AuthUser.isAdmin());
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage((isNew ? "project.created" : "project.updated"),
                        new Object[]{projectTo.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/projects/" + project.getId() + "/data";
    }
}
