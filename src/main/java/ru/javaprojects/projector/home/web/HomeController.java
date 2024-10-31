package ru.javaprojects.projector.home.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@AllArgsConstructor
@Slf4j
public class HomeController {
    private final ProjectService projectService;

    @GetMapping("/")
    public String showHomePage(@RequestParam(required = false) String popular,
                               @RequestParam(name = "by-author", required = false) Long authorId,  Model model) {
        List<ProjectPreviewTo> projects;
        if (popular != null) {
            log.info("Show home page with popular projects");
            projects = projectService.getAllVisibleOrderByPopularity();
        } else if (authorId != null) {
            log.info("Show home page with projects by author with id =" + authorId);
            projects = projectService.getAllByAuthor(authorId, true);
        } else {
            log.info("Show home page");
            projects = projectService.getAllVisible(Sort.by(DESC, "created"));
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
