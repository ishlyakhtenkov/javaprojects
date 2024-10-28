package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.common.CommonTestData.getPageableParams;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.web.ProjectManagementController.MANAGEMENT_PROJECTS_URL;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ProjectManagementControllerTest extends AbstractControllerTest {
    private static final String MANAGEMENT_PROJECTS_VIEW = "management/projects/projects";

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void showProjectsManagementPage() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(MANAGEMENT_PROJECTS_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(MANAGEMENT_PROJECTS_VIEW));
        Page<ProjectPreviewTo> projects = (Page<ProjectPreviewTo>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROJECTS_ATTRIBUTE);
        assertEquals(3, projects.getTotalElements());
        assertEquals(2, projects.getTotalPages());
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects.getContent(),
                List.of(project3PreviewTo, project1PreviewTo), "author.roles", "author.password", "author.registered",
                "technologies");
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void showProjectsManagementPageSearchByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(MANAGEMENT_PROJECTS_URL)
                .param(KEYWORD_PARAM, project1.getName()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(MANAGEMENT_PROJECTS_VIEW));
        Page<ProjectPreviewTo> projects = (Page<ProjectPreviewTo>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROJECTS_ATTRIBUTE);
        assertEquals(1, projects.getTotalElements());
        assertEquals(1, projects.getTotalPages());
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects.getContent(), List.of(project1PreviewTo),
                 "author.roles", "author.password", "author.registered", "technologies");
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
