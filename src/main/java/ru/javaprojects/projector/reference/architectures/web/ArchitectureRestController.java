package ru.javaprojects.projector.reference.architectures.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.reference.architectures.ArchitectureService;

@RestController
@RequestMapping(value = ArchitectureController.ARCHITECTURES_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class ArchitectureRestController {
    private final ArchitectureService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete architecture with id={}", id);
        service.delete(id);
    }
}
