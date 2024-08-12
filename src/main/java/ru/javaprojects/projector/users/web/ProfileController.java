package ru.javaprojects.projector.users.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.projector.users.PasswordResetTo;
import ru.javaprojects.projector.users.service.PasswordResetService;

@Controller
@RequestMapping(ProfileController.PROFILE_URL)
@AllArgsConstructor
@Slf4j
public class ProfileController {
    static final String PROFILE_URL = "/profile";

    private final PasswordResetService passwordResetService;
    private final MessageSource messageSource;

    @GetMapping("/resetPassword")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        log.info("show reset password form by token={}", token);
        passwordResetService.checkToken(token);
        model.addAttribute("passwordResetTo",  new PasswordResetTo(null, token));
        return "users/reset-password";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@Valid PasswordResetTo passwordResetTo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/reset-password";
        }
        log.info("reset password by token={}", passwordResetTo.getToken());
        passwordResetService.resetPassword(passwordResetTo);
        redirectAttributes.addFlashAttribute("action", messageSource.getMessage("password-reset.success-reset", null,
                LocaleContextHolder.getLocale()));
        return "redirect:/login";
    }
}
