package ru.javaprojects.projector.users.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.service.UserService;

@Controller
@RequestMapping(AdminController.USERS_URL)
@AllArgsConstructor
@Slf4j
public class AdminController {
    static final String USERS_URL = "/users";

    private final UserService service;
    private final UniqueEmailValidator emailValidator;

    @InitBinder({"user", "userTo"})
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault Pageable pageable, Model model, RedirectAttributes redirectAttributes) {
        Page<User> users;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/users";
            }
            log.info("get users (pageNumber={}, pageSize={}, keyword={})", pageable.getPageNumber(),
                    pageable.getPageSize(), keyword);
            users = service.getAll(pageable, keyword.trim());
        } else  {
            log.info("get users (pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            users = service.getAll(pageable);
        }
        if (users.getContent().isEmpty() && users.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/users";
        }
        model.addAttribute("users", users);
        return "users/users";
    }


}
