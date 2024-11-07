package ru.javaprojects.projector.home.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Tag;
import ru.javaprojects.projector.projects.repository.TagRepository;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.service.UserService;
import ru.javaprojects.projector.users.to.ProfileTo;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@AllArgsConstructor
@Slf4j
public class HomeController {
    private final ProjectService projectService;
    private final UserService userService;
    private final TagRepository tagRepository;

    @GetMapping("/")
    public String showHomePage(@RequestParam(required = false) String popular,
                               @RequestParam(name = "by-author", required = false) Long authorId, Model model) {
        List<ProjectPreviewTo> projects;
        if (popular != null) {
            log.info("Show home page with popular projects");
            projects = projectService.getAllVisibleOrderByPopularity(getFirstPage(Sort.unsorted())).getContent();
        } else if (authorId != null) {
            log.info("Show home page with projects by author with id =" + authorId);
            projects = projectService.getAllByAuthor(authorId, true);
        } else {
            log.info("Show home page");
            projects = projectService.getAllVisible(getFirstPage(Sort.by(DESC, "created"))).getContent();
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

    @GetMapping("/tags/{tag}")
    public String showProjectsByTag(@PathVariable String tag, Model model) {
        log.info("show projects by tag={}", tag);
        List<ProjectPreviewTo> projects = projectService.getAllVisibleByTag(tag,
                getFirstPage(Sort.by(DESC, "created"))).getContent();
        model.addAttribute("projects", projects);
        return "home/index";
    }

    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        if (keyword != null && !keyword.isBlank()) {
            log.info("do search by keyword={}", keyword);
            Page<ProfileTo> profilesPage = userService.getAllEnabledProfilesByKeyword(keyword.trim(),
                    getFirstPage(Sort.by("name")));
            model.addAttribute("profilesPage", profilesPage);
            Page<ProjectPreviewTo> projectsPage = projectService.getAllVisibleByKeyword(keyword.trim(),
                    getFirstPage(Sort.by("name")));
            model.addAttribute("projectsPage", projectsPage);
            if (keyword.startsWith("#")) {
                keyword = keyword.substring(1);
            }
            Page<Tag> tagsPage = tagRepository.findAllByKeyword(keyword.trim(), getFirstPage(Sort.by("name")));
            model.addAttribute("tagsPage", tagsPage);
            return "home/search";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping(value = "/search/tags", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Tag>> getTagsByKeyword(@RequestParam String keyword,
                                                     @PageableDefault @SortDefault(value = "name") Pageable pageable) {
        log.info("get tags by keyword={} (pageNumber={}, pageSize={})", keyword, pageable.getPageNumber(), pageable.getPageSize());
        if (keyword.startsWith("#")) {
            keyword = keyword.substring(1);
        }
        Page<Tag> tagsPage = tagRepository.findAllByKeyword(keyword.trim(), pageable);
        return ResponseEntity.ok(tagsPage);
    }

    private Pageable getFirstPage(Sort sort) {
        return PageRequest.of(0, 9, sort);
    }
}
