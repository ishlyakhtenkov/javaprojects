package ru.javaprojects.javaprojects.projects.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.javaprojects.projects.ProjectService;
import ru.javaprojects.javaprojects.projects.to.ProjectPreviewTo;


@Controller
@RequestMapping(ProjectManagementController.MANAGEMENT_PROJECTS_URL)
@AllArgsConstructor
@Slf4j
public class ProjectManagementController {
    static final String MANAGEMENT_PROJECTS_URL = "/management/projects";

    private final ProjectService service;

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault @SortDefault("name") Pageable p, Model model,
                         RedirectAttributes redirectAttributes) {
        Page<ProjectPreviewTo> projects;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:" + MANAGEMENT_PROJECTS_URL;
            }
            log.info("get projects by keyword={} (pageNumber={}, pageSize={})", keyword, p.getPageNumber(), p.getPageSize());
            projects = service.getAllByKeyword(keyword.trim(), p);
        } else  {
            log.info("get projects (pageNumber={}, pageSize={})", p.getPageNumber(), p.getPageSize());
            projects = service.getAll(p);
        }
        if (projects.getContent().isEmpty() && projects.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:" + MANAGEMENT_PROJECTS_URL;
        }
        model.addAttribute("projects", projects);
        return "management/projects/projects";
    }
}
