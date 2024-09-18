package ru.javaprojects.projector.reference.architectures.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.reference.architectures.Architecture;
import ru.javaprojects.projector.reference.architectures.ArchitectureService;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.*;
import static ru.javaprojects.projector.common.util.validation.UniqueNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.projector.reference.architectures.ArchitectureTestData.*;
import static ru.javaprojects.projector.reference.architectures.web.ArchitectureController.ARCHITECTURES_URL;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ArchitectureControllerTest extends AbstractControllerTest {
    private static final String ARCHITECTURES_VIEW = "management/reference/architectures";
    private static final String ARCHITECTURES_ADD_FORM_URL = ARCHITECTURES_URL + "/add";
    private static final String ARCHITECTURE_FORM_VIEW = "management/reference/architecture-form";
    private static final String ARCHITECTURES_EDIT_FORM_URL = ARCHITECTURES_URL + "/edit/";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ArchitectureService architectureService;

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ARCHITECTURES_ATTRIBUTE))
                .andExpect(view().name(ARCHITECTURES_VIEW))
                .andExpect(result -> ARCHITECTURE_MATCHER.assertMatch((List<Architecture>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(ARCHITECTURES_ATTRIBUTE), List.of(architecture2, architecture1)));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ARCHITECTURE_ATTRIBUTE))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        Architecture newArchitecture = getNew();
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.created",
                        new Object[]{newArchitecture.getName()}, LocaleContextHolder.getLocale())));
        Architecture created = architectureService.getByName(newArchitecture.getName());
        newArchitecture.setId(created.getId());
        ARCHITECTURE_MATCHER.assertMatch(created, newArchitecture);
    }

    @Test
    void createUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> architectureService.getByName(getNew().getName()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> architectureService.getByName(getNew().getName()));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(ARCHITECTURE_ATTRIBUTE, NAME_PARAM, DESCRIPTION_PARAM))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> architectureService.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(NAME_PARAM, architecture1.getName());
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(ARCHITECTURE_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
        assertNotEquals(getNew().getDescription(), architectureService.getByName(architecture1.getName()).getDescription());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_EDIT_FORM_URL + ARCHITECTURE1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ARCHITECTURE_ATTRIBUTE))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_EDIT_FORM_URL + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_EDIT_FORM_URL + ARCHITECTURE1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_EDIT_FORM_URL + ARCHITECTURE1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        Architecture updatedArchitecture = getUpdated();
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.updated",
                        new Object[]{updatedArchitecture.getName()}, LocaleContextHolder.getLocale())));
        ARCHITECTURE_MATCHER.assertMatch(architectureService.get(ARCHITECTURE1_ID), updatedArchitecture);
    }

    //Check UniqueNameValidator works correct when update with same name
    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNameNotChange() throws Exception {
        Architecture updatedArchitecture = getUpdated();
        updatedArchitecture.setName(architecture1.getName());
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set(NAME_PARAM, architecture1.getName());
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.updated",
                        new Object[]{updatedArchitecture.getName()}, LocaleContextHolder.getLocale())));
        ARCHITECTURE_MATCHER.assertMatch(architectureService.get(ARCHITECTURE1_ID), updatedArchitecture);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set(ID_PARAM, String.valueOf(NOT_EXISTING_ID));
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(architectureService.get(ARCHITECTURE1_ID).getName(), getUpdated().getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(architectureService.get(ARCHITECTURE1_ID).getName(), getUpdated().getName());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(getUpdatedInvalidParams())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(ARCHITECTURE_ATTRIBUTE, NAME_PARAM, DESCRIPTION_PARAM))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
        assertNotEquals(architectureService.get(ARCHITECTURE1_ID).getName(), getUpdated().getName());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set(NAME_PARAM, architecture2.getName());
        perform(MockMvcRequestBuilders.post(ARCHITECTURES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(ARCHITECTURE_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
        assertNotEquals(architectureService.get(ARCHITECTURE1_ID).getName(), architecture2.getName());
    }
}
