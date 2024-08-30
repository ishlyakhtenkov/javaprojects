package ru.javaprojects.projector.users.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.error.TokenException;
import ru.javaprojects.projector.users.error.UserDisabledException;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.ChangeEmailTokenRepository;
import ru.javaprojects.projector.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.projector.users.service.UserService;
import ru.javaprojects.projector.users.to.PasswordResetTo;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.ACTION_ATTRIBUTE;
import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.projector.users.web.ProfileController.PROFILE_URL;

class ProfileControllerTest extends AbstractControllerTest {
    private static final String PROFILE_RESET_PASSWORD_URL = PROFILE_URL + "/reset-password";
    private static final String RESET_PASSWORD_VIEW = "users/reset-password";
    private static final String PROFILE_VIEW = "users/profile";
    private static final String CONFIRM_CHANGE_EMAIL_URL = PROFILE_URL + "/change-email/confirm";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private ChangeEmailTokenRepository changeEmailTokenRepository;

    @Test
    void showResetPasswordForm() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
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
                .param(TOKEN_PARAM, NOT_EXISTING_TOKEN)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("password-reset.token-not-found", null,
                        LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void showResetPasswordFormTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, expiredPasswordResetToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("password-reset.token-expired", null,
                        LocaleContextHolder.getLocale()), TokenException.class));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showResetPasswordFormAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void showResetPasswordFormUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, disabledUserPasswordResetToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("user.disabled",
                        new Object[]{disabledUserPasswordResetToken.getUser().getEmail()},
                        LocaleContextHolder.getLocale()), UserDisabledException.class));
    }

    @Test
    void resetPassword() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("password-reset.success-reset", null,
                        LocaleContextHolder.getLocale())));
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(passwordResetToken.getUser().id()).getPassword()));
        assertTrue(passwordResetTokenRepository.findByToken(passwordResetToken.getToken()).isEmpty());
    }

    @Test
    void resetPasswordTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, NOT_EXISTING_TOKEN)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("password-reset.token-not-found", null,
                        LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void resetPasswordTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, expiredPasswordResetToken.getToken())
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("password-reset.token-expired", null,
                        LocaleContextHolder.getLocale()), TokenException.class));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(expiredPasswordResetToken.getUser().id()).getPassword()));
    }

    @Test
    void resetPasswordUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, disabledUserPasswordResetToken.getToken())
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("user.disabled",
                        new Object[]{disabledUserPasswordResetToken.getUser().getEmail()},
                        LocaleContextHolder.getLocale()), UserDisabledException.class));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(disabledUserPasswordResetToken.getUser().id()).getPassword()));
    }

    @Test
    void resetPasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .param(PASSWORD_PARAM, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PASSWORD_RESET_TO_ATTRIBUTE, PASSWORD_PARAM))
                .andExpect(view().name(RESET_PASSWORD_VIEW));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, userService.get(passwordResetToken.getUser().id()).getPassword()));

    }

    @Test
    void resetPasswordWithoutTokenParam() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PASSWORD_RESET_TO_ATTRIBUTE, TOKEN_PARAM))
                .andExpect(view().name(RESET_PASSWORD_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void resetPasswordAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showProfilePage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_VIEW))
                .andExpect(model().attribute(USER_ATTRIBUTE, user))
                .andExpect(result -> USER_MATCHER.assertMatch((User)Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(USER_ATTRIBUTE), user));
    }

    @Test
    void showProfilePageUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_URL)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void confirmChangeEmail() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, changeEmailToken.getToken())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROFILE_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("change-email.email-confirmed", null,
                        LocaleContextHolder.getLocale())));
        assertTrue(changeEmailTokenRepository.findByToken(changeEmailToken.getToken()).isEmpty());
        User updated = userService.get(USER2_ID);
        assertEquals(changeEmailToken.getNewEmail(), updated.getEmail());
    }

    @Test
    void confirmChangeEmailUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, changeEmailToken.getToken())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void confirmChangeEmailTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, UUID.randomUUID().toString())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("change-email.token-not-found", null,
                        LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void confirmChangeEmailTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, expiredChangeEmailToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("change-email.token-expired", null,
                        LocaleContextHolder.getLocale()), TokenException.class));
        assertNotEquals(expiredChangeEmailToken.getNewEmail(), userService.get(ADMIN_ID).getEmail());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void confirmChangeEmailTokenNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, changeEmailToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("change-email.token-not-belongs", null,
                        LocaleContextHolder.getLocale()), TokenException.class));
        assertNotEquals(changeEmailToken.getNewEmail(), userService.get(ADMIN_ID).getEmail());
    }
}
