package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.repository.LikeRepository;

import java.util.Comparator;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.projector.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.web.ProjectControllerTest.PROJECTS_URL_SLASH;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ProjectRestControllerTest extends AbstractControllerTest {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private LikeRepository likeRepository;

    @Test
    @WithUserDetails(USER2_MAIL)
    void like() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT2_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project2.getLikes().size() + 1,
                projectService.getWithTechnologiesAndDescription(PROJECT2_ID, Comparator.naturalOrder()).getLikes().size());
        assertDoesNotThrow(() -> likeRepository.findByProjectIdAndUserId(PROJECT2_ID, USER2_ID).orElseThrow());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void likeWhenAlreadyLiked() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1.getLikes().size(),
                projectService.getWithTechnologiesAndDescription(PROJECT1_ID, Comparator.naturalOrder()).getLikes().size());
        assertDoesNotThrow(() -> likeRepository.findByProjectIdAndUserId(PROJECT1_ID, USER_ID).orElseThrow());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void dislike() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1.getLikes().size() - 1,
                projectService.getWithTechnologiesAndDescription(PROJECT1_ID, Comparator.naturalOrder()).getLikes().size());
        assertTrue(() -> likeRepository.findByProjectIdAndUserId(PROJECT1_ID, USER_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void dislikeWhenHasNotLike() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT2_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project2.getLikes().size(),
                projectService.getWithTechnologiesAndDescription(PROJECT2_ID, Comparator.naturalOrder()).getLikes().size());
        assertTrue(() -> likeRepository.findByProjectIdAndUserId(PROJECT2_ID, USER2_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void likeProjectNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + NOT_EXISTING_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + NOT_EXISTING_ID + "/like"));
        assertTrue(() -> likeRepository.findByProjectIdAndUserId(NOT_EXISTING_ID, USER2_ID).isEmpty());
    }

    @Test
    void likeUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT2_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertEquals(project2.getLikes().size(),
                projectService.getWithTechnologiesAndDescription(PROJECT2_ID, Comparator.naturalOrder()).getLikes().size());
    }
}
