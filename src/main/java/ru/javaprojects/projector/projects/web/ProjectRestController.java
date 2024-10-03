package ru.javaprojects.projector.projects.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.common.util.validation.ValidationUtil;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Comment;
import ru.javaprojects.projector.projects.to.CommentTo;
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
    public void likeProject(@PathVariable long id, @RequestParam boolean liked) {
        log.info("{} project with id={} by user with id={}", liked ? "like" : "dislike", id, AuthUser.authId());
        service.likeProject(id, liked, AuthUser.authId());
    }

    @PostMapping(value = "/{id}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Comment createComment(@Valid @RequestBody CommentTo commentTo) {
        log.info("create {}", commentTo);
        ValidationUtil.checkNew(commentTo);
        return service.createComment(commentTo, AuthUser.authId());
    }

    @PatchMapping("/{projectId}/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeComment(@PathVariable long projectId, @PathVariable long id, @RequestParam boolean liked) {
        log.info("{} comment with id={} for project with id={} by user with id={}", liked ? "like" : "dislike",
                id, projectId, AuthUser.authId());
        service.likeComment(id, liked, AuthUser.authId());
    }
}
