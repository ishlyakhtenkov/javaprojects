package ru.javaprojects.projector.references.technologies.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.TestContentFilesManager;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.references.technologies.TechnologyService;
import ru.javaprojects.projector.references.technologies.TechnologyTo;
import ru.javaprojects.projector.references.technologies.model.Technology;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.*;
import static ru.javaprojects.projector.references.technologies.TechnologyTestData.*;
import static ru.javaprojects.projector.references.technologies.UniqueTechnologyNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.projector.references.technologies.web.TechnologyController.TECHNOLOGIES_URL;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class TechnologyControllerTest extends AbstractControllerTest implements TestContentFilesManager {
    private static final String TECHNOLOGIES_VIEW = "references/technologies";
    private static final String TECHNOLOGIES_ADD_FORM_URL = TECHNOLOGIES_URL + "/add";
    private static final String TECHNOLOGY_FORM_VIEW = "references/technology-form";
    private static final String TECHNOLOGIES_EDIT_FORM_URL = TECHNOLOGIES_URL + "/edit/";

    @Value("${content-path.technologies}")
    private String contentPath;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TechnologyService technologyService;

    @Override
    public Path getContentPath() {
        return Paths.get(contentPath);
    }

    @Override
    public Path getTestDataFilesPath() {
        return Paths.get(TECHNOLOGIES_TEST_DATA_FILES_PATH);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(TECHNOLOGIES_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(TECHNOLOGIES_ATTRIBUTE))
                .andExpect(view().name(TECHNOLOGIES_VIEW));
        Page<Technology> technologies = (Page<Technology>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(TECHNOLOGIES_ATTRIBUTE);
        assertEquals(3, technologies.getTotalElements());
        assertEquals(2, technologies.getTotalPages());
        TECHNOLOGY_MATCHER.assertMatch(technologies.getContent(), List.of(technology3, technology1));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAllByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(TECHNOLOGIES_URL)
                .param(KEYWORD_PARAM, technology1.getName()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(TECHNOLOGIES_ATTRIBUTE))
                .andExpect(view().name(TECHNOLOGIES_VIEW));
        Page<Technology> technologies = (Page<Technology>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(TECHNOLOGIES_ATTRIBUTE);
        assertEquals(1, technologies.getTotalElements());
        assertEquals(1, technologies.getTotalPages());
        TECHNOLOGY_MATCHER.assertMatch(technologies.getContent(), List.of(technology1));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_URL)
                .params(getPageableParams()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(TECHNOLOGY_TO_ATTRIBUTE))
                .andExpect(model().attributeExists(USAGES_ATTRIBUTE))
                .andExpect(model().attributeExists(PRIORITIES_ATTRIBUTE))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        TechnologyTo newTechnologyTo = getNewTo();
        Technology newTechnology = getNew(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.created",
                        new Object[]{newTechnologyTo.getName()}, LocaleContextHolder.getLocale())));

        Technology created = technologyService.getByName(newTechnologyTo.getName());
        newTechnology.setId(created.getId());
        TECHNOLOGY_MATCHER.assertMatch(created, newTechnology);
        assertTrue(Files.exists(Paths.get(created.getLogoFile().getFileLink())));
    }

    @Test
    void createUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> technologyService.getByName(getNewTo().getName()));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getLogoFile().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> technologyService.getByName(getNewTo().getName()));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getLogoFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(LOGO_FILE)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, URL_PARAM))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> technologyService.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + "/" +
                LOGO_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(NAME_PARAM, technology1.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(LOGO_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertNotEquals(getNew(contentPath).getUrl(), technologyService.getByName(technology1.getName()).getUrl());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_EDIT_FORM_URL + TECHNOLOGY1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(TECHNOLOGY_TO_ATTRIBUTE))
                .andExpect(model().attributeExists(USAGES_ATTRIBUTE))
                .andExpect(model().attributeExists(PRIORITIES_ATTRIBUTE))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_EDIT_FORM_URL + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_EDIT_FORM_URL + TECHNOLOGY1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_EDIT_FORM_URL + TECHNOLOGY1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        Technology updatedTechnology = getUpdated(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.updated",
                        new Object[]{updatedTechnology.getName()}, LocaleContextHolder.getLocale())));

        TECHNOLOGY_MATCHER.assertMatch(technologyService.get(TECHNOLOGY1_ID), updatedTechnology);
        assertTrue(Files.exists(Paths.get(updatedTechnology.getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technology1.getLogoFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenLogoNotUpdated() throws Exception {
        Technology updatedTechnology = getUpdatedWhenOldLogo(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.updated",
                        new Object[]{updatedTechnology.getName()}, LocaleContextHolder.getLocale())));

        TECHNOLOGY_MATCHER.assertMatch(technologyService.get(TECHNOLOGY1_ID), updatedTechnology);
        assertTrue(Files.exists(Paths.get(updatedTechnology.getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technology1.getLogoFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenNameNotUpdated() throws Exception {
        Technology updatedTechnology = getUpdatedWhenOldName(contentPath);
        MultiValueMap<String, String> updatedToParams = getUpdatedParams(contentPath);
        updatedToParams.set(NAME_PARAM, technology1.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedToParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.updated",
                        new Object[]{updatedTechnology.getName()}, LocaleContextHolder.getLocale())));

        TECHNOLOGY_MATCHER.assertMatch(technologyService.get(TECHNOLOGY1_ID), updatedTechnology);
        assertTrue(Files.exists(Paths.get(updatedTechnology.getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technology1.getLogoFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedToParams = getUpdatedParams(contentPath);
        updatedToParams.set(ID_PARAM, String.valueOf(NOT_EXISTING_ID));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedToParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(technologyService.get(TECHNOLOGY1_ID).getName(), getUpdated(contentPath).getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(technologyService.get(TECHNOLOGY1_ID).getName(), getUpdated(contentPath).getName());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedInvalidParams(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, URL_PARAM))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertNotEquals(technologyService.get(TECHNOLOGY1_ID).getName(), getUpdated(contentPath).getName());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedToParams = getUpdatedParams(contentPath);
        updatedToParams.set(NAME_PARAM, technology2.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedToParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertNotEquals(technologyService.get(TECHNOLOGY1_ID).getName(), technology2.getName());
    }
}