package ru.javaprojects.projector.home.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;

import java.util.List;
import java.util.Objects;

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
                .andExpect(view().name("index"))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1PreviewTo, project2PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name("index"))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1PreviewTo, project2PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }
}
