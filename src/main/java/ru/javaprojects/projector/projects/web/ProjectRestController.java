package ru.javaprojects.projector.projects.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.app.AuthUser;
import ru.javaprojects.projector.common.validation.NoHtml;
import ru.javaprojects.projector.common.validation.ValidationUtil;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Comment;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.to.CommentTo;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;

import java.util.List;

@RestController
@RequestMapping(value = ProjectController.PROJECTS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class ProjectRestController {
    private final ProjectService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete project with id={}", id);
        service.delete(id, AuthUser.authId(), AuthUser.isAdmin());
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reveal(@PathVariable long id, @RequestParam boolean visible) {
        log.info("{} project with id={}", visible ? "reveal" : "hide", id);
        service.reveal(id, visible, AuthUser.authId(), AuthUser.isAdmin());
    }

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

    @PatchMapping("/{projectId}/comments/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeComment(@PathVariable long projectId, @PathVariable long id, @RequestParam boolean liked) {
        log.info("{} comment with id={} for project with id={} by user with id={}", liked ? "like" : "dislike",
                id, projectId, AuthUser.authId());
        service.likeComment(id, liked, AuthUser.authId());
    }

    @DeleteMapping("/{projectId}/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long projectId, @PathVariable long id) {
        log.info("delete comment with id={} for project with id={}{}", id, projectId, AuthUser.isAdmin() ? " by admin" : "");
        service.deleteComment(id, AuthUser.authId(), AuthUser.isAdmin());
    }

    @PutMapping("/{projectId}/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateComment(@PathVariable long projectId, @PathVariable long id,
                              @NotBlank(message = "{validation.comment.text.NotBlank}")
                              @NoHtml(message = "{validation.comment.text.NoHtml}")
                              @Size(max = 4096, message = "{validation.comment.text.Size}") String text) {
        log.info("update comment with id={} for project with id={}", id, projectId);
        service.updateComment(id, text, AuthUser.authId());
    }


    @GetMapping("/by-author")
    public List<ProjectPreviewTo> getAllProjectsByAuthor(@RequestParam long userId) {
        log.info("get all projects by user with id={}", userId);
        boolean visibleOnly = AuthUser.safeGet() == null || AuthUser.authId() != userId;
        return service.getAllByAuthor(userId, visibleOnly);
    }
}
