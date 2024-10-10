package ru.javaprojects.projector.users.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.app.AuthUser;
import ru.javaprojects.projector.users.service.PasswordResetService;
import ru.javaprojects.projector.users.service.UserService;

@RestController
@RequestMapping(value = ProfileController.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class ProfileRestController {
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@RequestParam String email) {
        log.info("forgot password for user with email={}", email);
        passwordResetService.sendPasswordResetEmail(email);
    }

    @PatchMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestParam @NotBlank String currentPassword,
                               @RequestParam @Size(min = 5, max = 32) String newPassword) {
        log.info("change password for user with id={}", AuthUser.authId());
        userService.changePassword(AuthUser.authId(), currentPassword, newPassword);
    }
}
