package ru.javaprojects.projector.users.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.users.AuthUser;
import ru.javaprojects.projector.users.UserService;
import ru.javaprojects.projector.users.service.ChangeEmailService;
import ru.javaprojects.projector.users.service.PasswordResetService;

@RestController
@RequestMapping(value = ProfileController.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class ProfileRestController {
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final ChangeEmailService changeEmailService;

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@RequestParam String email) {
        log.info("forgot password for user with email={}", email);
        passwordResetService.sendPasswordResetEmail(email);
    }

    @PatchMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestParam @Size(min = 5, max = 32) String password,
                               @AuthenticationPrincipal AuthUser authUser) {
        log.info("change password for user with id={}", authUser.id());
        userService.changePassword(authUser.id(), password);
    }

    @PatchMapping("/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestParam @NotBlank @NoHtml @Size(min = 2, max = 32) String name,
                       @AuthenticationPrincipal AuthUser authUser) {
        log.info("update user with id={}", authUser.id());
        userService.update(authUser.id(), name);
    }

    @PostMapping("/change-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeEmail(@RequestParam @Email @NotBlank @NoHtml @Size(max = 128) String newEmail,
                            @AuthenticationPrincipal AuthUser authUser) {
        log.info("change email to {} for user with id={}", newEmail, authUser.id());
        changeEmailService.changeEmail(authUser.id(), newEmail);
    }
}
