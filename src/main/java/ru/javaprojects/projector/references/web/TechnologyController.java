package ru.javaprojects.projector.references.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.projector.references.service.TechnologyService;
import ru.javaprojects.projector.references.model.Technology;

@Controller
@RequestMapping(TechnologyController.TECHNOLOGIES_URL)
@AllArgsConstructor
@Slf4j
public class TechnologyController {
    static final String TECHNOLOGIES_URL = "/references/technologies";

    private final TechnologyService service;

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault Pageable pageable, Model model, RedirectAttributes redirectAttributes) {
        Page<Technology> technologies;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/references/technologies";
            }
            log.info("get technologies (pageNumber={}, pageSize={}, keyword={})", pageable.getPageNumber(),
                    pageable.getPageSize(), keyword);
            technologies = service.getAll(pageable, keyword.trim());
        } else  {
            log.info("get technologies (pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            technologies = service.getAll(pageable);
        }
        if (technologies.getContent().isEmpty() && technologies.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/references/technologies";
        }
        model.addAttribute("technologies", technologies);
        return "references/technologies";
    }

}
