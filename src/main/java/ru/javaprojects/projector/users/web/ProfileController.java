package ru.javaprojects.projector.users.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.projector.users.AuthUser;
import ru.javaprojects.projector.users.to.PasswordResetTo;
import ru.javaprojects.projector.users.service.UserService;
import ru.javaprojects.projector.users.service.ChangeEmailService;
import ru.javaprojects.projector.users.service.PasswordResetService;

@Controller
@RequestMapping(ProfileController.PROFILE_URL)
@AllArgsConstructor
@Slf4j
public class ProfileController {
    public static final String PROFILE_URL = "/profile";

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final ChangeEmailService changeEmailService;
    private final MessageSource messageSource;

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        log.info("show reset password form by token={}", token);
        passwordResetService.checkToken(token);
        model.addAttribute("passwordResetTo",  new PasswordResetTo(null, token));
        return "profile/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid PasswordResetTo passwordResetTo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profile/reset-password";
        }
        log.info("reset password by token={}", passwordResetTo.getToken());
        passwordResetService.resetPassword(passwordResetTo);
        redirectAttributes.addFlashAttribute("action", messageSource.getMessage("password-reset.success-reset", null,
                LocaleContextHolder.getLocale()));
        return "redirect:/login";
    }

    @GetMapping
    public String profile(@AuthenticationPrincipal AuthUser authUser, Model model) {
        log.info("show profile for user with id={}", authUser.id());
        model.addAttribute("user", userService.get(authUser.id()));
        return "profile/profile";
    }

    @GetMapping("/change-email/confirm")
    public String confirmChangeEmail(@RequestParam String token, RedirectAttributes redirectAttributes,
                                     @AuthenticationPrincipal AuthUser authUser) {
        log.info("confirm change email for user with id={} by token={}", authUser.id(), token);
        changeEmailService.confirmChangeEmail(token, authUser.id());
        redirectAttributes.addFlashAttribute("action", messageSource.getMessage("change-email.email-confirmed", null,
                LocaleContextHolder.getLocale()));
        return "redirect:/profile";
    }
}
