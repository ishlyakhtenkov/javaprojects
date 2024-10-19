package ru.javaprojects.projector.home.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.projects.model.Project;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;

class HomeControllerTest extends AbstractControllerTest {

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePage() throws Exception {
        perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_PROJECTS_IDS_ATTRIBUTE))
                .andExpect(model().attributeExists(COMMENTS_TOTAL_ATTRIBUTE))
                .andExpect(view().name("index"))
                .andExpect(result -> PROJECT_MATCHER
                        .assertMatchIgnoreFields((List<Project>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1, project2), "author.roles",
                                "author.password", "author.registered", "descriptionElements", "comments"))
                .andExpect(result -> assertEquals(Set.of(PROJECT1_ID, PROJECT2_ID),
                        Objects.requireNonNull(result.getModelAndView()).getModel().get(LIKED_PROJECTS_IDS_ATTRIBUTE)))
                .andExpect(result -> assertEquals(Map.of(PROJECT1_ID, 6L, PROJECT2_ID, 1L),
                        Objects.requireNonNull(result.getModelAndView()).getModel().get(COMMENTS_TOTAL_ATTRIBUTE)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(model().attributeDoesNotExist(LIKED_PROJECTS_IDS_ATTRIBUTE))
                .andExpect(model().attributeExists(COMMENTS_TOTAL_ATTRIBUTE))
                .andExpect(view().name("index"))
                .andExpect(result -> PROJECT_MATCHER
                        .assertMatchIgnoreFields((List<Project>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1, project2), "author.roles",
                                "author.password", "author.registered", "descriptionElements", "comments"))
                .andExpect(result -> assertEquals(Map.of(PROJECT1_ID, 6L, PROJECT2_ID, 1L),
                        Objects.requireNonNull(result.getModelAndView()).getModel().get(COMMENTS_TOTAL_ATTRIBUTE)));
    }
}
