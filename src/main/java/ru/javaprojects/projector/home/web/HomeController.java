package ru.javaprojects.projector.home.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.javaprojects.projector.app.AuthUser;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Like;
import ru.javaprojects.projector.projects.model.Project;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@Slf4j
public class HomeController {
    private final ProjectService projectService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.info("Show home page");
        List<Project> projects = projectService.getAllEnabledWithAllInformation();
        if (AuthUser.safeGet() != null) {
            long authId = AuthUser.authId();
            Set<Long> likedProjectsIds = projects.stream()
                    .flatMap(project -> project.getLikes().stream())
                    .filter(like -> like.getUserId() == authId)
                    .map(Like::getObjectId)
                    .collect(Collectors.toSet());
            model.addAttribute("likedProjectsIds", likedProjectsIds);
        }
        model.addAttribute("projects", projects);
        model.addAttribute("commentsTotal", projectService.getTotalCommentsByProject());
        return "index";
    }
}
