package ru.javaprojects.projector.projects.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.projector.projects.ProjectService;


@Controller
@RequestMapping(ProjectManagementController.MANAGEMENT_PROJECTS_URL)
@AllArgsConstructor
@Slf4j
public class ProjectManagementController {
    static final String MANAGEMENT_PROJECTS_URL = "/management/projects";

    private final ProjectService projectService;

    @GetMapping
    public String getAll(Model model) {
        log.info("get projects");
        model.addAttribute("projects", projectService.getAll());
        return "management/projects/projects";
    }
}
