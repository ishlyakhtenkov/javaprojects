package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.TestContentFilesManager;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.ProjectTo;
import ru.javaprojects.projector.projects.ProjectUtil;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.references.technologies.TechnologyTestData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.*;
import static ru.javaprojects.projector.common.util.validation.UniqueNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.web.ProjectController.PROJECTS_URL;
import static ru.javaprojects.projector.references.architectures.ArchitectureTestData.architecture1;
import static ru.javaprojects.projector.references.architectures.ArchitectureTestData.architecture2;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ProjectControllerTest extends AbstractControllerTest implements TestContentFilesManager {
    private static final String PROJECTS_VIEW = "projects/projects";
    private static final String PROJECTS_ADD_FORM_URL = PROJECTS_URL + "/add";
    private static final String PROJECT_FORM_VIEW = "projects/project-form";
    static final String PROJECTS_URL_SLASH = PROJECTS_URL + "/";
    private static final String PROJECT_VIEW = "projects/project";
    private static final String PROJECTS_EDIT_FORM_URL = PROJECTS_URL + "/edit/";

    static final String PROJECTS_TEST_DATA_FILES_PATH = "src/test/test-data-files/projects";

    @Value("${content-path.projects}")
    private String contentPath;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectUtil projectUtil;

    @Override
    public Path getContentPath() {
        return Paths.get(contentPath);
    }

    @Override
    public Path getTestDataFilesPath() {
        return Paths.get(PROJECTS_TEST_DATA_FILES_PATH);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(PROJECTS_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((List<Project>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project3, project1, project2), "technologies"));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatch((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE), project1));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void getUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_TO_ATTRIBUTE))
                .andExpect(model().attribute(TECHNOLOGIES_ATTRIBUTE, List.of(TechnologyTestData.technology3,
                        TechnologyTestData.technology1, TechnologyTestData.technology2, TechnologyTestData.technology4)))
                .andExpect(model().attribute(PRIORITIES_ATTRIBUTE, Priority.values()))
                .andExpect(model().attribute(ARCHITECTURES_ATTRIBUTE, List.of(architecture2, architecture1)))
                .andExpect(view().name(PROJECT_FORM_VIEW));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_FORM_URL + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PROJECT_TO_ATTRIBUTE, projectUtil.asTo(project1)))
                .andExpect(model().attribute(TECHNOLOGIES_ATTRIBUTE, List.of(TechnologyTestData.technology3,
                        TechnologyTestData.technology1, TechnologyTestData.technology2, TechnologyTestData.technology4)))
                .andExpect(model().attribute(PRIORITIES_ATTRIBUTE, Priority.values()))
                .andExpect(model().attribute(ARCHITECTURES_ATTRIBUTE, List.of(architecture2, architecture1)))
                .andExpect(view().name(PROJECT_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_FORM_URL + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_FORM_URL + PROJECT1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_FORM_URL + PROJECT1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        ProjectTo newProjectTo = getNewTo();
        Project newProject = getNew(contentPath);
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                        new Object[]{newProjectTo.getName()}, LocaleContextHolder.getLocale())));
        Project created = projectService.getByName(newProjectTo.getName());
        newProject.setId(created.getId());
        created = projectService.getWithTechnologies(created.id());
        PROJECT_MATCHER.assertMatch(created, newProject);
        actions.andExpect(redirectedUrl(PROJECTS_URL_SLASH + created.getId()));
        assertTrue(Files.exists(Paths.get(created.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(created.getDockerComposeFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(created.getCardImageFile().getFileLink())));
    }

    @Test
    void createUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> projectService.getByName(getNewTo().getName()));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getCardImageFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getDockerComposeFile().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> projectService.getByName(getNewTo().getName()));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getCardImageFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getDockerComposeFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PROJECT_TO_ATTRIBUTE, NAME_PARAM, SHORT_DESCRIPTION_PARAM,
                        START_DATE_PARAM, END_DATE_PARAM, DEPLOYMENT_URL_PARAM, BACKEND_SRC_URL_PARAM,
                        FRONTEND_SRC_URL_PARAM, OPEN_API_URL_PARAM))
                .andExpect(view().name(PROJECT_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> projectService.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + "/logo/" +
                LOGO_FILE.getOriginalFilename())));
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + "/docker/" +
                DOCKER_COMPOSE_FILE.getOriginalFilename())));
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + "/card_img/" +
                CARD_IMAGE_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithoutLogoFile() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("project.logo-not-present",
                        null, LocaleContextHolder.getLocale()), IllegalRequestDataException.class));
        assertThrows(NotFoundException.class, () -> projectService.getByName(newParams.get(NAME_PARAM).get(0)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithoutCardImageFile() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("project.card-image-not-present",
                        null, LocaleContextHolder.getLocale()), IllegalRequestDataException.class));
        assertThrows(NotFoundException.class, () -> projectService.getByName(newParams.get(NAME_PARAM).get(0)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithoutDockerComposeFile() throws Exception {
        ProjectTo newProjectTo = getNewTo();
        Project newProject = getNew(contentPath);
        newProject.setDockerComposeFile(null);
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(LOGO_FILE)
                .file(CARD_IMAGE_FILE)
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                        new Object[]{newProjectTo.getName()}, LocaleContextHolder.getLocale())));
        Project created = projectService.getByName(newProjectTo.getName());
        newProject.setId(created.getId());
        created = projectService.getWithTechnologies(created.id());
        PROJECT_MATCHER.assertMatch(created, newProject);
        actions.andExpect(redirectedUrl(PROJECTS_URL_SLASH + created.getId()));
        assertTrue(Files.exists(Paths.get(created.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(created.getCardImageFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(NAME_PARAM, project1.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(PROJECT_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(PROJECT_FORM_VIEW));
        assertNotEquals(getNew(contentPath).getShortDescription(), projectService.getByName(project1.getName()).getShortDescription());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        Project updatedProject = getUpdated(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROJECTS_URL_SLASH + updatedProject.getId()))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                        new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

        PROJECT_MATCHER.assertMatch(projectService.getWithTechnologies(PROJECT1_ID), updatedProject);
        assertTrue(Files.exists(Paths.get(updatedProject.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getCardImageFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getDockerComposeFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getCardImageFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerComposeFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(contentPath + project1.getName().toLowerCase().replace(' ', '_'))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenNameNotUpdated() throws Exception {
        Project updatedProject = getUpdatedWhenOldName(contentPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
        updatedParams.set(NAME_PARAM, project1.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                        new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

        PROJECT_MATCHER.assertMatch(projectService.getWithTechnologies(PROJECT1_ID), updatedProject);
        assertTrue(Files.exists(Paths.get(updatedProject.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getCardImageFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getDockerComposeFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getCardImageFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerComposeFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenFilesNotUpdated() throws Exception {
        Project updatedProject = getUpdatedWhenOldFiles(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROJECTS_URL_SLASH + PROJECT1_ID))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                        new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

        PROJECT_MATCHER.assertMatch(projectService.getWithTechnologies(PROJECT1_ID), updatedProject);
        assertTrue(Files.exists(Paths.get(updatedProject.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getCardImageFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getDockerComposeFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getCardImageFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerComposeFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(contentPath + project1.getName().toLowerCase().replace(' ', '_'))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
        updatedParams.set(ID_PARAM, String.valueOf(NOT_EXISTING_ID));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(contentPath).getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(contentPath).getName());
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getCardImageFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getDockerComposeFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedInvalidParams(contentPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PROJECT_TO_ATTRIBUTE, NAME_PARAM, SHORT_DESCRIPTION_PARAM,
                        START_DATE_PARAM, END_DATE_PARAM, DEPLOYMENT_URL_PARAM, BACKEND_SRC_URL_PARAM,
                        FRONTEND_SRC_URL_PARAM, OPEN_API_URL_PARAM))
                .andExpect(view().name(PROJECT_FORM_VIEW));
        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(contentPath).getName());

        assertTrue(Files.exists(Paths.get(project1.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getCardImageFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getDockerComposeFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getLogoFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getCardImageFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getDockerComposeFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
        updatedParams.set(NAME_PARAM, project2.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(PROJECT_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(PROJECT_FORM_VIEW));
        assertNotEquals(projectService.get(PROJECT1_ID).getName(), project2.getName());
        assertTrue(Files.exists(Paths.get(project2.getLogoFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(project2.getCardImageFile().getFileLink())));
        assertTrue(Files.exists(Paths.get(project2.getDockerComposeFile().getFileLink())));
        assertTrue(Files.notExists(Paths.get(contentPath + project2.getName() + "/logo/" +
                UPDATED_LOGO_FILE.getOriginalFilename().toLowerCase().replace(' ', '_'))));
        assertTrue(Files.notExists(Paths.get(contentPath + project2.getName() + "/card_img/" +
                UPDATED_CARD_IMAGE_FILE.getOriginalFilename().toLowerCase().replace(' ', '_'))));
        assertTrue(Files.notExists(Paths.get(contentPath + project2.getName() + "/docker/" +
                UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename().toLowerCase().replace(' ', '_'))));
    }



}
