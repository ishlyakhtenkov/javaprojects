package ru.javaprojects.projector.reference.architectures.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.TestContentFilesManager;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.reference.architectures.ArchitectureService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.projector.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.projector.reference.architectures.ArchitectureTestData.*;
import static ru.javaprojects.projector.reference.architectures.web.ArchitectureController.ARCHITECTURES_URL;
import static ru.javaprojects.projector.reference.architectures.web.ArchitectureControllerTest.ARCHITECTURES_TEST_DATA_FILES_PATH;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ArchitectureRestControllerTest extends AbstractControllerTest implements TestContentFilesManager {
    private static final String ARCHITECTURES_URL_SLASH = ARCHITECTURES_URL + "/";

    @Value("${content-path.architectures}")
    private String contentPath;

    @Autowired
    private ArchitectureService architectureService;

    @Autowired
    private MessageSource messageSource;

    @Override
    public Path getContentPath() {
        return Paths.get(contentPath);
    }

    @Override
    public Path getTestDataFilesPath() {
        return Paths.get(ARCHITECTURES_TEST_DATA_FILES_PATH);
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
    void deleteWhenReferenced() throws Exception {
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
                .andExpect(problemDetail(messageSource.getMessage("notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(ARCHITECTURES_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
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
