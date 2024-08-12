package ru.javaprojects.projector.users.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.users.service.PasswordResetService;

@RestController
@RequestMapping(value = ProfileController.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class ProfileRestController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/forgotPassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@RequestParam String email) {
        log.info("forgot password for user with email={}", email);
        passwordResetService.sendPasswordResetEmail(email);
    }
}
