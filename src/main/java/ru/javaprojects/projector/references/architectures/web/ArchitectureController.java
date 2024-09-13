package ru.javaprojects.projector.references.architectures.web;

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
import ru.javaprojects.projector.references.architectures.Architecture;
import ru.javaprojects.projector.references.architectures.ArchitectureService;

@Controller
@RequestMapping(ArchitectureController.ARCHITECTURES_URL)
@AllArgsConstructor
@Slf4j
public class ArchitectureController {
    static final String ARCHITECTURES_URL = "/references/architectures";

    private final ArchitectureService service;
    private final UniqueArchitectureNameValidator nameValidator;
    private final MessageSource messageSource;

    @InitBinder("architecture")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(Model model) {
        log.info("get architectures");
        model.addAttribute("architectures", service.getAll());
        return "references/architectures";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show architecture add form");
        model.addAttribute("architecture", new Architecture());
        return "references/architecture-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for architecture with id={}", id);
        Architecture architecture = service.get(id);
        model.addAttribute("architecture", architecture);
        return "references/architecture-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid Architecture architecture, BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        boolean isNew = architecture.isNew();
        if (result.hasErrors()) {
            if (!isNew) {
                model.addAttribute("architectureName", service.get(architecture.id()).getName());
            }
            return "references/architecture-form";
        }
        log.info("{} {}", isNew ? "create" : "update", architecture);
        if (isNew) {
            service.create(architecture);
        }  else {
            service.update(architecture);
        }
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage((isNew ? "architecture.created" : "architecture.updated"),
                        new Object[]{architecture.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/references/architectures";
    }
}
