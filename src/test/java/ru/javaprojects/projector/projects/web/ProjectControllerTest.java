package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.projects.model.Project;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.projector.projects.ProjectTestData.*;

class ProjectControllerTest extends AbstractControllerTest {
    private static final String PROJECT_VIEW = "project";
    private static final String PROJECTS_URL_SLASH = "/projects/";

    @Autowired
    private MessageSource messageSource;

    @Test
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(model().attributeExists("hasFrontendTechnologies"))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE), project1, "likes", "views", "descriptionElements.project"))
                .andExpect(result -> assertEquals(project1.getLikes(), ((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE)).getLikes()))
                .andExpect(result -> assertEquals(project1.getViews() + 1, ((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE)).getViews()))
                .andExpect(result -> assertTrue((Boolean) Objects.requireNonNull(result.getModelAndView()).getModel()
                        .get("hasFrontendTechnologies")));
    }

    @Test
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePage() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name("index"))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((List<Project>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1, project2), "likes", "descriptionElements"));
        List<Project> projects = (List<Project>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROJECTS_ATTRIBUTE);
        assertEquals(project1.getLikes(), projects.get(0).getLikes());
        assertEquals(project2.getLikes(), projects.get(1).getLikes());
    }
}
