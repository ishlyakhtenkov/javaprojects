package ru.javaprojects.projector.home.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.javaprojects.projector.projects.ProjectService;

@Controller
@AllArgsConstructor
@Slf4j
public class HomeController {
    private final ProjectService projectService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.info("Show home page");
        model.addAttribute("projects", projectService.getAllVisibleWithAllInformation());
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
