package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.ProjectUtil;
import ru.javaprojects.projector.projects.model.Project;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.*;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.web.ProjectController.PROJECTS_URL;
import static ru.javaprojects.projector.references.architectures.ArchitectureTestData.architecture1;
import static ru.javaprojects.projector.references.architectures.ArchitectureTestData.architecture2;
import static ru.javaprojects.projector.references.technologies.TechnologyTestData.*;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ProjectControllerTest extends AbstractControllerTest {
    private static final String PROJECTS_VIEW = "projects/projects";
    private static final String PROJECTS_ADD_FORM_URL = PROJECTS_URL + "/add";
    private static final String PROJECT_FORM_VIEW = "projects/project-form";
    static final String PROJECTS_URL_SLASH = PROJECTS_URL + "/";
    private static final String PROJECT_VIEW = "projects/project";
    private static final String PROJECTS_EDIT_FORM_URL = PROJECTS_URL + "/edit/";

    static final String PROJECTS_TEST_DATA_FILES_PATH = "src/test/test-data-files/projects";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ProjectService projectService;

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(PROJECTS_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((List<Project>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project3, project1, project2), "technologies"));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatch((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE), project1));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void getUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_TO_ATTRIBUTE))
                .andExpect(model().attribute(TECHNOLOGIES_ATTRIBUTE, List.of(technology3, technology1, technology2, technology4)))
                .andExpect(model().attribute(PRIORITIES_ATTRIBUTE, Priority.values()))
                .andExpect(model().attribute(ARCHITECTURES_ATTRIBUTE, List.of(architecture2, architecture1)))
                .andExpect(view().name(PROJECT_FORM_VIEW));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_FORM_URL + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PROJECT_TO_ATTRIBUTE, ProjectUtil.asTo(project1)))
                .andExpect(model().attribute(TECHNOLOGIES_ATTRIBUTE, List.of(technology3, technology1, technology2, technology4)))
                .andExpect(model().attribute(PRIORITIES_ATTRIBUTE, Priority.values()))
                .andExpect(model().attribute(ARCHITECTURES_ATTRIBUTE, List.of(architecture2, architecture1)))
                .andExpect(view().name(PROJECT_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_FORM_URL + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_FORM_URL + PROJECT1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_FORM_URL + PROJECT1_ID))
                .andExpect(status().isForbidden());
    }

}
