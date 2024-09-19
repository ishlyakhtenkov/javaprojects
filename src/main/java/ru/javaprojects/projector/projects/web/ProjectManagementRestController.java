package ru.javaprojects.projector.projects.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.projects.ProjectService;

@RestController
@RequestMapping(value = ProjectManagementController.PROJECT_MANAGEMENT_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class ProjectManagementRestController {
    private final ProjectService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete project with id={}", id);
        service.delete(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable long id, @RequestParam boolean enabled) {
        log.info((enabled ? "enable" : "disable") + " project with id={}", id);
        service.enable(id, enabled);
    }
}
