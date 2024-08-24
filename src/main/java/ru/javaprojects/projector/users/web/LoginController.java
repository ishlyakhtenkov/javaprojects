package ru.javaprojects.projector.users.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.javaprojects.projector.users.AuthUser;

@Controller
@RequestMapping(LoginController.LOGIN_URL)
@AllArgsConstructor
@Slf4j
public class LoginController {
    public static final String LOGIN_URL = "/login";

    private final MessageSource messageSource;

    @GetMapping
    public String showLoginPage(@RequestParam(name = "error", required = false) String error, Model model,
                                @AuthenticationPrincipal AuthUser authUser) {
        log.info("show login page");
        if (authUser == null) {
            if (error != null) {
                model.addAttribute("error", messageSource.getMessage(error, null,
                        "Bad credentials", LocaleContextHolder.getLocale()));
            }
            return "users/login";
        }
        return "redirect:/";
    }
}
