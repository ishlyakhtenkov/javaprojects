package ru.javaprojects.projector.projects.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.reference.technologies.model.Technology;
import ru.javaprojects.projector.reference.technologies.model.Usage;

import java.util.Comparator;

@Controller
@AllArgsConstructor
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.info("Show home page");
        model.addAttribute("projects", projectService.getAllEnabledWithArchitectureAndTechnologies());
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
