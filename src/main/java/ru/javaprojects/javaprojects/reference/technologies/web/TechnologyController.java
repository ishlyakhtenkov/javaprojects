package ru.javaprojects.javaprojects.reference.technologies.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.javaprojects.common.model.Priority;
import ru.javaprojects.javaprojects.common.to.FileTo;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyService;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyTo;
import ru.javaprojects.javaprojects.reference.technologies.model.Technology;
import ru.javaprojects.javaprojects.reference.technologies.model.Usage;

import static ru.javaprojects.javaprojects.reference.technologies.TechnologyUtil.asTo;

@Controller
@RequestMapping(TechnologyController.TECHNOLOGIES_URL)
@AllArgsConstructor
@Slf4j
public class TechnologyController {
    static final String TECHNOLOGIES_URL = "/management/reference/technologies";

    private final TechnologyService service;
    private final UniqueTechnologyNameValidator nameValidator;
    private final MessageSource messageSource;

    @InitBinder("technologyTo")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault @SortDefault("name") Pageable pageable, Model model,
                         RedirectAttributes redirectAttributes) {
        Page<Technology> technologies;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/management/reference/technologies";
            }
            log.info("get technologies by keyword={} (pageNumber={}, pageSize={})", keyword, pageable.getPageNumber(),
                    pageable.getPageSize());
            technologies = service.getAllByKeyword(keyword.trim(), pageable);
        } else  {
            log.info("get technologies (pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            technologies = service.getAll(pageable);
        }
        if (technologies.getContent().isEmpty() && technologies.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/management/reference/technologies";
        }
        model.addAttribute("technologies", technologies);
        return "management/reference/technologies";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show technology add form");
        model.addAttribute("technologyTo", new TechnologyTo());
        addAttributesToModel(model);
        return "management/reference/technology-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for technology with id={}", id);
        Technology technology = service.get(id);
        model.addAttribute("technologyTo", asTo(technology));
        addAttributesToModel(model);
        return "management/reference/technology-form";
    }

    private void addAttributesToModel(Model model) {
        model.addAttribute("usages", Usage.values());
        model.addAttribute("priorities", Priority.values());
    }

    @PostMapping
    public String createOrUpdate(@Valid TechnologyTo technologyTo, BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        boolean isNew = technologyTo.isNew();
        if (result.hasErrors()) {
            addAttributesToModel(model);
            if (technologyTo.getLogo() != null) {
                technologyTo.getLogo().keepInputtedFile(FileTo.IS_IMAGE_FILE, () -> technologyTo.setLogo(null));
            }
            if (!isNew) {
                model.addAttribute("technologyName", service.get(technologyTo.getId()).getName());
            }
            return "management/reference/technology-form";
        }
        log.info("{} {}", isNew ? "create" : "update", technologyTo);
        if (isNew) {
            service.create(technologyTo);
        }  else {
            service.update(technologyTo);
        }
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage((isNew ? "technology.created" : "technology.updated"),
                new Object[]{technologyTo.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/management/reference/technologies";
    }
}
