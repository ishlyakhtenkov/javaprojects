package ru.javaprojects.projector.projects.web;

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
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
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
    void likeProject() throws Exception {
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
    void likeProjectWhenAlreadyLiked() throws Exception {
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
    void dislikeProject() throws Exception {
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
    void dislikeProjectWhenHasNotLike() throws Exception {
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
    void likeProjectWhenProjectNotFound() throws Exception {
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
    void likeProjectUnauthorized() throws Exception {
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

    @Test
    @WithUserDetails(USER_MAIL)
    void likeComment() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + PROJECT1_COMMENT2_ID)
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1Comment2.getLikes().size() + 1,
                commentRepository.findById(PROJECT1_COMMENT2_ID).orElseThrow().getLikes().size());
        assertDoesNotThrow(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_COMMENT2_ID, USER_ID).orElseThrow());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void likeCommentWhenAlreadyLiked() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + PROJECT1_COMMENT1_ID)
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1Comment1.getLikes().size(),
                commentRepository.findById(PROJECT1_COMMENT1_ID).orElseThrow().getLikes().size());
        assertDoesNotThrow(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_COMMENT1_ID, USER_ID).orElseThrow());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void likeYourselfComment() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + PROJECT1_COMMENT3_ID)
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("like.forbidden-like-yourself", null,
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + PROJECT1_COMMENT3_ID));
        assertEquals(project1Comment3.getLikes().size(),
                commentRepository.findById(PROJECT1_COMMENT3_ID).orElseThrow().getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_COMMENT3_ID, USER_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void dislikeComment() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + PROJECT1_COMMENT1_ID)
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1Comment1.getLikes().size() - 1,
                commentRepository.findById(PROJECT1_COMMENT1_ID).orElseThrow().getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_COMMENT1_ID, USER_ID).isEmpty());
    }

    @Test
    @WithUserDetails(DISABLED_USER_MAIL)
    void dislikeCommentWhenHasNotLike() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + PROJECT1_COMMENT4_ID)
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1Comment4.getLikes().size(),
                commentRepository.findById(PROJECT1_COMMENT4_ID).orElseThrow().getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_COMMENT4_ID, DISABLED_USER_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void likeCommentWhenCommentNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + NOT_EXISTING_ID)
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + NOT_EXISTING_ID));
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(NOT_EXISTING_ID, USER_ID).isEmpty());
    }

    @Test
    void likeCommentUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID + "/comments/" + PROJECT1_COMMENT1_ID)
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertEquals(project1Comment1.getLikes().size(),
                commentRepository.findById(PROJECT1_COMMENT1_ID).orElseThrow().getLikes().size());
    }
}
