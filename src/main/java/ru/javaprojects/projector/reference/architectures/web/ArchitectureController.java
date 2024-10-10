package ru.javaprojects.projector.reference.architectures.web;

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
import ru.javaprojects.projector.common.util.Util;
import ru.javaprojects.projector.reference.architectures.Architecture;
import ru.javaprojects.projector.reference.architectures.ArchitectureService;
import ru.javaprojects.projector.reference.architectures.ArchitectureTo;

import static ru.javaprojects.projector.reference.architectures.ArchitectureUtil.asTo;

@Controller
@RequestMapping(ArchitectureController.ARCHITECTURES_URL)
@AllArgsConstructor
@Slf4j
public class ArchitectureController {
    static final String ARCHITECTURES_URL = "/management/reference/architectures";

    private final ArchitectureService service;
    private final UniqueArchitectureNameValidator nameValidator;
    private final MessageSource messageSource;

    @InitBinder("architectureTo")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(Model model) {
        log.info("get architectures");
        model.addAttribute("architectures", service.getAll());
        return "management/reference/architectures";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show architecture add form");
        model.addAttribute("architectureTo", new ArchitectureTo());
        return "management/reference/architecture-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for architecture with id={}", id);
        Architecture architecture = service.get(id);
        model.addAttribute("architectureTo", asTo(architecture));
        return "management/reference/architecture-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid ArchitectureTo architectureTo, BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        boolean isNew = architectureTo.isNew();
        if (result.hasErrors()) {
            Util.keepInputtedFile(architectureTo.getLogo(), Util.IS_IMAGE_FILE,  () -> architectureTo.setLogo(null));
            if (!isNew) {
                model.addAttribute("architectureName", service.get(architectureTo.getId()).getName());
            }
            return "management/reference/architecture-form";
        }
        log.info("{} {}", isNew ? "create" : "update", architectureTo);
        if (isNew) {
            service.create(architectureTo);
        }  else {
            service.update(architectureTo);
        }
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage((isNew ? "architecture.created" : "architecture.updated"),
                        new Object[]{architectureTo.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/management/reference/architectures";
    }
}
