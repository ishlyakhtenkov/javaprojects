package ru.javaprojects.projector.projects.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.model.Comment;
import ru.javaprojects.projector.projects.repository.CommentRepository;
import ru.javaprojects.projector.projects.repository.LikeRepository;
import ru.javaprojects.projector.projects.to.CommentTo;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.projector.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.projector.common.util.JsonUtil.writeValue;
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

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @WithUserDetails(USER2_MAIL)
    void like() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT2_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project2.getLikes().size() + 1,
                projectService.getWithAllInformation(PROJECT2_ID, Comparator.naturalOrder()).getLikes().size());
        assertDoesNotThrow(() -> likeRepository.findByObjectIdAndUserId(PROJECT2_ID, USER2_ID).orElseThrow());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void likeWhenAlreadyLiked() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1.getLikes().size(),
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getLikes().size());
        assertDoesNotThrow(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_ID, USER_ID).orElseThrow());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void dislike() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1.getLikes().size() - 1,
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_ID, USER_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void dislikeWhenHasNotLike() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT2_ID + "/like")
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project2.getLikes().size(),
                projectService.getWithAllInformation(PROJECT2_ID, Comparator.naturalOrder()).getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT2_ID, USER2_ID).isEmpty());
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
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(NOT_EXISTING_ID, USER2_ID).isEmpty());
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
                projectService.getWithAllInformation(PROJECT2_ID, Comparator.naturalOrder()).getLikes().size());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createComment() throws Exception {
        Comment newComment = getNewComment();
        ResultActions action = perform(MockMvcRequestBuilders.post(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewCommentTo()))
                .with(csrf()))
                .andExpect(status().isCreated());
        Comment created = COMMENT_MATCHER.readFromJson(action);
        newComment.setId(created.getId());
        COMMENT_MATCHER.assertMatch(created, newComment);
        COMMENT_MATCHER.assertMatch(commentRepository.findById(created.id()).orElseThrow(), newComment);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createCommentProjectNotFound() throws Exception {
        CommentTo newCommentTo = getNewCommentTo();
        newCommentTo.setProjectId(NOT_EXISTING_ID);
        perform(MockMvcRequestBuilders.post(PROJECTS_URL_SLASH + NOT_EXISTING_ID + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newCommentTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + NOT_EXISTING_ID + "/comments"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createCommentParentNotFound() throws Exception {
        CommentTo newCommentTo = getNewCommentTo();
        newCommentTo.setParentId(NOT_EXISTING_ID);
        perform(MockMvcRequestBuilders.post(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newCommentTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments"));
        assertEquals(project1.getComments().size(),
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getComments().size());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createCommentInvalid() throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        CommentTo newCommentTo = getNewCommentTo();
        newCommentTo.setText("<p>dsfsdfdsfdsf</p>");
        ResultActions actions = perform(MockMvcRequestBuilders.post(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newCommentTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(jsonPath("$.invalid_params.text").value("Should not be html"))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments"));
        assertEquals(project1.getComments().size(),
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getComments().size());
    }

    @Test
    void createCommentUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewCommentTo()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertEquals(project1.getComments().size(),
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getComments().size());
    }
}
