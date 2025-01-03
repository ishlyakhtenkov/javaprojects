package ru.javaprojects.javaprojects.projects.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.javaprojects.javaprojects.AbstractControllerTest;
import ru.javaprojects.javaprojects.ContentFilesManager;
import ru.javaprojects.javaprojects.common.error.IllegalRequestDataException;
import ru.javaprojects.javaprojects.common.error.NotFoundException;
import ru.javaprojects.javaprojects.common.util.JsonUtil;
import ru.javaprojects.javaprojects.projects.ProjectService;
import ru.javaprojects.javaprojects.projects.model.Comment;
import ru.javaprojects.javaprojects.projects.repository.CommentRepository;
import ru.javaprojects.javaprojects.projects.repository.LikeRepository;
import ru.javaprojects.javaprojects.projects.to.CommentTo;
import ru.javaprojects.javaprojects.projects.to.ProjectPreviewTo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.javaprojects.common.CommonTestData.*;
import static ru.javaprojects.javaprojects.common.util.JsonUtil.writeValue;
import static ru.javaprojects.javaprojects.projects.ProjectTestData.*;
import static ru.javaprojects.javaprojects.projects.web.ProjectControllerTest.PROJECTS_URL_SLASH;
import static ru.javaprojects.javaprojects.users.UserTestData.*;
import static ru.javaprojects.javaprojects.users.web.LoginController.LOGIN_URL;

class ProjectRestControllerTest extends AbstractControllerTest implements ContentFilesManager {
    private static final String PROJECTS_LIKE_PROJECT_URL = PROJECTS_URL_SLASH + "%d/like";
    private static final String PROJECTS_COMMENTS_URL = PROJECTS_URL_SLASH + "%d/comments";
    private static final String PROJECTS_COMMENTS_URL_SLASH_ID = PROJECTS_URL_SLASH + "%d/comments/%d";
    private static final String PROJECTS_LIKE_COMMENT_URL = PROJECTS_COMMENTS_URL_SLASH_ID + "/like";
    private static final String PROJECTS_BY_AUTHOR_URL = PROJECTS_URL_SLASH + "/by-author";
    private static final String PROJECTS_FRESH_URL = PROJECTS_URL_SLASH + "/fresh";
    private static final String PROJECTS_POPULAR_URL = PROJECTS_URL_SLASH + "/popular";
    private static final String PROJECTS_BY_TAG_URL = PROJECTS_URL_SLASH + "/by-tag";
    private static final String PROJECTS_BY_KEYWORD_URL = PROJECTS_URL_SLASH + "/by-keyword";

    @Value("${content-path.projects}")
    private String projectFilesPath;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Path getContentPath() {
        return Paths.get(projectFilesPath);
    }

