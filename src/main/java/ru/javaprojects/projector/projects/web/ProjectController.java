package ru.javaprojects.projector.projects.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.javaprojects.projector.common.util.TreeNode;
import ru.javaprojects.projector.common.util.AppUtil;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Comment;
import ru.javaprojects.projector.projects.model.Like;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.reference.technologies.model.Usage;
import ru.javaprojects.projector.app.AuthUser;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.info("Show home page");
        List<Project> projects = projectService.getAllEnabledWithArchitectureAndTechnologiesAndLikes();
        if (AuthUser.safeGet() != null) {
            long authId = AuthUser.authId();
            Set<Long> likedProjectsIds = projects.stream()
                    .flatMap(project -> project.getLikes().stream())
                    .filter(like -> like.getUserId() == authId)
                    .map(Like::getObjectId)
                    .collect(Collectors.toSet());
            model.addAttribute("likedProjectsIds", likedProjectsIds);
        }
        model.addAttribute("projects", projects);
        model.addAttribute("commentsTotal", projectService.getTotalCommentsByProject());
        return "index";
    }

    @GetMapping("/projects/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get project with id={}", id);
        Comparator<Technology> technologyComparator = Comparator
                .comparingInt((Technology t) -> t.getPriority().ordinal())
                .thenComparing(Technology::getName);
        projectService.addViewsToProject(id);
        Project project = projectService.getWithAllInformation(id, technologyComparator);
        boolean hasFrontendTechnologies = project.getTechnologies().stream()
                .anyMatch(technology -> technology.getUsage() == Usage.FRONTEND);
        if (AuthUser.safeGet() != null) {
            long authId = AuthUser.authId();
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
        return "project";
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
}
