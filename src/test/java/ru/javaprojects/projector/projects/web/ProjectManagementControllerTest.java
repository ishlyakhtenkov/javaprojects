package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.web.ProjectManagementController.MANAGEMENT_PROJECTS_URL;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ProjectManagementControllerTest extends AbstractControllerTest {
    private static final String MANAGEMENT_PROJECTS_VIEW = "management/projects/projects";

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void showProjectsManagementPage() throws Exception {
        perform(MockMvcRequestBuilders.get(MANAGEMENT_PROJECTS_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(MANAGEMENT_PROJECTS_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project3PreviewTo, project1PreviewTo, project2PreviewTo),
                                "author.roles", "author.password", "author.registered", "technologies"));
    }

    @Test
    void showProjectsManagementPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(MANAGEMENT_PROJECTS_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showProjectsManagementPageForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(MANAGEMENT_PROJECTS_URL))
                .andExpect(status().isForbidden());
    }
}
