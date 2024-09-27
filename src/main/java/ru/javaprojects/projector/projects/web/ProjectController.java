package ru.javaprojects.projector.projects.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Like;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.reference.technologies.model.Usage;
import ru.javaprojects.projector.users.AuthUser;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.info("Show home page");
        List<Project> projects = projectService.getAllEnabledWithArchitectureAndTechnologies();
        if (AuthUser.safeGet() != null) {
            long authId = AuthUser.authId();
            Set<Long> likedProjectsIds = projects.stream()
                    .flatMap(project -> project.getLikes().stream())
                    .filter(like -> like.getUserId() == authId)
                    .map(Like::getProjectId)
                    .collect(Collectors.toSet());
            model.addAttribute("likedProjectsIds", likedProjectsIds);
        }
        model.addAttribute("projects", projects);
        return "index";
    }

    @GetMapping("/projects/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get project with id={}", id);
        Comparator<Technology> technologyComparator = Comparator
                .comparingInt((Technology t) -> t.getPriority().ordinal())
                .thenComparing(Technology::getName);
        projectService.addViewsToProject(id);
        Project project = projectService.getWithTechnologiesAndDescription(id, technologyComparator);
        boolean hasFrontendTechnologies = project.getTechnologies().stream()
                .anyMatch(technology -> technology.getUsage() == Usage.FRONTEND);
        model.addAttribute("project", project);
        model.addAttribute("hasFrontendTechnologies", hasFrontendTechnologies);
        return "project";
    }
}
