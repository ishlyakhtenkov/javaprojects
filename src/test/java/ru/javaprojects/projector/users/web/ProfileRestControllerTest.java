package ru.javaprojects.projector.users.web;

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
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.mail.MailSender;
import ru.javaprojects.projector.users.model.PasswordResetToken;
import ru.javaprojects.projector.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.projector.users.service.UserDisabledException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.projector.CommonTestData.*;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.service.PasswordResetService.PASSWORD_RESET_MESSAGE_LINK_TEMPLATE;
import static ru.javaprojects.projector.users.web.ProfileController.PROFILE_URL;

class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String PROFILE_FORGOT_PASSWORD_URL = PROFILE_URL + "/forgotPassword";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @MockBean
    private MailSender mailSender;

    @Value("${password-reset.url}")
    private String passwordResetUrl;

    @Test
    void forgotPassword() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, USER_MAIL)
                .with(csrf()))
                .andExpect(status().isNoContent());
        PasswordResetToken createdToken = tokenRepository.findByUserEmailIgnoreCase(USER_MAIL).orElseThrow();
        assertTrue(createdToken.getExpiryDate().after(new Date()));
        Locale locale = LocaleContextHolder.getLocale();
        String passwordResetUrlLinkText = messageSource.getMessage("password-reset.url-link-text", null, locale);
        String passwordResetMessageSubject = messageSource.getMessage("password-reset.message-subject", null, locale);
        String passwordResetMessageText = messageSource.getMessage("password-reset.message-text", null, locale);
        String link = String.format(PASSWORD_RESET_MESSAGE_LINK_TEMPLATE, passwordResetUrl, createdToken.getToken(),
                passwordResetUrlLinkText);
        String emailText = passwordResetMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(USER_MAIL, passwordResetMessageSubject, emailText);
    }

    @Test
    void forgotPasswordWhenPasswordResetTokenExists() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, passwordResetToken.getUser().getEmail())
                .with(csrf()))
                .andExpect(status().isNoContent());
        PasswordResetToken updatedToken = tokenRepository.findByUserEmailIgnoreCase(passwordResetToken.getUser().getEmail())
                .orElseThrow();
        assertTrue(updatedToken.getExpiryDate().after(new Date()));
        Locale locale = LocaleContextHolder.getLocale();
        String passwordResetUrlLinkText = messageSource.getMessage("password-reset.url-link-text", null, locale);
        String passwordResetMessageSubject = messageSource.getMessage("password-reset.message-subject", null, locale);
        String passwordResetMessageText = messageSource.getMessage("password-reset.message-text", null, locale);
        String link = String.format(PASSWORD_RESET_MESSAGE_LINK_TEMPLATE, passwordResetUrl, updatedToken.getToken(),
                passwordResetUrlLinkText);
        String emailText = passwordResetMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(passwordResetToken.getUser().getEmail(),
                passwordResetMessageSubject, emailText);
    }

    @Test
    void forgotPasswordNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, NOT_EXISTING_EMAIL)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("notfound.user", new Object[]{NOT_EXISTING_EMAIL},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROFILE_FORGOT_PASSWORD_URL));
        assertTrue(tokenRepository.findByUserEmailIgnoreCase(NOT_EXISTING_EMAIL).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void forgotPasswordAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, ADMIN_MAIL)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(tokenRepository.findByUserEmailIgnoreCase(ADMIN_MAIL).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void forgotPasswordUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, DISABLED_USER_MAIL)
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
                simpleDateFormat.format(tokenRepository.findByUserEmailIgnoreCase(DISABLED_USER_MAIL).orElseThrow().getExpiryDate()));
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }}
