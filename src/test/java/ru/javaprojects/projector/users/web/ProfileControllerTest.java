package ru.javaprojects.projector.users.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.PasswordResetTo;
import ru.javaprojects.projector.users.TokenExpiredException;
import ru.javaprojects.projector.users.UserRepository;
import ru.javaprojects.projector.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.projector.users.service.UserDisabledException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.ACTION;
import static ru.javaprojects.projector.CommonTestData.USER_MAIL;
import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.projector.users.web.ProfileController.PROFILE_URL;

class ProfileControllerTest extends AbstractControllerTest {
    private static final String PROFILE_RESET_PASSWORD_URL = PROFILE_URL + "/resetPassword";
    private static final String RESET_PASSWORD_VIEW = "users/reset-password";
    private static final String PROFILE_VIEW = "users/profile";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void showResetPasswordForm() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, passwordResetToken.getToken())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PASSWORD_RESET_TO_ATTRIBUTE))
                .andExpect(view().name(RESET_PASSWORD_VIEW))
                .andExpect(result ->
                        PASSWORD_RESET_TO_MATCHER.assertMatch((PasswordResetTo) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(PASSWORD_RESET_TO_ATTRIBUTE),
                                new PasswordResetTo(null, passwordResetToken.getToken())));
    }

    @Test
    void showResetPasswordFormTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, NOT_EXISTING_TOKEN)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("password-reset.token-not-found", null,
                        LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void showResetPasswordFormTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, expiredPasswordResetToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("password-reset.token-expired", null,
                        LocaleContextHolder.getLocale()), TokenExpiredException.class));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showResetPasswordFormAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, passwordResetToken.getToken())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void showResetPasswordFormUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, disabledUserPasswordResetToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("user.disabled",
                        new Object[]{disabledUserPasswordResetToken.getUser().getEmail()},
                        LocaleContextHolder.getLocale()), UserDisabledException.class));
    }

    @Test
    void resetPassword() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, passwordResetToken.getToken())
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION, messageSource.getMessage("password-reset.success-reset", null,
                        LocaleContextHolder.getLocale())));
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, userRepository.findById(passwordResetToken.getUser().id())
                .orElseThrow().getPassword()));
        assertTrue(passwordResetTokenRepository.findByToken(passwordResetToken.getToken()).isEmpty());
    }

    @Test
    void resetPasswordTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, NOT_EXISTING_TOKEN)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("password-reset.token-not-found", null,
                        LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void resetPasswordTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, expiredPasswordResetToken.getToken())
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("password-reset.token-expired", null,
                        LocaleContextHolder.getLocale()), TokenExpiredException.class));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, userRepository.
                findById(expiredPasswordResetToken.getUser().id()).orElseThrow().getPassword()));
    }

    @Test
    void resetPasswordUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, disabledUserPasswordResetToken.getToken())
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("user.disabled",
                        new Object[]{disabledUserPasswordResetToken.getUser().getEmail()},
                        LocaleContextHolder.getLocale()), UserDisabledException.class));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, userRepository.
                findById(disabledUserPasswordResetToken.getUser().id()).orElseThrow().getPassword()));
    }

    @Test
    void resetPasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, passwordResetToken.getToken())
                .param(PASSWORD, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PASSWORD_RESET_TO_ATTRIBUTE, PASSWORD))
                .andExpect(view().name(RESET_PASSWORD_VIEW));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, userRepository.findById(passwordResetToken.getUser().id())
                .orElseThrow().getPassword()));
    }

    @Test
    void resetPasswordWithoutTokenParam() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PASSWORD_RESET_TO_ATTRIBUTE, TOKEN))
                .andExpect(view().name(RESET_PASSWORD_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void resetPasswordAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, passwordResetToken.getToken())
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
