package ru.javaprojects.projector.references.web;

import org.junit.jupiter.api.BeforeEach;
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
import ru.javaprojects.projector.references.TechnologyTo;
import ru.javaprojects.projector.references.model.Technology;
import ru.javaprojects.projector.references.repository.TechnologyRepository;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.CommonTestData.ACTION_ATTRIBUTE;
import static ru.javaprojects.projector.CommonTestData.getPageableParams;
import static ru.javaprojects.projector.references.TechnologyTestData.getNew;
import static ru.javaprojects.projector.references.TechnologyTestData.*;
import static ru.javaprojects.projector.references.UniqueTechnologyNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.projector.references.web.TechnologyController.TECHNOLOGIES_URL;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class TechnologyControllerTest extends AbstractControllerTest {
    private static final String TECHNOLOGIES_VIEW = "references/technologies";
    private static final String TECHNOLOGIES_ADD_FORM_URL = TECHNOLOGIES_URL + "/add";
    private static final String TECHNOLOGY_FORM_VIEW = "references/technology-form";

    @Value("${content-path.technologies}")
    private String contentPath;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TechnologyRepository technologyRepository;

    @BeforeEach
    void deleteTestContent() throws IOException {
        Files.walkFileTree(Paths.get(contentPath), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    if (!dir.equals(Paths.get(contentPath))) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
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
                .file(IMAGE_FILE)
                .params((getNewToParams()))
                .with(csrf()))
                .andExpect(redirectedUrl(TECHNOLOGIES_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("technology.created",
                        new Object[]{newTechnologyTo.getName()}, LocaleContextHolder.getLocale())));

        Technology created = technologyRepository.findByNameIgnoreCase(newTechnologyTo.getName()).orElseThrow();
        newTechnology.setId(created.getId());
        TECHNOLOGY_MATCHER.assertMatch(created, newTechnology);
        assertTrue(Files.exists(Paths.get(contentPath, newTechnology.getName(),
                newTechnology.getImageFile().getFileName())));
    }

    @Test
    void createUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(IMAGE_FILE)
                .params((getNewToParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(() -> technologyRepository.findByNameIgnoreCase(getNewTo().getName()).isEmpty());
        assertTrue(Files.notExists(Paths.get(contentPath, getNew(contentPath).getImageFile().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(IMAGE_FILE)
                .params((getNewToParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(() -> technologyRepository.findByNameIgnoreCase(getNewTo().getName()).isEmpty());
        assertTrue(Files.notExists(Paths.get(contentPath, getNew(contentPath).getImageFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewToInvalidParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(IMAGE_FILE)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, URL_PARAM))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertTrue(() -> technologyRepository.findByNameIgnoreCase(newInvalidParams.get(NAME_PARAM).get(0)).isEmpty());
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + "/" +
                IMAGE_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewToParams();
        newParams.set(NAME_PARAM, technology1.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, TECHNOLOGIES_URL)
                .file(IMAGE_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(TECHNOLOGY_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(TECHNOLOGY_FORM_VIEW));
        assertNotEquals(getNew(contentPath).getUrl(),
                technologyRepository.findByNameIgnoreCase(technology1.getName()).orElseThrow().getUrl());
    }
}
