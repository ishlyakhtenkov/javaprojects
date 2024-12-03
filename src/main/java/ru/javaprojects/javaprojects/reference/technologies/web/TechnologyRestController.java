package ru.javaprojects.javaprojects.reference.technologies.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyService;

@RestController
@RequestMapping(value = TechnologyController.TECHNOLOGIES_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class TechnologyRestController {
    private final TechnologyService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete technology with id={}", id);
        service.delete(id);
    }
}
