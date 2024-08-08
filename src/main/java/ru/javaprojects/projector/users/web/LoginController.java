package ru.javaprojects.projector.users.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.projector.users.AuthUser;

@Controller
@RequestMapping(LoginController.LOGIN_URL)
@AllArgsConstructor
@Slf4j
public class LoginController {
    static final String LOGIN_URL = "/login";

    @GetMapping
    public String showLoginPage(@AuthenticationPrincipal AuthUser authUser) {
        log.info("show login page");
        if (authUser == null) {
            return "users/login";
        }
        return "redirect:/";
    }
}
