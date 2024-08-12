package ru.javaprojects.projector.users.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.*;
import ru.javaprojects.projector.users.mail.MailSender;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.ACTION;
import static ru.javaprojects.projector.CommonTestData.USER_MAIL;
import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.users.RegisterService.CONFIRM_REGISTER_MESSAGE_LINK_TEMPLATE;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.projector.users.web.RegisterController.REGISTER_URL;
import static ru.javaprojects.projector.users.web.UniqueEmailValidator.DUPLICATE_ERROR_CODE;

class RegisterControllerTest extends AbstractControllerTest {
    private static final String CONFIRM_REGISTER_URL = REGISTER_URL + "/confirm";
    private static final String REGISTER_PAGE_VIEW = "users/register";

    @MockBean
    private MailSender mailSender;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RegisterTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${register.confirm-url}")
    private String confirmRegisterUrl;

    @Test
    void showRegisterPage() throws Exception {
        perform(MockMvcRequestBuilders.get(REGISTER_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USER_TO_ATTRIBUTE))
                .andExpect(view().name(REGISTER_PAGE_VIEW))
                .andExpect(result ->
                        USER_TO_MATCHER.assertMatch((UserTo) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(USER_TO_ATTRIBUTE), new UserTo()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showRegisterPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REGISTER_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void register() throws Exception {
        UserTo newUserTo = getNewTo();
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(getNewToParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION, messageSource.getMessage("register.check-your-email", null,
                        LocaleContextHolder.getLocale())));
        RegisterToken createdToken = tokenRepository.findByEmailIgnoreCase(newUserTo.getEmail()).orElseThrow();
        assertTrue(createdToken.getExpiryDate().after(new Date()));
        assertEquals(newUserTo.getName(), createdToken.getName());
        assertTrue(PASSWORD_ENCODER.matches(newUserTo.getPassword(), createdToken.getPassword()));
        Locale locale = LocaleContextHolder.getLocale();
        String confirmRegisterUrlLinkText = messageSource.getMessage("register.confirm-url-link-text", null, locale);
        String confirmRegisterMessageSubject = messageSource.getMessage("register.confirm-message-subject", null, locale);
        String confirmRegisterMessageText = messageSource.getMessage("register.confirm-message-text", null, locale);
        String link = String.format(CONFIRM_REGISTER_MESSAGE_LINK_TEMPLATE, confirmRegisterUrl, createdToken.getToken(),
                confirmRegisterUrlLinkText);
        String emailText = confirmRegisterMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(newUserTo.getEmail(), confirmRegisterMessageSubject, emailText);
    }

    @Test
    void registerWhenRegisterTokenExists() throws Exception {
        UserTo newUserTo = getNewTo();
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(getNewToParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION, messageSource.getMessage("register.check-your-email", null,
                        LocaleContextHolder.getLocale())));
        RegisterToken updatedToken = tokenRepository.findByEmailIgnoreCase(newUserTo.getEmail()).orElseThrow();
        assertTrue(updatedToken.getExpiryDate().after(new Date()));
        assertEquals(newUserTo.getName(), updatedToken.getName());
        assertTrue(PASSWORD_ENCODER.matches(newUserTo.getPassword(), updatedToken.getPassword()));
        Locale locale = LocaleContextHolder.getLocale();
        String confirmRegisterUrlLinkText = messageSource.getMessage("register.confirm-url-link-text", null, locale);
        String confirmRegisterMessageSubject = messageSource.getMessage("register.confirm-message-subject", null, locale);
        String confirmRegisterMessageText = messageSource.getMessage("register.confirm-message-text", null, locale);
        String link = String.format(CONFIRM_REGISTER_MESSAGE_LINK_TEMPLATE, confirmRegisterUrl, updatedToken.getToken(),
                confirmRegisterUrlLinkText);
        String emailText = confirmRegisterMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(newUserTo.getEmail(), confirmRegisterMessageSubject, emailText);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void registerAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(getNewToParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerInvalid() throws Exception {
        MultiValueMap<String, String> newToInvalidParams = getNewToInvalidParams();
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(newToInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(USER_TO_ATTRIBUTE, EMAIL, NAME, PASSWORD))
                .andExpect(view().name(REGISTER_PAGE_VIEW));
        assertTrue(tokenRepository.findByEmailIgnoreCase(newToInvalidParams.get(EMAIL).get(0)).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void registerDuplicateEmail() throws Exception {
        MultiValueMap<String, String> newToParams = getNewToParams();
        newToParams.set(EMAIL, USER_MAIL);
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(newToParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(USER_TO_ATTRIBUTE, EMAIL, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(REGISTER_PAGE_VIEW));
        assertTrue(tokenRepository.findByEmailIgnoreCase(newToParams.get(EMAIL).get(0)).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void confirmRegister() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_REGISTER_URL)
                .param(TOKEN, registerToken.getToken())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION, messageSource.getMessage("register.email-confirmed", null,
                        LocaleContextHolder.getLocale())));
        assertTrue(tokenRepository.findByToken(registerToken.getToken()).isEmpty());
        User created = userRepository.findByEmailIgnoreCase(registerToken.getEmail()).orElseThrow();
        User newUser = getNew();
        newUser.setId(created.id());
        USER_MATCHER.assertMatch(created, newUser);
        assertTrue(PASSWORD_ENCODER.matches(newUser.getPassword(), created.getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void confirmRegisterAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(CONFIRM_REGISTER_URL)
                .param(TOKEN, registerToken.getToken())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void confirmRegisterTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_REGISTER_URL)
                .param(TOKEN, UUID.randomUUID().toString())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("register.token-not-found", null,
                        LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void confirmRegisterTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_REGISTER_URL)
                .param(TOKEN, expiredRegisterToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("register.token-expired", null,
                        LocaleContextHolder.getLocale()), TokenExpiredException.class));
        assertTrue(userRepository.findByEmailIgnoreCase(expiredRegisterToken.getEmail()).isEmpty());
    }
}