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
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.ProjectTo;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.references.architectures.ArchitectureService;
import ru.javaprojects.projector.references.technologies.TechnologyService;

import static ru.javaprojects.projector.projects.ProjectUtil.asTo;

@Controller
@RequestMapping(ProjectController.PROJECTS_URL)
@AllArgsConstructor
@Slf4j
public class ProjectController {
    static final String PROJECTS_URL = "/projects";

    private final ProjectService projectService;
    private final ArchitectureService architectureService;
    private final TechnologyService technologyService;
    private final UniqueProjectNameValidator nameValidator;
    private final MessageSource messageSource;

    @InitBinder("projectTo")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(Model model) {
        log.info("get projects");
        model.addAttribute("projects", projectService.getAll());
        return "projects/projects";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get project with id={}", id);
        model.addAttribute("project", projectService.getWithTechnologies(id));
        return "projects/project";
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
        Project project = projectService.getWithTechnologies(id);
        model.addAttribute("projectTo", asTo(project));
        model.addAttribute("logoFile", project.getLogoFile());
        model.addAttribute("dockerComposeFile", project.getDockerComposeFile());
        model.addAttribute("cardImageFile", project.getCardImageFile());
        addAttributesToModel(model);
        return "projects/project-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid ProjectTo projectTo, BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        boolean isNew = projectTo.isNew();
        if (result.hasErrors()) {
            addAttributesToModel(model);
            if (!isNew) {
                Project project = projectService.get(projectTo.getId());
                if (projectTo.getLogoFile() == null) {
                    model.addAttribute("logoFile", project.getLogoFile());
                }
                if (projectTo.getCardImageFile() == null) {
                    model.addAttribute("cardImageFile", project.getCardImageFile());
                }
                if (projectTo.getDockerComposeFile() == null) {
                    model.addAttribute("dockerComposeFile", project.getDockerComposeFile());
                }
            }
            return "projects/project-form";
        }
        log.info((isNew ? "create" : "update") + " {}", projectTo);
        Project project = isNew ? projectService.create(projectTo) : projectService.update(projectTo);
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage((isNew ? "project.created" : "project.updated"),
                        new Object[]{projectTo.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/projects/" + project.getId();
    }
}
