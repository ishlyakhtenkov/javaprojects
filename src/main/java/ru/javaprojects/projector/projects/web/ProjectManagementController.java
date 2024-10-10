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
import ru.javaprojects.projector.common.util.Util;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.ProjectUtil;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.to.ProjectTo;
import ru.javaprojects.projector.reference.architectures.ArchitectureService;
import ru.javaprojects.projector.reference.technologies.TechnologyService;

import java.util.Comparator;

import static ru.javaprojects.projector.projects.model.ElementType.IMAGE;


@Controller
@RequestMapping(ProjectManagementController.PROJECT_MANAGEMENT_URL)
@AllArgsConstructor
@Slf4j
public class ProjectManagementController {
    static final String PROJECT_MANAGEMENT_URL = "/management/projects";

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

    @GetMapping
    public String getAll(Model model) {
        log.info("get projects");
        model.addAttribute("projects", projectService.getAll());
        return "management/projects/projects";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get project with id={}", id);
        model.addAttribute("project", projectService.getWithAllInformation(id, Comparator.naturalOrder()));
        return "management/projects/project";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show project add form");
        model.addAttribute("projectTo", new ProjectTo());
        addAttributesToModel(model);
        return "management/projects/project-form";
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
        model.addAttribute("projectTo", projectUtil.asTo(project));
        addAttributesToModel(model);
        return "management/projects/project-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid ProjectTo projectTo, BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        boolean isNew = projectTo.isNew();
        if (result.hasErrors()) {
            addAttributesToModel(model);
            Util.keepInputtedFile(projectTo.getLogo(), Util.IS_IMAGE_FILE,  () -> projectTo.setLogo(null));
            Util.keepInputtedFile(projectTo.getCardImage(), Util.IS_IMAGE_FILE,  () -> projectTo.setCardImage(null));
            Util.keepInputtedFile(projectTo.getDockerCompose(), Util.IS_YAML_FILE,  () -> projectTo.setDockerCompose(null));
            projectTo.getDescriptionElementTos().stream()
                    .filter(deTo -> deTo.getType() == IMAGE)
                    .forEach(deTo -> Util.keepInputtedFile(deTo.getImage(), Util.IS_IMAGE_FILE, () -> deTo.setImage(null)));
            if (!isNew) {
                model.addAttribute("projectName", projectService.get(projectTo.getId()).getName());
            }
            return "management/projects/project-form";
        }
        log.info("{} {}", isNew ? "create" : "update", projectTo);
        Project project = isNew ? projectService.create(projectTo) : projectService.update(projectTo);
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage((isNew ? "project.created" : "project.updated"),
                        new Object[]{projectTo.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/management/projects/" + project.getId();
    }
}
