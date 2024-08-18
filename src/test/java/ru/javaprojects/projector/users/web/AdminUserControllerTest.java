package ru.javaprojects.projector.users.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.service.UserService;
import ru.javaprojects.projector.users.to.UserTo;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.ACTION_ATTRIBUTE;
import static ru.javaprojects.projector.CommonTestData.getPageableParams;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.UserUtil.asTo;
import static ru.javaprojects.projector.users.web.AdminUserController.USERS_URL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.projector.users.web.UniqueEmailValidator.DUPLICATE_ERROR_CODE;

class AdminUserControllerTest extends AbstractControllerTest {
    private static final String USERS_ADD_FORM_URL = USERS_URL + "/add";
    private static final String USERS_CREATE_URL = USERS_URL + "/create";
    private static final String USERS_EDIT_FORM_URL = USERS_URL + "/edit/";
    private static final String USERS_UPDATE_URL = USERS_URL + "/update";
    private static final String USERS_VIEW = "users/users";
    private static final String USER_ADD_VIEW = "users/user-add-form";
    private static final String USER_EDIT_VIEW = "users/user-edit-form";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService service;

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(USERS_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USERS_ATTRIBUTE))
                .andExpect(view().name(USERS_VIEW));
        Page<User> users = (Page<User>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(USERS_ATTRIBUTE);
        assertEquals(4, users.getTotalElements());
        assertEquals(2, users.getTotalPages());
        USER_MATCHER.assertMatch(users.getContent(), List.of(user2, disabledUser));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAllByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(USERS_URL)
                .param(KEYWORD_PARAM, admin.getName()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USERS_ATTRIBUTE))
                .andExpect(view().name(USERS_VIEW));
        Page<User> users = (Page<User>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(USERS_ATTRIBUTE);
        assertEquals(1, users.getTotalElements());
        assertEquals(1, users.getTotalPages());
        USER_MATCHER.assertMatch(users.getContent(), List.of(admin));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL)
                .params(getPageableParams()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USER_ATTRIBUTE))
                .andExpect(view().name(USER_ADD_VIEW))
                .andExpect(result ->
                        USER_MATCHER.assertMatch((User) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(USER_ATTRIBUTE), new User()));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        User newUser = getNew();
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(getNewUserParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(USERS_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("user.created",
                        new Object[]{newUser.getName()}, LocaleContextHolder.getLocale())));
        User created = service.getByEmail(newUser.getEmail());
        newUser.setId(created.id());
        USER_MATCHER.assertMatch(created, newUser);
    }

    @Test
    void createUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(getNewUserParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> service.getByEmail(getNew().getEmail()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(getNewUserParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByEmail(getNew().getEmail()));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newUserInvalidParams = getNewUserInvalidParams();
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(newUserInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(USER_ATTRIBUTE, NAME_PARAM, PASSWORD_PARAM, EMAIL_PARAM, ROLES_PARAM))
                .andExpect(view().name(USER_ADD_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByEmail(newUserInvalidParams.get(EMAIL_PARAM).get(0)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateEmail() throws Exception {
        MultiValueMap<String, String> newUserParams = getNewUserParams();
        newUserParams.set(EMAIL_PARAM, USER_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(newUserParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(USER_ATTRIBUTE, EMAIL_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(USER_ADD_VIEW));
        assertNotEquals(getNew().getName(), service.getByEmail(USER_MAIL).getName());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_EDIT_FORM_URL + USER_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USER_TO_ATTRIBUTE))
                .andExpect(view().name(USER_EDIT_VIEW))
                .andExpect(result ->
                        USER_TO_MATCHER.assertMatch((UserTo)Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(USER_TO_ATTRIBUTE), asTo(user)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_EDIT_FORM_URL + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_EDIT_FORM_URL + USER_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_EDIT_FORM_URL + USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        User updatedUser = getUpdated();
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(getUpdatedUserParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(USERS_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("user.updated",
                        new Object[]{updatedUser.getName()}, LocaleContextHolder.getLocale())));
        USER_MATCHER.assertMatch(service.get(USER_ID), updatedUser);
    }

    //Check UniqueMailValidator works correct when update with same email
    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateEmailNotChange() throws Exception {
        User updatedUser = getUpdated();
        updatedUser.setEmail(USER_MAIL);
        MultiValueMap<String, String> updatedParams = getUpdatedUserParams();
        updatedParams.set(EMAIL_PARAM, USER_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(USERS_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("user.updated",
                        new Object[]{updatedUser.getName()}, LocaleContextHolder.getLocale())));
        USER_MATCHER.assertMatch(service.get(USER_ID), updatedUser);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedUserParams();
        updatedParams.set(ID_PARAM, String.valueOf(NOT_EXISTING_ID));
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(getUpdatedUserParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(USER_ID).getEmail(), getUpdated().getEmail());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(getUpdatedUserParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(USER_ID).getEmail(), getUpdated().getEmail());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedUserInvalidParams();
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(USER_TO_ATTRIBUTE, NAME_PARAM, EMAIL_PARAM, ROLES_PARAM))
                .andExpect(view().name(USER_EDIT_VIEW));
        assertNotEquals(service.get(USER_ID).getEmail(), updatedInvalidParams.get(EMAIL_PARAM).get(0));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateEmail() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedUserParams();
        updatedParams.set(EMAIL_PARAM, ADMIN_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(USER_TO_ATTRIBUTE, EMAIL_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(USER_EDIT_VIEW));
        assertNotEquals(service.get(USER_ID).getEmail(), ADMIN_MAIL);
    }
}
