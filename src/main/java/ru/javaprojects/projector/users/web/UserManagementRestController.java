package ru.javaprojects.projector.users.web;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.users.service.UserService;

@RestController
@RequestMapping(value = UserManagementController.USERS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class UserManagementRestController {
    private final UserService service;

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable long id, @RequestParam boolean enabled) {
        log.info((enabled ? "enable" : "disable") + " user with id={}", id);
        service.enable(id, enabled);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete user with id={}", id);
        service.delete(id);
    }

    @PatchMapping("/change-password/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable long id, @RequestParam @Size(min = 5, max = 32) String password) {
        log.info("change password for user with id={}", id);
        service.changePassword(id, password);
    }
}
