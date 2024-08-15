package ru.javaprojects.projector.users.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.projector.users.service.RegisterService;
import ru.javaprojects.projector.users.to.UserTo;

import static ru.javaprojects.projector.common.util.validation.ValidationUtil.checkNew;

@Controller
@RequestMapping(RegisterController.REGISTER_URL)
@AllArgsConstructor
@Slf4j
public class RegisterController {
    static final String REGISTER_URL = "/register";

    private final RegisterService service;
    private final UniqueEmailValidator emailValidator;
    private final MessageSource messageSource;

    @InitBinder("userTo")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String showRegisterPage(Model model) {
        log.info("show register page");
        model.addAttribute("userTo", new UserTo());
        return "users/register";
    }

    @PostMapping
    public String register(@Valid UserTo userTo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/register";
        }
        log.info("register {}", userTo);
        checkNew(userTo);
        service.register(userTo);
        redirectAttributes.addFlashAttribute("action", messageSource.getMessage("register.check-your-email", null,
                LocaleContextHolder.getLocale()));
        return "redirect:/login";
    }

    @GetMapping("/confirm")
    public String confirmRegister(@RequestParam String token, RedirectAttributes redirectAttributes) {
        log.info("confirm register by token={}", token);
        service.confirmRegister(token);
        redirectAttributes.addFlashAttribute("action", messageSource.getMessage("register.email-confirmed", null,
                LocaleContextHolder.getLocale()));
        return "redirect:/login";
    }
}
