package ru.javaprojects.projector.projects.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.app.AuthUser;
import ru.javaprojects.projector.common.validation.NoHtml;
import ru.javaprojects.projector.common.validation.ValidationUtil;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Comment;
import ru.javaprojects.projector.projects.to.CommentTo;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping(value = ProjectController.PROJECTS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class ProjectRestController {
    private final ProjectService service;

    @GetMapping("/by-author")
    public List<ProjectPreviewTo> getAllProjectsByAuthor(@RequestParam long userId) {
        log.info("get all projects by author with id={}", userId);
        boolean visibleOnly = AuthUser.safeGet() == null || AuthUser.authId() != userId;
        return service.getAllByAuthor(userId, visibleOnly);
    }

    @GetMapping("/fresh")
    public Page<ProjectPreviewTo> getFreshProjects(@PageableDefault @SortDefault(value = "created", direction = DESC)
                                                   Pageable p) {
        log.info("get fresh projects (pageNumber={}, pageSize={})", p.getPageNumber(), p.getPageSize());
        return service.getAllVisible(p);
    }

    @GetMapping("/popular")
    public Page<ProjectPreviewTo> getPopularProjects(@PageableDefault Pageable p) {
        log.info("get popular projects (pageNumber={}, pageSize={})", p.getPageNumber(), p.getPageSize());
        return service.getAllVisibleOrderByPopularity(p);
    }

    @GetMapping("/by-tag")
    public Page<ProjectPreviewTo> getProjectsByTag(@RequestParam String tag,
                                                   @PageableDefault @SortDefault(value = "created", direction = DESC)
                                                   Pageable p) {
        log.info("get projects by tag={} (pageNumber={}, pageSize={})", tag, p.getPageNumber(), p.getPageSize());
        return service.getAllVisibleByTag(tag.trim(), p);
    }

    @GetMapping("/by-keyword")
    public Page<ProjectPreviewTo> getProjectsByKeyword(@RequestParam String keyword,
                                                       @PageableDefault @SortDefault(value = "name") Pageable p) {
        log.info("get projects by keyword={} (pageNumber={}, pageSize={})", keyword, p.getPageNumber(), p.getPageSize());
        return service.getAllVisibleByKeyword(keyword.trim(), p);
    }

    @PatchMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeProject(@PathVariable long id, @RequestParam boolean liked) {
        log.info("{} project with id={} by user with id={}", liked ? "like" : "dislike", id, AuthUser.authId());
        service.likeProject(id, liked, AuthUser.authId());
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideProject(@PathVariable long id, @RequestParam boolean visible) {
        log.info("{} project with id={}", visible ? "reveal" : "hide", id);
        service.hide(id, visible, AuthUser.authId(), AuthUser.isAdmin());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable long id) {
        log.info("delete project with id={}", id);
        service.delete(id, AuthUser.authId(), AuthUser.isAdmin());
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

    @PutMapping("/{projectId}/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateComment(@PathVariable long projectId, @PathVariable long id,
                              @NotBlank(message = "{validation.comment.text.NotBlank}")
                              @NoHtml(message = "{validation.comment.text.NoHtml}")
                              @Size(max = 4096, message = "{validation.comment.text.Size}") String text) {
        log.info("update comment with id={} for project with id={}", id, projectId);
        service.updateComment(id, text, AuthUser.authId());
    }

    @DeleteMapping("/{projectId}/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long projectId, @PathVariable long id) {
        log.info("delete comment with id={} for project with id={}{}", id, projectId, AuthUser.isAdmin() ? " by admin" : "");
        service.deleteComment(id, AuthUser.authId(), AuthUser.isAdmin());
    }
}
