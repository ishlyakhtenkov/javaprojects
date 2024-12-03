package ru.javaprojects.javaprojects.reference.technologies.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.javaprojects.AbstractControllerTest;
import ru.javaprojects.javaprojects.ContentFilesManager;
import ru.javaprojects.javaprojects.common.error.IllegalRequestDataException;
import ru.javaprojects.javaprojects.common.error.NotFoundException;
import ru.javaprojects.javaprojects.common.model.Priority;
import ru.javaprojects.javaprojects.common.util.FileUtil;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyRepository;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyService;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyTo;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyUtil;
import ru.javaprojects.javaprojects.reference.technologies.model.Technology;
import ru.javaprojects.javaprojects.reference.technologies.model.Usage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.javaprojects.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.javaprojects.common.CommonTestData.*;
import static ru.javaprojects.javaprojects.reference.technologies.TechnologyTestData.*;
import static ru.javaprojects.javaprojects.reference.technologies.web.TechnologyController.TECHNOLOGIES_URL;
import static ru.javaprojects.javaprojects.reference.technologies.web.UniqueTechnologyNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.javaprojects.users.UserTestData.*;
import static ru.javaprojects.javaprojects.users.web.LoginController.LOGIN_URL;

class TechnologyControllerTest extends AbstractControllerTest implements ContentFilesManager {
    private static final String TECHNOLOGIES_ADD_URL = TECHNOLOGIES_URL + "/add";
    private static final String TECHNOLOGIES_EDIT_URL = TECHNOLOGIES_URL + "/edit/";
    private static final String TECHNOLOGIES_VIEW = "management/reference/technologies";
    private static final String TECHNOLOGY_FORM_VIEW = "management/reference/technology-form";

    @Value("${content-path.technologies}")
    private String technologyFilesPath;

    @Autowired
    private TechnologyService service;

    @Autowired
    private TechnologyRepository repository;

    @Override
    public Path getContentPath() {
        return Paths.get(technologyFilesPath);
    }

    @Override
    public Path getContentFilesPath() {
        return Paths.get(TECHNOLOGIES_TEST_CONTENT_FILES_PATH);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void showTechnologiesPage() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(TECHNOLOGIES_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(TECHNOLOGIES_ATTRIBUTE))
                .andExpect(view().name(TECHNOLOGIES_VIEW));
        Page<Technology> technologies = (Page<Technology>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(TECHNOLOGIES_ATTRIBUTE);
        assertEquals(4, technologies.getTotalElements());
        assertEquals(2, technologies.getTotalPages());
        TECHNOLOGY_MATCHER.assertMatch(technologies.getContent(), List.of(technology3, technology1));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void showTechnologiesPageSearchByKeyword() throws Exception {
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
    void showTechnologiesPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showTechnologiesPageForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_URL)
                .params(getPageableParams()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddPage() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_ADD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(TECHNOLOGY_TO_ATTRIBUTE))
                .andExpect(model().attribute(USAGES_ATTRIBUTE, Usage.values()))
                .andExpect(model().attribute(PRIORITIES_ATTRIBUTE, Priority.values()))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
    }

    @Test
    void showAddPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_ADD_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddPageForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_ADD_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        TechnologyTo newTechnologyTo = getNewTo();
        Technology newTechnology = getNew(technologyFilesPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(NEW_LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.created",
                        new Object[]{newTechnologyTo.getName()}, getLocale())));

