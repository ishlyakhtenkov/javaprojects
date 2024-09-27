package ru.javaprojects.projector.projects.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.users.AuthUser;

@RestController
@RequestMapping(value = ProjectRestController.PROJECTS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class ProjectRestController {
    static final String PROJECTS_URL = "/projects";

    private final ProjectService service;

    @PatchMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable long id, @RequestParam boolean liked) {
        log.info("{} project with id={} by user with id={}", liked ? "like" : "dislike", id, AuthUser.authId());
        service.like(id, liked, AuthUser.authId());
    }
}
