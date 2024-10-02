package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.projects.model.Comment;
import ru.javaprojects.projector.projects.model.Project;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;

class ProjectControllerTest extends AbstractControllerTest {
    private static final String PROJECT_VIEW = "project";
    static final String PROJECTS_URL_SLASH = "/projects/";

    @Autowired
    private MessageSource messageSource;

    @Test
    @SuppressWarnings("unchecked")
    void getUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(model().attributeExists(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE))
                .andExpect(model().attributeExists(COMMENTS_ATTRIBUTE))
                .andExpect(model().attributeDoesNotExist(LIKED_COMMENTS_IDS_ATTRIBUTE))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE), project1, "views", "descriptionElements.project",
                        "comments.created", "comments.author.roles", "comments.author.password", "comments.author.registered"))
                .andExpect(result -> assertEquals(project1.getViews() + 1, ((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE)).getViews()))
                .andExpect(result -> assertTrue((Boolean) Objects.requireNonNull(result.getModelAndView()).getModel()
                        .get(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE)))
                .andExpect(result -> assertEquals(project1CommentIndents, Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)))
                .andExpect(result -> assertEquals(new ArrayList<>(project1CommentIndents.keySet()), new ArrayList<>(((Map<Comment, Integer>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)).keySet())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(model().attributeExists(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE))
                .andExpect(model().attributeExists(COMMENTS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_COMMENTS_IDS_ATTRIBUTE))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(PROJECT_ATTRIBUTE), project1, "views", "descriptionElements.project",
                        "comments.created", "comments.author.roles", "comments.author.password", "comments.author.registered"))
                .andExpect(result -> assertEquals(project1.getViews() + 1, ((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE)).getViews()))
                .andExpect(result -> assertTrue((Boolean) Objects.requireNonNull(result.getModelAndView()).getModel()
                        .get(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE)))
                .andExpect(result -> assertEquals(project1CommentIndents, Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)))
                .andExpect(result -> assertEquals(new ArrayList<>(project1CommentIndents.keySet()), new ArrayList<>(((Map<Comment, Integer>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)).keySet())))
                .andExpect(result -> assertEquals(Set.of(PROJECT1_COMMENT1_ID, PROJECT1_COMMENT4_ID), Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(LIKED_COMMENTS_IDS_ATTRIBUTE)));
    }

    @Test
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePage() throws Exception {
        perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_PROJECTS_IDS_ATTRIBUTE))
                .andExpect(view().name("index"))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((List<Project>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1, project2), "descriptionElements",
                        "comments"))
                .andExpect(result -> assertEquals(Set.of(PROJECT1_ID, PROJECT2_ID), Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(LIKED_PROJECTS_IDS_ATTRIBUTE)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(model().attributeDoesNotExist(LIKED_PROJECTS_IDS_ATTRIBUTE))
                .andExpect(view().name("index"))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((List<Project>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1, project2), "descriptionElements",
                        "comments"));
    }
}
