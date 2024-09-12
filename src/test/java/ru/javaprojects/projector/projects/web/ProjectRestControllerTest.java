package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.TestContentFilesManager;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.projects.ProjectService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.projector.CommonTestData.ENABLED_PARAM;
import static ru.javaprojects.projector.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.web.ProjectControllerTest.PROJECTS_TEST_DATA_FILES_PATH;
import static ru.javaprojects.projector.projects.web.ProjectControllerTest.PROJECTS_URL_SLASH;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ProjectRestControllerTest extends AbstractControllerTest implements TestContentFilesManager {

    @Value("${content-path.projects}")
    private String contentPath;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MessageSource messageSource;

    @Override
    public Path getContentPath() {
        return Paths.get(contentPath);
    }

    @Override
    public Path getTestDataFilesPath() {
        return Paths.get(PROJECTS_TEST_DATA_FILES_PATH);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + PROJECT1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> projectService.get(PROJECT1_ID));
        assertTrue(Files.notExists(Paths.get(project1.getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getCardImageFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerComposeFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de3.getFileLink())));
        assertTrue(Files.notExists(Paths.get(de6.getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + NOT_EXISTING_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + PROJECT1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> projectService.get(PROJECT1_ID));
        assertTrue(Files.exists(Paths.get(project1.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getCardImageFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getDockerComposeFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(de3.getFileLink())));
        assertTrue(Files.exists(Paths.get(de6.getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + PROJECT1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> projectService.get(PROJECT1_ID));
        assertTrue(Files.exists(Paths.get(project1.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getCardImageFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getDockerComposeFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(de3.getFileLink())));
        assertTrue(Files.exists(Paths.get(de6.getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enable() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertFalse(projectService.get(PROJECT1_ID).isEnabled());

        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(ENABLED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(projectService.get(PROJECT1_ID).isEnabled());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enableNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + NOT_EXISTING_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    void enableUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(projectService.get(PROJECT1_ID).isEnabled());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void enableForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(projectService.get(PROJECT1_ID).isEnabled());
    }
}
