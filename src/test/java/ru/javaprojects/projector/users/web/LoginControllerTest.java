package ru.javaprojects.projector.users.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;

import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.common.CommonTestData.HOME_URL;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class LoginControllerTest extends AbstractControllerTest {
    private static final String LOGIN_PAGE_VIEW = "profile/login";
    private static final String ERROR_PARAM_BAD_CREDENTIALS = "?error=bad-credentials";
    private static final String ERROR_PARAM_DISABLED_CREDENTIALS = "?error=disabled-credentials";

    @Autowired
    private MessageSource messageSource;

    @Test
    void showLoginPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(LOGIN_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN_PAGE_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showLoginPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(LOGIN_URL))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(HOME_URL));
    }

    @Test
    void loginWithNotExistingEmail() throws Exception {
        perform(MockMvcRequestBuilders.post(LOGIN_URL)
                .param(USERNAME_PARAM, NOT_EXISTING_EMAIL)
                .param(PASSWORD_PARAM, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL + ERROR_PARAM_BAD_CREDENTIALS));
    }

    @Test
    void loginWhenUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.post(LOGIN_URL)
                .param(USERNAME_PARAM, disabledUser.getEmail())
                .param(PASSWORD_PARAM, disabledUser.getPassword())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL + ERROR_PARAM_DISABLED_CREDENTIALS));
    }

    @Test
    void showLoginPageWithBadCredentialsParam() throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        perform(MockMvcRequestBuilders.get(LOGIN_URL + ERROR_PARAM_BAD_CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN_PAGE_VIEW))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, messageSource.getMessage("bad-credentials", null,
                        LocaleContextHolder.getLocale())));
    }

    @Test
    void showLoginPageWithDisabledCredentialsParam() throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        perform(MockMvcRequestBuilders.get(LOGIN_URL + ERROR_PARAM_DISABLED_CREDENTIALS))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN_PAGE_VIEW))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, messageSource.getMessage("disabled-credentials", null,
                        LocaleContextHolder.getLocale())));
    }
}
