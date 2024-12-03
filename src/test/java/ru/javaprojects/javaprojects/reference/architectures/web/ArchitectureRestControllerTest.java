package ru.javaprojects.javaprojects.reference.architectures.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.javaprojects.AbstractControllerTest;
import ru.javaprojects.javaprojects.ContentFilesManager;
import ru.javaprojects.javaprojects.common.error.NotFoundException;
import ru.javaprojects.javaprojects.reference.architectures.ArchitectureService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.javaprojects.common.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.javaprojects.reference.architectures.ArchitectureTestData.*;
import static ru.javaprojects.javaprojects.reference.architectures.web.ArchitectureController.ARCHITECTURES_URL;
import static ru.javaprojects.javaprojects.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.javaprojects.users.UserTestData.USER_MAIL;
import static ru.javaprojects.javaprojects.users.web.LoginController.LOGIN_URL;

class ArchitectureRestControllerTest extends AbstractControllerTest implements ContentFilesManager {
    private static final String ARCHITECTURES_URL_SLASH = ARCHITECTURES_URL + "/";

    @Value("${content-path.architectures}")
    private String architectureFilesPath;

    @Autowired
    private ArchitectureService architectureService;

    @Override
    public Path getContentPath() {
        return Paths.get(architectureFilesPath);
    }

    @Override
    public Path getContentFilesPath() {
        return Paths.get(ARCHITECTURES_TEST_CONTENT_FILES_PATH);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(ARCHITECTURES_URL_SLASH + ARCHITECTURE2_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> architectureService.get(ARCHITECTURE2_ID));
        assertTrue(Files.notExists(Paths.get(architecture2.getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteWhenArchitectureIsReferenced() throws Exception {
        perform(MockMvcRequestBuilders.delete(ARCHITECTURES_URL_SLASH + ARCHITECTURE1_ID)
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(messageSource.getMessage("architecture.is-referenced", null,
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(ARCHITECTURES_URL_SLASH + ARCHITECTURE1_ID));
        assertDoesNotThrow(() -> architectureService.get(ARCHITECTURE1_ID));
        assertTrue(Files.exists(Paths.get(architecture1.getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(ARCHITECTURES_URL_SLASH + NOT_EXISTING_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(ARCHITECTURES_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    void deleteUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(ARCHITECTURES_URL_SLASH + ARCHITECTURE2_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> architectureService.get(ARCHITECTURE2_ID));
        assertTrue(Files.exists(Paths.get(architecture2.getLogo().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(ARCHITECTURES_URL_SLASH + ARCHITECTURE2_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> architectureService.get(ARCHITECTURE2_ID));
        assertTrue(Files.exists(Paths.get(architecture2.getLogo().getFileLink())));
    }
}