        Technology created = repository.findByNameIgnoreCase(newTechnologyTo.getName()).orElseThrow();
        newTechnology.setId(created.getId());
        TECHNOLOGY_MATCHER.assertMatch(created, newTechnology);
        assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWhenLogoIsBytesArray() throws Exception {
        MultipartFile logoFile = getNewTo().getLogo().getInputtedFile();
        Technology newTechnology = getNew(technologyFilesPath);
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.add(LOGO_FILE_NAME_PARAM, logoFile.getOriginalFilename());
        newParams.add(LOGO_INPUTTED_FILE_BYTES_PARAM,  Arrays.toString(logoFile.getBytes()));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .params((newParams))
                .with(csrf()))
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.created",
                        new Object[]{newTechnology.getName()}, getLocale())));
        Technology created = repository.findByNameIgnoreCase(newTechnology.getName()).orElseThrow();
        newTechnology.setId(created.getId());
        TECHNOLOGY_MATCHER.assertMatch(created, newTechnology);
        assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
    }

    @Test
    void createUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(NEW_LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(() -> repository.findByNameIgnoreCase(getNewTo().getName()).isEmpty());
        assertTrue(Files.notExists(Paths.get(getNew(technologyFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(NEW_LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(() -> repository.findByNameIgnoreCase(getNewTo().getName()).isEmpty());
        assertTrue(Files.notExists(Paths.get(getNew(technologyFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(NEW_LOGO_FILE)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, URL_PARAM))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertArrayEquals(getNewTo().getLogo().getInputtedFile().getBytes(),
                ((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(getNewTo().getLogo().getInputtedFile().getOriginalFilename(),
                ((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getFileLink());
        assertTrue(() -> repository.findByNameIgnoreCase(newInvalidParams.get(NAME_PARAM).get(0)).isEmpty());
        assertTrue(Files.notExists(Paths.get(technologyFilesPath,
                FileUtil.normalizePath(newInvalidParams.get(NAME_PARAM).get(0) + "/" + NEW_LOGO_FILE.getOriginalFilename()))));

    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithoutLogo() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("technology.logo-not-present", null, getLocale()),
                        IllegalRequestDataException.class));
        assertTrue(() -> repository.findByNameIgnoreCase(newParams.get(NAME_PARAM).get(0)).isEmpty());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(NAME_PARAM, technology1.getName());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(NEW_LOGO_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertArrayEquals(getNewTo().getLogo().getInputtedFile().getBytes(),
                ((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(getNewTo().getLogo().getInputtedFile().getOriginalFilename(),
                ((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getFileLink());
        assertNotEquals(getNew(technologyFilesPath).getUrl(), repository.findByNameIgnoreCase(technology1.getName())
                .orElseThrow().getUrl());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditPage() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_EDIT_URL + TECHNOLOGY1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute(TECHNOLOGY_TO_ATTRIBUTE, TechnologyUtil.asTo(technology1)))
                .andExpect(model().attribute(USAGES_ATTRIBUTE, Usage.values()))
                .andExpect(model().attribute(PRIORITIES_ATTRIBUTE, Priority.values()))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditPageNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_EDIT_URL + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void showEditPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_EDIT_URL + TECHNOLOGY1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditPageForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(TECHNOLOGIES_EDIT_URL + TECHNOLOGY1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        Technology updatedTechnology = getUpdated(technologyFilesPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(technologyFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.updated",
                        new Object[]{updatedTechnology.getName()}, getLocale())));
        TECHNOLOGY_MATCHER.assertMatch(service.get(TECHNOLOGY1_ID), updatedTechnology);
        assertTrue(Files.exists(Paths.get(updatedTechnology.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technology1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technologyFilesPath + FileUtil.normalizePath(technology1.getName()))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenLogoIsBytesArray() throws Exception {
        Technology updatedTechnology = getUpdated(technologyFilesPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(technologyFilesPath);
        updatedParams.add(LOGO_INPUTTED_FILE_BYTES_PARAM,  Arrays.toString(UPDATED_LOGO_FILE.getBytes()));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.updated",
                        new Object[]{updatedTechnology.getName()}, getLocale())));
        TECHNOLOGY_MATCHER.assertMatch(service.get(TECHNOLOGY1_ID), updatedTechnology);
        assertTrue(Files.exists(Paths.get(updatedTechnology.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technology1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technologyFilesPath + FileUtil.normalizePath(technology1.getName()))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWithoutChangingLogo() throws Exception {
        Technology updatedTechnology = getUpdatedWithOldLogo(technologyFilesPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .params(getUpdatedParams(technologyFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.updated",
                        new Object[]{updatedTechnology.getName()}, getLocale())));
        TECHNOLOGY_MATCHER.assertMatch(service.get(TECHNOLOGY1_ID), updatedTechnology);
        assertTrue(Files.exists(Paths.get(updatedTechnology.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technology1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technologyFilesPath + FileUtil.normalizePath(technology1.getName()))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWithoutChangingName() throws Exception {
        Technology updatedTechnology = getUpdatedWithOldName(technologyFilesPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(technologyFilesPath);
        updatedParams.set(NAME_PARAM, technology1.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.updated",
                        new Object[]{updatedTechnology.getName()}, getLocale())));
        TECHNOLOGY_MATCHER.assertMatch(service.get(TECHNOLOGY1_ID), updatedTechnology);
        assertTrue(Files.exists(Paths.get(updatedTechnology.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technology1.getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(technologyFilesPath);
        updatedParams.set(ID_PARAM, String.valueOf(NOT_EXISTING_ID));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void updateUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(technologyFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(TECHNOLOGY1_ID).getName(), getUpdated(technologyFilesPath).getName());
        assertTrue(Files.exists(Paths.get(technology1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(technologyFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(technologyFilesPath))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(TECHNOLOGY1_ID).getName(), getUpdated(technologyFilesPath).getName());
        assertTrue(Files.exists(Paths.get(technology1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(technologyFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedInvalidParams(technologyFilesPath);
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, URL_PARAM))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));

        assertArrayEquals(UPDATED_LOGO_FILE.getBytes(),
                ((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(UPDATED_LOGO_FILE.getOriginalFilename(),
                ((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getFileLink());
        assertNotEquals(service.get(TECHNOLOGY1_ID).getName(), updatedInvalidParams.get(NAME_PARAM).get(0));
        assertTrue(Files.exists(Paths.get(technology1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(technologyFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(technologyFilesPath);
        updatedParams.set(NAME_PARAM, technology2.getName());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertArrayEquals(UPDATED_LOGO_FILE.getBytes(),
                ((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(UPDATED_LOGO_FILE.getOriginalFilename(),
                ((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((TechnologyTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(TECHNOLOGY_TO_ATTRIBUTE)).getLogo().getFileLink());
        assertNotEquals(service.get(TECHNOLOGY1_ID).getName(), technology2.getName());
        assertTrue(Files.exists(Paths.get(technology2.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(technologyFilesPath +
                FileUtil.normalizePath(technology2.getName() + "/" + UPDATED_LOGO_FILE.getOriginalFilename()))));
    }
}
