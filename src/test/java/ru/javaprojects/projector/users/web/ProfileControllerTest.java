package ru.javaprojects.projector.users.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.TestContentFilesManager;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.error.TokenException;
import ru.javaprojects.projector.users.error.UserDisabledException;
import ru.javaprojects.projector.common.mail.MailSender;
import ru.javaprojects.projector.users.model.token.ChangeEmailToken;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.ChangeEmailTokenRepository;
import ru.javaprojects.projector.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.projector.users.service.UserService;
import ru.javaprojects.projector.users.to.PasswordResetTo;
import ru.javaprojects.projector.users.to.ProfileTo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.ACTION_ATTRIBUTE;
import static ru.javaprojects.projector.CommonTestData.NAME_PARAM;
import static ru.javaprojects.projector.app.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.common.validation.UniqueNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.service.TokenService.CONFIRMATION_LINK_TEMPLATE;
import static ru.javaprojects.projector.users.util.UserUtil.asProfileTo;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.projector.users.web.ProfileController.PROFILE_URL;
import static ru.javaprojects.projector.users.web.UserManagementControllerTest.AVATARS_TEST_DATA_FILES_PATH;

class ProfileControllerTest extends AbstractControllerTest implements TestContentFilesManager {
    private static final String PROFILE_RESET_PASSWORD_URL = PROFILE_URL + "/reset-password";
    private static final String RESET_PASSWORD_VIEW = "profile/reset-password";
    private static final String PROFILE_VIEW = "profile/profile";
    private static final String CONFIRM_CHANGE_EMAIL_URL = PROFILE_URL + "/change-email/confirm";
    private static final String PROFILE_EDIT_FORM_URL = PROFILE_URL + "/edit";
    private static final String PROFILE_EDIT_VIEW = "profile/profile-edit-form";

    @Value("${content-path.avatars}")
    private String contentPath;

    @Value("${change-email.confirm-url}")
    private String confirmChangeEmailUrl;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private ChangeEmailTokenRepository changeEmailTokenRepository;

    @MockBean
    private MailSender mailSender;

    @Override
    public Path getContentPath() {
        return Paths.get(contentPath);
    }

