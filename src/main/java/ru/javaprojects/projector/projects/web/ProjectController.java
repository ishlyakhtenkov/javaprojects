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
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.ProjectUtil;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.to.DescriptionElementTo;
import ru.javaprojects.projector.projects.to.ProjectTo;
import ru.javaprojects.projector.references.architectures.ArchitectureService;
import ru.javaprojects.projector.references.technologies.TechnologyService;
import ru.javaprojects.projector.references.technologies.TechnologyTo;

import java.io.IOException;

import static ru.javaprojects.projector.projects.model.ElementType.IMAGE;


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
        return "projects/projects";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get project with id={}", id);
        model.addAttribute("project", projectService.getWithTechnologiesAndDescription(id, true));
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
        Project project = projectService.getWithTechnologiesAndDescription(id, true);
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
            if (projectTo.getLogo().getInputtedFile() != null && !projectTo.getLogo().getInputtedFile().isEmpty()) {
                keepInputtedFile(projectTo.getLogo());
            }
            if (projectTo.getCardImage().getInputtedFile() != null && !projectTo.getCardImage().getInputtedFile().isEmpty()) {
                keepInputtedFile(projectTo.getCardImage());
            }
            if (!isNew) {
                model.addAttribute("projectName", projectService.get(projectTo.getId()).getName());
            }
            projectTo.getDescriptionElementTos().stream()
                    .filter(de -> de.getType() == IMAGE && de.getImage() != null &&
                            (de.getImage().getInputtedFile() != null && !de.getImage().getInputtedFile().isEmpty()))
                    .forEach(this::keepInputtedFile);
            return "projects/project-form";
        }
        log.info("{} {}", isNew ? "create" : "update", projectTo);
        Project project = isNew ? projectService.create(projectTo) : projectService.update(projectTo);
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage((isNew ? "project.created" : "project.updated"),
                        new Object[]{projectTo.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/projects/" + project.getId();
    }

    private void keepInputtedFile(DescriptionElementTo descriptionElementTo) {
        try {
            FileTo image = descriptionElementTo.getImage();
            image.setInputtedFileBytes(image.getInputtedFile().getBytes());
            image.setFileName(image.getInputtedFile().getOriginalFilename());
            image.setFileLink(null);
        } catch (IOException e) {
            throw new IllegalRequestDataException(e.getMessage(), "file.failed-to-upload",
                    new Object[]{descriptionElementTo.getImage().getInputtedFile().getOriginalFilename()});
        }
    }

    private void keepInputtedFile(FileTo fileTo) {
        try {
            fileTo.setInputtedFileBytes(fileTo.getInputtedFile().getBytes());
            fileTo.setFileName(fileTo.getInputtedFile().getOriginalFilename());
            fileTo.setFileLink(null);
        } catch (IOException e) {
            throw new IllegalRequestDataException(e.getMessage(), "file.failed-to-upload",
                    new Object[]{fileTo.getInputtedFile().getOriginalFilename()});
        }
    }
}
