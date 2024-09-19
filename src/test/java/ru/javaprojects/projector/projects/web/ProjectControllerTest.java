package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.projects.model.Project;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.web.ProjectController.PROJECTS_URL;

class ProjectControllerTest extends AbstractControllerTest {
    private static final String PROJECT_VIEW = "project";
    private static final String PROJECTS_URL_SLASH = PROJECTS_URL + "/";

    @Autowired
    private MessageSource messageSource;

    @Test
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(model().attributeExists("hasFrontendTechnologies"))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatch((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE), project1))
                .andExpect(result -> assertTrue((Boolean) Objects.requireNonNull(result.getModelAndView()).getModel()
                        .get("hasFrontendTechnologies")));
    }

    @Test
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }
}