    @Override
    public Path getTestDataFilesPath() {
        return Paths.get(AVATARS_TEST_DATA_FILES_PATH);
    }

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
        assertTrue(Files.exists(Paths.get(updated.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(user2.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(contentPath + user2.getEmail().toLowerCase().replace(' ', '_'))));
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

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditProfilePage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_EDIT_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PROFILE_TO_ATTRIBUTE, asProfileTo(user)))
                .andExpect(view().name(PROFILE_EDIT_VIEW))
                .andExpect(result ->
                        PROFILE_TO_MATCHER.assertMatch((ProfileTo) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(PROFILE_TO_ATTRIBUTE), asProfileTo(user)));
    }

    @Test
    void showEditProfilePageUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_EDIT_FORM_URL)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWithoutEmailChange() throws Exception {
        User updatedProfileUser = getUpdatedProfileUser(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(getUpdatedProfileParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROFILE_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("profile.updated",
                       null, LocaleContextHolder.getLocale())));

        USER_MATCHER.assertMatch(userService.get(USER_ID), updatedProfileUser);
        assertTrue(Files.exists(Paths.get(updatedProfileUser.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(user.getAvatar().getFileLink())));

        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWithEmailChange() throws Exception {
        User updatedProfileUser = getUpdatedProfileUser(contentPath);
        MultiValueMap<String, String> updatedProfileParams = getUpdatedProfileParams(contentPath);
        updatedProfileParams.set(EMAIL_PARAM, NEW_EMAIL);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(updatedProfileParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROFILE_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("profile.updated.confirm-email",
                        null, LocaleContextHolder.getLocale())));

        USER_MATCHER.assertMatch(userService.get(USER_ID), updatedProfileUser);
        assertTrue(Files.exists(Paths.get(updatedProfileUser.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(user.getAvatar().getFileLink())));

        ChangeEmailToken createdToken = changeEmailTokenRepository.findByUser_Id(USER_ID).orElseThrow();
        assertTrue(createdToken.getExpiryDate().after(new Date()));
        Locale locale = LocaleContextHolder.getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.message-link-text", null, locale);
        String changeEmailMessageSubject = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, confirmChangeEmailUrl, createdToken.getToken(),
                changeEmailUrlLinkText);
        String emailText = changeEmailMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(NEW_EMAIL, changeEmailMessageSubject, emailText);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWithoutEmailChangeWhenLogoFileIsBytesArray() throws Exception {
        User updatedProfileUser = getUpdatedProfileUser(contentPath);
        MultiValueMap<String, String> updatedParams = getUpdatedProfileParams(contentPath);
        updatedParams.add(AVATAR_FILE_AS_BYTES_PARAM,  Arrays.toString(UPDATED_AVATAR_FILE.getBytes()));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROFILE_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("profile.updated",
                        null, LocaleContextHolder.getLocale())));

        USER_MATCHER.assertMatch(userService.get(USER_ID), updatedProfileUser);
        assertTrue(Files.exists(Paths.get(updatedProfileUser.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(user.getAvatar().getFileLink())));

        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWithoutEmailChangeWhenAvatarNotUpdated() throws Exception {
        User updatedProfileUser = getUpdatedProfileUserWhenOldAvatar(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .params(getUpdatedProfileParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROFILE_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("profile.updated",
                        null, LocaleContextHolder.getLocale())));

        USER_MATCHER.assertMatch(userService.get(USER_ID), updatedProfileUser);
        assertTrue(Files.exists(Paths.get(user.getAvatar().getFileLink())));

        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(getUpdatedProfileParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(userService.get(USER_ID).getName(), getUpdatedProfileUser(contentPath).getName());
        assertTrue(Files.exists(Paths.get(user.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdatedProfileUser(contentPath).getAvatar().getFileLink())));

        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedProfileInvalidParams = getUpdatedProfileInvalidParams(contentPath);
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(updatedProfileInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PROFILE_TO_ATTRIBUTE, NAME_PARAM, EMAIL_PARAM))
                .andExpect(view().name(PROFILE_EDIT_VIEW));

        assertArrayEquals(UPDATED_AVATAR_FILE.getBytes(),
                ((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROFILE_TO_ATTRIBUTE))
                        .getAvatar().getInputtedFileBytes());
        assertEquals(UPDATED_AVATAR_FILE.getOriginalFilename(),
                ((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROFILE_TO_ATTRIBUTE))
                        .getAvatar().getFileName());
        assertNull(((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROFILE_TO_ATTRIBUTE))
                .getAvatar().getFileLink());
        assertNotEquals(userService.get(USER_ID).getName(), updatedProfileInvalidParams.get(NAME_PARAM).get(0));
        assertTrue(Files.exists(Paths.get(user.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdatedProfileUser(contentPath).getAvatar().getFileLink())));

        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateDuplicateEmail() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedProfileParams(contentPath);
        updatedParams.set(EMAIL_PARAM, admin.getEmail());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(PROFILE_TO_ATTRIBUTE, EMAIL_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(PROFILE_EDIT_VIEW));

        assertArrayEquals(UPDATED_AVATAR_FILE.getBytes(),
                ((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROFILE_TO_ATTRIBUTE))
                        .getAvatar().getInputtedFileBytes());
        assertEquals(UPDATED_AVATAR_FILE.getOriginalFilename(),
                ((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROFILE_TO_ATTRIBUTE))
                        .getAvatar().getFileName());
        assertNull(((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROFILE_TO_ATTRIBUTE))
                .getAvatar().getFileLink());

        assertNotEquals(userService.get(USER_ID).getEmail(), admin.getEmail());
        assertTrue(Files.exists(Paths.get(admin.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(contentPath + admin.getEmail() + "/" + UPDATED_AVATAR_FILE.getOriginalFilename().toLowerCase())));

        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
