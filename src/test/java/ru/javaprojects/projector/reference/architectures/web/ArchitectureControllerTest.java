package ru.javaprojects.projector.reference.architectures.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.TestContentFilesManager;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.reference.architectures.model.Architecture;
import ru.javaprojects.projector.reference.architectures.ArchitectureService;
import ru.javaprojects.projector.reference.architectures.ArchitectureTo;
import ru.javaprojects.projector.reference.architectures.ArchitectureUtil;

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
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.common.CommonTestData.*;
import static ru.javaprojects.projector.common.validation.UniqueNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.projector.reference.architectures.ArchitectureTestData.*;
import static ru.javaprojects.projector.reference.architectures.web.ArchitectureController.ARCHITECTURES_URL;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ArchitectureControllerTest extends AbstractControllerTest implements TestContentFilesManager {
    private static final String ARCHITECTURES_ADD_URL = ARCHITECTURES_URL + "/add";
    private static final String ARCHITECTURES_EDIT_URL = ARCHITECTURES_URL + "/edit/";
    private static final String ARCHITECTURES_VIEW = "management/reference/architectures";
    private static final String ARCHITECTURE_FORM_VIEW = "management/reference/architecture-form";

    @Value("${content-path.architectures}")
    private String architectureFilesPath;
    
    @Autowired
    private ArchitectureService architectureService;

    @Override
    public Path getContentPath() {
        return Paths.get(architectureFilesPath);
    }

    @Override
    public Path getTestDataFilesPath() {
        return Paths.get(ARCHITECTURES_TEST_DATA_FILES_PATH);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void showArchitecturesPage() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ARCHITECTURES_ATTRIBUTE))
                .andExpect(view().name(ARCHITECTURES_VIEW))
                .andExpect(result -> ARCHITECTURE_MATCHER
                        .assertMatch((List<Architecture>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(ARCHITECTURES_ATTRIBUTE), List.of(architecture2, architecture1)));
    }

    @Test
    void showArchitecturesPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showArchitecturesPageForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddPage() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_ADD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ARCHITECTURE_TO_ATTRIBUTE))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
    }

    @Test
    void showAddPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_ADD_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddPageForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_ADD_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        Architecture newArchitecture = getNew(architectureFilesPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(NEW_LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.created",
                        new Object[]{newArchitecture.getName()}, getLocale())));
        Architecture created = architectureService.getByName(newArchitecture.getName());
        newArchitecture.setId(created.getId());
        ARCHITECTURE_MATCHER.assertMatch(created, newArchitecture);
        assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWhenLogoIsBytesArray() throws Exception {
        MultipartFile logoFile = getNewTo().getLogo().getInputtedFile();
        Architecture newArchitecture = getNew(architectureFilesPath);
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.add(LOGO_FILE_NAME_PARAM, logoFile.getOriginalFilename());
        newParams.add(LOGO_INPUTTED_FILE_BYTES_PARAM,  Arrays.toString(logoFile.getBytes()));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .params((newParams))
                .with(csrf()))
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.created",
                        new Object[]{newArchitecture.getName()}, getLocale())));
        Architecture created = architectureService.getByName(newArchitecture.getName());
        newArchitecture.setId(created.getId());
        ARCHITECTURE_MATCHER.assertMatch(created, newArchitecture);
        assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
    }

    @Test
    void createUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(NEW_LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> architectureService.getByName(getNew(architectureFilesPath).getName()));
        assertTrue(Files.notExists(Paths.get(getNew(architectureFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(NEW_LOGO_FILE)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> architectureService.getByName(getNew(architectureFilesPath).getName()));
        assertTrue(Files.notExists(Paths.get(getNew(architectureFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(NEW_LOGO_FILE)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(ARCHITECTURE_TO_ATTRIBUTE, NAME_PARAM, DESCRIPTION_PARAM))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
        assertArrayEquals(getNewTo().getLogo().getInputtedFile().getBytes(),
                ((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(getNewTo().getLogo().getInputtedFile().getOriginalFilename(),
                ((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getFileLink());
        assertThrows(NotFoundException.class, () -> architectureService.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(architectureFilesPath,
                FileUtil.normalizePath(newInvalidParams.get(NAME_PARAM).get(0) + "/" + NEW_LOGO_FILE.getOriginalFilename()))));

    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithoutLogo() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("architecture.logo-not-present", null, getLocale()), 
                        IllegalRequestDataException.class));
        assertThrows(NotFoundException.class, () -> architectureService.getByName(newParams.get(NAME_PARAM).get(0)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(NAME_PARAM, architecture1.getName());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(NEW_LOGO_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(ARCHITECTURE_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
        assertArrayEquals(getNewTo().getLogo().getInputtedFile().getBytes(),
                ((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(getNewTo().getLogo().getInputtedFile().getOriginalFilename(),
                ((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getFileLink());
        assertNotEquals(getNew(architectureFilesPath).getDescription(),
                architectureService.getByName(architecture1.getName()).getDescription());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditPage() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_EDIT_URL + ARCHITECTURE1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute(ARCHITECTURE_TO_ATTRIBUTE, ArchitectureUtil.asTo(architecture1)))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditPageNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_EDIT_URL + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void showEditPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_EDIT_URL + ARCHITECTURE1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditPageForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(ARCHITECTURES_EDIT_URL + ARCHITECTURE1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        Architecture updatedArchitecture = getUpdated(architectureFilesPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(architectureFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.updated",
                        new Object[]{updatedArchitecture.getName()}, getLocale())));
        ARCHITECTURE_MATCHER.assertMatch(architectureService.getWithLocalizedFields(ARCHITECTURE1_ID), updatedArchitecture);
        assertTrue(Files.exists(Paths.get(updatedArchitecture.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(architecture1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(architectureFilesPath + FileUtil.normalizePath(architecture1.getName()))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenLogoIsBytesArray() throws Exception {
        Architecture updatedArchitecture = getUpdated(architectureFilesPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(architectureFilesPath);
        updatedParams.add(LOGO_INPUTTED_FILE_BYTES_PARAM,  Arrays.toString(UPDATED_LOGO_FILE.getBytes()));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.updated",
                        new Object[]{updatedArchitecture.getName()}, getLocale())));
        ARCHITECTURE_MATCHER.assertMatch(architectureService.getWithLocalizedFields(ARCHITECTURE1_ID), updatedArchitecture);
        assertTrue(Files.exists(Paths.get(updatedArchitecture.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(architecture1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(architectureFilesPath + FileUtil.normalizePath(architecture1.getName()))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWithoutChangingLogo() throws Exception {
        Architecture updatedArchitecture = getUpdatedWithOldLogo(architectureFilesPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .params(getUpdatedParams(architectureFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.updated",
                        new Object[]{updatedArchitecture.getName()}, getLocale())));
        ARCHITECTURE_MATCHER.assertMatch(architectureService.getWithLocalizedFields(ARCHITECTURE1_ID), updatedArchitecture);
        assertTrue(Files.exists(Paths.get(updatedArchitecture.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(architecture1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(architectureFilesPath + FileUtil.normalizePath(architecture1.getName()))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWithoutChangingName() throws Exception {
        Architecture updatedArchitecture = getUpdatedWithOldName(architectureFilesPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(architectureFilesPath);
        updatedParams.set(NAME_PARAM, architecture1.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ARCHITECTURES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("architecture.updated",
                        new Object[]{updatedArchitecture.getName()}, getLocale())));
        ARCHITECTURE_MATCHER.assertMatch(architectureService.getWithLocalizedFields(ARCHITECTURE1_ID), updatedArchitecture);
        assertTrue(Files.exists(Paths.get(updatedArchitecture.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(architecture1.getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(architectureFilesPath);
        updatedParams.set(ID_PARAM, String.valueOf(NOT_EXISTING_ID));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void updateUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(architectureFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(architectureService.get(ARCHITECTURE1_ID).getName(), getUpdated(architectureFilesPath).getName());
        assertTrue(Files.exists(Paths.get(architecture1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(architectureFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(getUpdatedParams(architectureFilesPath))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(architectureService.get(ARCHITECTURE1_ID).getName(), getUpdated(architectureFilesPath).getName());
        assertTrue(Files.exists(Paths.get(architecture1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(architectureFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedInvalidParams(architectureFilesPath);
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(ARCHITECTURE_TO_ATTRIBUTE, NAME_PARAM, DESCRIPTION_PARAM))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
        assertArrayEquals(UPDATED_LOGO_FILE.getBytes(),
                ((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(UPDATED_LOGO_FILE.getOriginalFilename(),
                ((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getFileLink());
        assertNotEquals(architectureService.get(ARCHITECTURE1_ID).getName(), getUpdated(architectureFilesPath).getName());
        assertTrue(Files.exists(Paths.get(architecture1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(architectureFilesPath).getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(architectureFilesPath);
        updatedParams.set(NAME_PARAM, architecture2.getName());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ARCHITECTURES_URL)
                .file(UPDATED_LOGO_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(ARCHITECTURE_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(ARCHITECTURE_FORM_VIEW));
        assertArrayEquals(UPDATED_LOGO_FILE.getBytes(),
                ((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(UPDATED_LOGO_FILE.getOriginalFilename(),
                ((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((ArchitectureTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(ARCHITECTURE_TO_ATTRIBUTE)).getLogo().getFileLink());
        assertNotEquals(architectureService.get(ARCHITECTURE1_ID).getName(), architecture2.getName());
        assertTrue(Files.exists(Paths.get(architecture2.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(architectureFilesPath +
                FileUtil.normalizePath(architecture2.getName() + "/" + UPDATED_LOGO_FILE.getOriginalFilename()))));
    }
}
