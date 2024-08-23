package ru.javaprojects.projector.users.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.error.UserDisabledException;
import ru.javaprojects.projector.users.mail.MailSender;
import ru.javaprojects.projector.users.model.ChangeEmailToken;
import ru.javaprojects.projector.users.model.PasswordResetToken;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.ChangeEmailTokenRepository;
import ru.javaprojects.projector.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.projector.users.service.UserService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.projector.CommonTestData.INVALID_NAME;
import static ru.javaprojects.projector.CommonTestData.NAME_PARAM;
import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.service.TokenService.LINK_TEMPLATE;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.projector.users.web.ProfileController.PROFILE_URL;

class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String PROFILE_FORGOT_PASSWORD_URL = PROFILE_URL + "/forgot-password";
    private static final String PROFILE_CHANGE_PASSWORD_URL = PROFILE_URL + "/change-password";
    private static final String PROFILE_UPDATE_URL = PROFILE_URL + "/update";
    private static final String PROFILE_CHANGE_EMAIL_URL = PROFILE_URL + "/change-email";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private ChangeEmailTokenRepository changeEmailTokenRepository;

    @Autowired
    private UserService userService;

    @MockBean
    private MailSender mailSender;

    @Value("${password-reset.confirm-url}")
    private String passwordResetUrl;

    @Value("${change-email.confirm-url}")
    private String confirmChangeEmailUrl;

    @Test
    void forgotPassword() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, USER_MAIL)
                .with(csrf()))
                .andExpect(status().isNoContent());
        PasswordResetToken createdToken = passwordResetTokenRepository.findByUserEmailIgnoreCase(USER_MAIL).orElseThrow();
        assertTrue(createdToken.getExpiryDate().after(new Date()));
        Locale locale = LocaleContextHolder.getLocale();
        String passwordResetUrlLinkText = messageSource.getMessage("password-reset.message-link-text", null, locale);
        String passwordResetMessageSubject = messageSource.getMessage("password-reset.message-subject", null, locale);
        String passwordResetMessageText = messageSource.getMessage("password-reset.message-text", null, locale);
        String link = String.format(LINK_TEMPLATE, passwordResetUrl, createdToken.getToken(),
                passwordResetUrlLinkText);
        String emailText = passwordResetMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(USER_MAIL, passwordResetMessageSubject, emailText);
    }

    @Test
    void forgotPasswordWhenPasswordResetTokenExists() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, passwordResetToken.getUser().getEmail())
                .with(csrf()))
                .andExpect(status().isNoContent());
        PasswordResetToken updatedToken = passwordResetTokenRepository.findByUserEmailIgnoreCase(passwordResetToken.getUser().getEmail())
                .orElseThrow();
        assertTrue(updatedToken.getExpiryDate().after(new Date()));
        Locale locale = LocaleContextHolder.getLocale();
        String passwordResetUrlLinkText = messageSource.getMessage("password-reset.message-link-text", null, locale);
        String passwordResetMessageSubject = messageSource.getMessage("password-reset.message-subject", null, locale);
        String passwordResetMessageText = messageSource.getMessage("password-reset.message-text", null, locale);
        String link = String.format(LINK_TEMPLATE, passwordResetUrl, updatedToken.getToken(),
                passwordResetUrlLinkText);
        String emailText = passwordResetMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(passwordResetToken.getUser().getEmail(),
                passwordResetMessageSubject, emailText);
    }

    @Test
    void forgotPasswordNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, NOT_EXISTING_EMAIL)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("notfound.user", new Object[]{NOT_EXISTING_EMAIL},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROFILE_FORGOT_PASSWORD_URL));
        assertTrue(passwordResetTokenRepository.findByUserEmailIgnoreCase(NOT_EXISTING_EMAIL).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void forgotPasswordAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, ADMIN_MAIL)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(passwordResetTokenRepository.findByUserEmailIgnoreCase(ADMIN_MAIL).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void forgotPasswordUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, DISABLED_USER_MAIL)
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        UserDisabledException.class))
                .andExpect(problemTitle(HttpStatus.FORBIDDEN.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.FORBIDDEN.value()))
                .andExpect(problemDetail(messageSource.getMessage("user.disabled", new Object[]{DISABLED_USER_MAIL},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROFILE_FORGOT_PASSWORD_URL));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(simpleDateFormat.format(disabledUserPasswordResetToken.getExpiryDate()),
                simpleDateFormat.format(passwordResetTokenRepository.findByUserEmailIgnoreCase(DISABLED_USER_MAIL).orElseThrow().getExpiryDate()));
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePassword() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(USER_ID).getPassword()));
    }

    @Test
    void changePasswordUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePasswordInvalid() throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD_PARAM, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("changePassword.password: size must be between 5 and 32"))
                .andExpect(problemInstance(PROFILE_CHANGE_PASSWORD_URL));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, userService.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfile() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_UPDATE_URL)
                .param(NAME_PARAM, UPDATED_NAME)
                .with(csrf()))
                .andExpect(status().isNoContent());
        User updated = new User(user);
        updated.setName(UPDATED_NAME);
        USER_MATCHER.assertMatch(userService.get(USER_ID), updated);
    }

    @Test
    void updateProfileUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_UPDATE_URL)
                .param(NAME_PARAM, UPDATED_NAME)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfileInvalid() throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        perform(MockMvcRequestBuilders.patch(PROFILE_UPDATE_URL)
                .param(NAME_PARAM, INVALID_NAME)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("update.name: Should not be html"))
                .andExpect(problemInstance(PROFILE_UPDATE_URL));
        assertNotEquals(INVALID_NAME, userService.get(USER_ID).getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changeEmail() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_CHANGE_EMAIL_URL)
                .param(NEW_EMAIL_PARAM, NEW_EMAIL)
                .with(csrf()))
                .andExpect(status().isNoContent());
        ChangeEmailToken createdToken = changeEmailTokenRepository.findByUser_Id(USER_ID).orElseThrow();
        assertTrue(createdToken.getExpiryDate().after(new Date()));
        Locale locale = LocaleContextHolder.getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.message-link-text", null, locale);
        String changeEmailMessageSubject = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(LINK_TEMPLATE, confirmChangeEmailUrl, createdToken.getToken(),
                changeEmailUrlLinkText);
        String emailText = changeEmailMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(NEW_EMAIL, changeEmailMessageSubject, emailText);
    }

    @Test
    void changeEmailUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_CHANGE_EMAIL_URL)
                .param(NEW_EMAIL_PARAM, NEW_EMAIL)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changeEmailDuplicateEmail() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_CHANGE_EMAIL_URL)
                .param(NEW_EMAIL_PARAM, ADMIN_MAIL)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("change-email.already-in-use", new Object[]{ADMIN_MAIL},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROFILE_CHANGE_EMAIL_URL));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changeEmailAlreadyHas() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_CHANGE_EMAIL_URL)
                .param(NEW_EMAIL_PARAM, USER_MAIL)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("change-email.already-has-email", new Object[]{USER_MAIL},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROFILE_CHANGE_EMAIL_URL));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changeEmailInvalid() throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        perform(MockMvcRequestBuilders.post(PROFILE_CHANGE_EMAIL_URL)
                .param(NEW_EMAIL_PARAM, INVALID_EMAIL)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("changeEmail.newEmail: must be a well-formed email address"))
                .andExpect(problemInstance(PROFILE_CHANGE_EMAIL_URL));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changeEmailWhenChangeEmailTokenExists() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_CHANGE_EMAIL_URL)
                .param(NEW_EMAIL_PARAM, NEW_EMAIL)
                .with(csrf()))
                .andExpect(status().isNoContent());
        ChangeEmailToken updatedToken = changeEmailTokenRepository.findByUser_Id(ADMIN_ID).orElseThrow();
        assertTrue(updatedToken.getExpiryDate().after(new Date()));
        assertEquals(NEW_EMAIL, updatedToken.getNewEmail());
        Locale locale = LocaleContextHolder.getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.message-link-text", null, locale);
        String changeEmailMessageSubject = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(LINK_TEMPLATE, confirmChangeEmailUrl, updatedToken.getToken(),
                changeEmailUrlLinkText);
        String emailText = changeEmailMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(NEW_EMAIL, changeEmailMessageSubject, emailText);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changeEmailWhenSomeOneHasTokenWithThisEmail() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_CHANGE_EMAIL_URL)
                .param(NEW_EMAIL_PARAM, NEW_EMAIL_SOMEONE_HAS_TOKEN)
                .with(csrf()))
                .andExpect(status().isNoContent());
        ChangeEmailToken createdToken = changeEmailTokenRepository.findByUser_Id(USER_ID).orElseThrow();
        assertTrue(createdToken.getExpiryDate().after(new Date()));
        Locale locale = LocaleContextHolder.getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.message-link-text", null, locale);
        String changeEmailMessageSubject = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(LINK_TEMPLATE, confirmChangeEmailUrl, createdToken.getToken(),
                changeEmailUrlLinkText);
        String emailText = changeEmailMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(NEW_EMAIL_SOMEONE_HAS_TOKEN, changeEmailMessageSubject,
                emailText);
    }
}