    @Override
    public Path getContentFilesPath() {
        return Paths.get(PROJECTS_TEST_CONTENT_FILES_PATH);
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void likeProject() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_PROJECT_URL, PROJECT2_ID))
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project2.getLikes().size() + 1,
                projectService.getWithAllInformation(PROJECT2_ID, Comparator.naturalOrder()).getLikes().size());
        assertDoesNotThrow(() -> likeRepository.findByObjectIdAndUserId(PROJECT2_ID, USER2_ID).orElseThrow());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void likeProjectWhenAlreadyLiked() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_PROJECT_URL, PROJECT1_ID))
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1.getLikes().size(),
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getLikes().size());
        assertDoesNotThrow(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_ID, ADMIN_ID).orElseThrow());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void dislikeProject() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_PROJECT_URL, PROJECT1_ID))
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1.getLikes().size() - 1,
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_ID, ADMIN_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void dislikeProjectWhenHasNotLike() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_PROJECT_URL, PROJECT2_ID))
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project2.getLikes().size(),
                projectService.getWithAllInformation(PROJECT2_ID, Comparator.naturalOrder()).getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT2_ID, USER2_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void likeYourselfProject() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_PROJECT_URL, PROJECT3_ID))
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("project.forbidden-like-yourself", null, getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_LIKE_PROJECT_URL, PROJECT3_ID)));
        assertEquals(project3.getLikes().size(),
                projectService.getWithAllInformation(PROJECT3_ID, Comparator.naturalOrder()).getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT3_ID, USER_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void likeProjectWhenProjectNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_PROJECT_URL, NOT_EXISTING_ID))
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_LIKE_PROJECT_URL, NOT_EXISTING_ID)));
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(NOT_EXISTING_ID, USER2_ID).isEmpty());
    }

    @Test
    void likeProjectUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_PROJECT_URL, PROJECT2_ID))
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
        ResultActions action = perform(MockMvcRequestBuilders.post(String.format(PROJECTS_COMMENTS_URL, PROJECT1_ID))
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
    void createCommentWhenProjectNotFound() throws Exception {
        CommentTo newCommentTo = getNewCommentTo();
        newCommentTo.setProjectId(NOT_EXISTING_ID);
        perform(MockMvcRequestBuilders.post(String.format(PROJECTS_COMMENTS_URL, NOT_EXISTING_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newCommentTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_COMMENTS_URL, NOT_EXISTING_ID)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createCommentWhenParentCommentNotFound() throws Exception {
        CommentTo newCommentTo = getNewCommentTo();
        newCommentTo.setParentId(NOT_EXISTING_ID);
        perform(MockMvcRequestBuilders.post(String.format(PROJECTS_COMMENTS_URL, PROJECT1_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newCommentTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_COMMENTS_URL, PROJECT1_ID)));
        assertEquals(project1.getComments().size(),
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getComments().size());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createCommentInvalid() throws Exception {
        CommentTo newCommentTo = getNewCommentTo();
        newCommentTo.setText(HTML_TEXT);
        perform(MockMvcRequestBuilders.post(String.format(PROJECTS_COMMENTS_URL, PROJECT1_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newCommentTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(jsonPath("$.invalid_params.text")
                        .value(messageSource.getMessage("validation.comment.text.NoHtml", null, getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_COMMENTS_URL, PROJECT1_ID)));
        assertEquals(project1.getComments().size(),
                projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder()).getComments().size());
    }

    @Test
    void createCommentUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(String.format(PROJECTS_COMMENTS_URL, PROJECT1_ID))
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
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, PROJECT1_COMMENT2_ID))
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
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, PROJECT1_COMMENT1_ID))
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
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, PROJECT1_COMMENT3_ID))
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("comment.forbidden-like-yourself", null, getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, PROJECT1_COMMENT3_ID)));
        assertEquals(project1Comment3.getLikes().size(),
                commentRepository.findById(PROJECT1_COMMENT3_ID).orElseThrow().getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_COMMENT3_ID, USER_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void dislikeComment() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, PROJECT1_COMMENT1_ID))
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
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, PROJECT1_COMMENT4_ID))
                .param(LIKED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertEquals(project1Comment4.getLikes().size(),
                commentRepository.findById(PROJECT1_COMMENT4_ID).orElseThrow().getLikes().size());
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(PROJECT1_COMMENT4_ID, DISABLED_USER_ID).isEmpty());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void likeCommentNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, NOT_EXISTING_ID))
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, NOT_EXISTING_ID)));
        assertTrue(() -> likeRepository.findByObjectIdAndUserId(NOT_EXISTING_ID, USER_ID).isEmpty());
    }

    @Test
    void likeCommentUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(PROJECTS_LIKE_COMMENT_URL, PROJECT1_ID, PROJECT1_COMMENT1_ID))
                .param(LIKED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertEquals(project1Comment1.getLikes().size(),
                commentRepository.findById(PROJECT1_COMMENT1_ID).orElseThrow().getLikes().size());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteComment() throws Exception {
        perform(MockMvcRequestBuilders.delete(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT3_ID))
                .with(csrf()))
                .andExpect(status().isNoContent());
        Comment deleted = commentRepository.findById(PROJECT1_COMMENT3_ID).orElseThrow();
        COMMENT_MATCHER.assertMatchIgnoreFields(deleted, project1Comment3, "created", "updated", "author.password",
                "author.registered", "author.roles", "deleted");
        assertTrue(deleted.isDeleted());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteCommentNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.delete(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT1_ID))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("comment.forbidden-delete-not-belong", null, getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT1_ID)));
        Comment comment = commentRepository.findById(PROJECT1_COMMENT1_ID).orElseThrow();
        COMMENT_MATCHER.assertMatch(comment, project1Comment1);
        assertFalse(comment.isDeleted());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteCommentNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.delete(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT3_ID))
                .with(csrf()))
                .andExpect(status().isNoContent());
        Comment deleted = commentRepository.findById(PROJECT1_COMMENT3_ID).orElseThrow();
        COMMENT_MATCHER.assertMatchIgnoreFields(deleted, project1Comment3, "created", "updated", "author.password",
                "author.registered", "author.roles", "deleted");
        assertTrue(deleted.isDeleted());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteCommentNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID, NOT_EXISTING_ID))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID, NOT_EXISTING_ID)));
    }

    @Test
    void deleteCommentUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT3_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        Comment comment = commentRepository.findById(PROJECT1_COMMENT3_ID).orElseThrow();
        COMMENT_MATCHER.assertMatch(comment, project1Comment3);
        assertFalse(comment.isDeleted());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateComment() throws Exception {
        perform(MockMvcRequestBuilders.put(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT3_ID))
                .param(TEXT_PARAM, UPDATED_COMMENT_TEXT)
                .with(csrf()))
                .andExpect(status().isNoContent());
        Comment updated = commentRepository.findById(PROJECT1_COMMENT3_ID).orElseThrow();
        COMMENT_MATCHER.assertMatchIgnoreFields(updated, project1Comment3, "created", "updated", "author.password",
                "author.registered", "author.roles", "text");
        assertEquals(UPDATED_COMMENT_TEXT, updated.getText());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateCommentNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.put(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT1_ID))
                .param(TEXT_PARAM, UPDATED_COMMENT_TEXT)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("comment.forbidden-edit-not-belong", null, getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT1_ID)));
        Comment updated = commentRepository.findById(PROJECT1_COMMENT1_ID).orElseThrow();
        COMMENT_MATCHER.assertMatch(updated, project1Comment1);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateCommentNotFound() throws Exception {
        perform(MockMvcRequestBuilders.put(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID, NOT_EXISTING_ID))
                .param(TEXT_PARAM, UPDATED_COMMENT_TEXT)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID, NOT_EXISTING_ID)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateCommentInvalid() throws Exception {
        perform(MockMvcRequestBuilders.put(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT3_ID))
                .param(TEXT_PARAM, HTML_TEXT)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("updateComment.text: Comment must not be HTML"))
                .andExpect(jsonPath("$.invalid_params")
                        .value(messageSource.getMessage("validation.comment.text.NoHtml", null, getLocale())))
                .andExpect(problemInstance(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT3_ID)));
        COMMENT_MATCHER.assertMatch(commentRepository.findById(PROJECT1_COMMENT3_ID).orElseThrow(), project1Comment3);
    }

    @Test
    void updateCommentUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.put(String.format(PROJECTS_COMMENTS_URL_SLASH_ID, PROJECT1_ID,
                        PROJECT1_COMMENT1_ID))
                .param(TEXT_PARAM, UPDATED_COMMENT_TEXT)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        COMMENT_MATCHER.assertMatch(commentRepository.findById(PROJECT1_COMMENT1_ID).orElseThrow(), project1Comment1);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteProject() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + PROJECT1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> projectService.get(PROJECT1_ID));
        assertTrue(likeRepository.findAllByObjectId(PROJECT1_ID).isEmpty());
        assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getPreview().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de6.getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void deleteProjectNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + PROJECT1_ID)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("project.forbidden-delete-not-belong", null,
                        getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + PROJECT1_ID));
        assertDoesNotThrow(() -> projectService.get(PROJECT1_ID));
        assertFalse(likeRepository.findAllByObjectId(PROJECT1_ID).isEmpty());
        assertTrue(Files.exists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getPreview().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(de6.getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteProjectNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + PROJECT1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> projectService.get(PROJECT1_ID));
        assertTrue(likeRepository.findAllByObjectId(PROJECT1_ID).isEmpty());
        assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getPreview().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de6.getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteProjectNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + NOT_EXISTING_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    void deleteProjectUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(PROJECTS_URL_SLASH + PROJECT1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> projectService.get(PROJECT1_ID));
        assertFalse(likeRepository.findAllByObjectId(PROJECT1_ID).isEmpty());
        assertTrue(Files.exists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getPreview().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(de6.getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void hideProject() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(VISIBLE_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertFalse(projectService.get(PROJECT1_ID).isVisible());

        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(VISIBLE_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(projectService.get(PROJECT1_ID).isVisible());
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void hideProjectNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(VISIBLE_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("project.forbidden-hide-not-belong", null,
                        getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + PROJECT1_ID));
        assertTrue(projectService.get(PROJECT1_ID).isVisible());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void hideProjectNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(VISIBLE_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertFalse(projectService.get(PROJECT1_ID).isVisible());

        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(VISIBLE_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(projectService.get(PROJECT1_ID).isVisible());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void hideProjectNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + NOT_EXISTING_ID)
                .param(VISIBLE_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        LocaleContextHolder.getLocale())))
                .andExpect(problemInstance(PROJECTS_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    void hideProjectUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROJECTS_URL_SLASH + PROJECT1_ID)
                .param(VISIBLE_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(projectService.get(PROJECT1_ID).isVisible());
    }

    @Test
    void getAllProjectsByAuthorUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_BY_AUTHOR_URL)
                .param(USER_ID_PARAM, String.valueOf(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROJECT_PREVIEW_TO_MATCHER.contentJsonIgnoreFields(List.of(project1PreviewTo), "author.roles",
                        "author.password", "author.registered"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllProjectsByAuthorWhenAuthor() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_BY_AUTHOR_URL)
                .param(USER_ID_PARAM, String.valueOf(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROJECT_PREVIEW_TO_MATCHER.contentJsonIgnoreFields(List.of(project1PreviewTo, project3PreviewTo),
                        "author.roles", "author.password", "author.registered"));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void getAllProjectsByAuthorWhenNotAuthor() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_BY_AUTHOR_URL)
                .param(USER_ID_PARAM, String.valueOf(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(PROJECT_PREVIEW_TO_MATCHER.contentJsonIgnoreFields(List.of(project1PreviewTo), "author.roles",
                        "author.password", "author.registered"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getFreshProjects() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROJECTS_FRESH_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProjectPreviewTo> projects =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProjectPreviewTo.class);
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects, List.of(project2PreviewTo, project1PreviewTo),
                "author.roles", "author.password", "author.registered");
    }

    @Test
    void getFreshProjectsUnauthorized() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROJECTS_FRESH_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProjectPreviewTo> projects =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProjectPreviewTo.class);
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects, List.of(project2PreviewTo, project1PreviewTo),
                "author.roles", "author.password", "author.registered");
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getPopularProjects() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROJECTS_POPULAR_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProjectPreviewTo> projects =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProjectPreviewTo.class);
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects, List.of(project1PreviewTo, project2PreviewTo),
                "author.roles", "author.password", "author.registered");
    }

    @Test
    void getPopularProjectsUnauthorized() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROJECTS_POPULAR_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProjectPreviewTo> projects =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProjectPreviewTo.class);
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects, List.of(project1PreviewTo, project2PreviewTo),
                "author.roles", "author.password", "author.registered");
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getProjectsByTag() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROJECTS_BY_TAG_URL)
                .param(TAG_PARAM, tag1.getName())
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProjectPreviewTo> projects =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProjectPreviewTo.class);
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects, List.of(project2PreviewTo, project1PreviewTo),
                "author.roles", "author.password", "author.registered");
    }

    @Test
    void getProjectsByTagUnauthorized() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROJECTS_BY_TAG_URL)
                .param(TAG_PARAM, tag1.getName())
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProjectPreviewTo> projects =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProjectPreviewTo.class);
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects, List.of(project2PreviewTo, project1PreviewTo),
                "author.roles", "author.password", "author.registered");
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getProjectsByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROJECTS_BY_KEYWORD_URL)
                .param(KEYWORD_PARAM, AGGREGATOR_KEYWORD)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProjectPreviewTo> projects =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProjectPreviewTo.class);
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects, List.of(project1PreviewTo, project2PreviewTo),
                "author.roles", "author.password", "author.registered");
    }

    @Test
    void getProjectsByKeywordUnauthorized() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROJECTS_BY_KEYWORD_URL)
                .param(KEYWORD_PARAM, AGGREGATOR_KEYWORD)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProjectPreviewTo> projects =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProjectPreviewTo.class);
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects, List.of(project1PreviewTo, project2PreviewTo),
                "author.roles", "author.password", "author.registered");
    }
}
