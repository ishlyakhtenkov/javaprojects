package ru.javaprojects.projector.projects.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.reference.technologies.model.Usage;

@Controller
@RequestMapping(ProjectController.PROJECTS_URL)
@AllArgsConstructor
@Slf4j
public class ProjectController {
    static final String PROJECTS_URL = "/projects";

    private final ProjectService projectService;

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get project with id={}", id);
        Project project = projectService.getWithTechnologiesAndDescription(id, true);
        boolean hasFrontendTechnologies = project.getTechnologies().stream()
                .anyMatch(technology -> technology.getUsage() == Usage.FRONTEND);
        model.addAttribute("project", project);
        model.addAttribute("hasFrontendTechnologies", hasFrontendTechnologies);
        return "project";
    }
}
