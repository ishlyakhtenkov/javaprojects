package ru.javaprojects.projector.home.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;

import java.util.List;

@Controller
@AllArgsConstructor
@Slf4j
public class HomeController {
    static final Pageable FIRST_PAGE = PageRequest.of(0, 9);

    private final ProjectService projectService;

    @GetMapping("/")
    public String showHomePage(@RequestParam(required = false) String popular,
                               @RequestParam(name = "by-author", required = false) Long authorId, Model model) {
        List<ProjectPreviewTo> projects;
        if (popular != null) {
            log.info("Show home page with popular projects");
            projects = projectService.getAllVisibleOrderByPopularity(FIRST_PAGE).getContent();
        } else if (authorId != null) {
            log.info("Show home page with projects by author with id =" + authorId);
            projects = projectService.getAllByAuthor(authorId, true);
            model.addAttribute("byAuthor", true);
        } else {
            log.info("Show home page");
            projects = projectService.getAllVisibleOrderByCreated(FIRST_PAGE).getContent();
        }
        model.addAttribute("projects", projects);
        return "home/index";
    }

    @GetMapping("/about")
    public String showAboutPage() {
        log.info("Show about page");
        return "home/about";
    }

    @GetMapping("/contact")
    public String showContactPage() {
        log.info("Show contact page");
        return "home/contact";
    }
}
