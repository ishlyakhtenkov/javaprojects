package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.TestContentFilesManager;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.ProjectUtil;
import ru.javaprojects.projector.projects.model.Comment;
import ru.javaprojects.projector.projects.model.DescriptionElement;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.to.DescriptionElementTo;
import ru.javaprojects.projector.projects.to.ProjectTo;
import ru.javaprojects.projector.reference.technologies.TechnologyTestData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.common.CommonTestData.*;
import static ru.javaprojects.projector.common.validation.UniqueNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.projector.projects.ProjectService.*;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.model.ElementType.IMAGE;
import static ru.javaprojects.projector.projects.web.ProjectController.PROJECTS_URL;
import static ru.javaprojects.projector.reference.architectures.ArchitectureTestData.architecture1;
import static ru.javaprojects.projector.reference.architectures.ArchitectureTestData.architecture2;
import static ru.javaprojects.projector.users.UserTestData.*;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ProjectControllerTest extends AbstractControllerTest implements TestContentFilesManager {
    static final String PROJECTS_URL_SLASH = PROJECTS_URL + "/";
    static final String PROJECTS_DATA_URL = PROJECTS_URL_SLASH + "%d/data";
    static final String PROJECTS_VIEW_URL = PROJECTS_URL_SLASH + "%d/view";
    private static final String PROJECTS_ADD_URL = PROJECTS_URL_SLASH + "add";
    private static final String PROJECTS_EDIT_URL = PROJECTS_URL_SLASH + "edit/";
    private static final String PROJECT_VIEW = "projects/project";
    private static final String PROJECT_DATA_VIEW = "projects/project-data";
    private static final String PROJECT_FORM_VIEW = "projects/project-form";

    @Value("${content-path.projects}")
    private String projectFilesPath;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectUtil projectUtil;

    @Override
    public Path getContentPath() {
        return Paths.get(projectFilesPath);
    }

    @Override
    public Path getTestDataFilesPath() {
        return Paths.get(PROJECTS_TEST_DATA_FILES_PATH);
    }

    @Test
    @SuppressWarnings("unchecked")
    void showProjectPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_VIEW_URL, PROJECT1_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(model().attributeExists(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE))
                .andExpect(model().attributeExists(COMMENTS_ATTRIBUTE))
                .andExpect(model().attributeDoesNotExist(LIKED_ATTRIBUTE))
                .andExpect(model().attributeDoesNotExist(LIKED_COMMENTS_IDS_ATTRIBUTE))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER
                        .assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE), project1, "views", "descriptionElements.project",
                        "author.roles", "author.password", "author.registered", "comments.created",
                                "comments.author.roles", "comments.author.password", "comments.author.registered"))
                .andExpect(result -> assertEquals(project1.getViews() + 1, ((Project) Objects.requireNonNull(result
                                .getModelAndView()).getModel().get(PROJECT_ATTRIBUTE)).getViews()))
                .andExpect(result -> assertTrue((Boolean) Objects.requireNonNull(result.getModelAndView()).getModel()
                        .get(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE)))
                .andExpect(result -> assertEquals(project1CommentIndents, Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)))
                .andExpect(result -> assertEquals(new ArrayList<>(project1CommentIndents.keySet()),
                        new ArrayList<>(((Map<Comment, Integer>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)).keySet())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void showProjectPage() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_VIEW_URL, PROJECT1_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(model().attributeExists(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE))
                .andExpect(model().attributeExists(COMMENTS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_COMMENTS_IDS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_ATTRIBUTE))
                .andExpect(model().attribute(LIKED_ATTRIBUTE, true))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER
                        .assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(PROJECT_ATTRIBUTE), project1, "views", "author.roles",
                                "author.password", "author.registered", "descriptionElements.project", "comments.created",
                         "comments.author.roles", "comments.author.password", "comments.author.registered"))
                .andExpect(result -> assertEquals(project1.getViews() + 1, ((Project) Objects.requireNonNull(result
                                .getModelAndView()).getModel().get(PROJECT_ATTRIBUTE)).getViews()))
                .andExpect(result -> assertTrue((Boolean) Objects.requireNonNull(result.getModelAndView()).getModel()
                        .get(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE)))
                .andExpect(result -> assertEquals(project1CommentIndents, Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)))
                .andExpect(result -> assertEquals(new ArrayList<>(project1CommentIndents.keySet()),
                        new ArrayList<>(((Map<Comment, Integer>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)).keySet())))
                .andExpect(result -> assertEquals(Set.of(PROJECT1_COMMENT1_ID, PROJECT1_COMMENT4_ID),
                        Objects.requireNonNull(result.getModelAndView()).getModel().get(LIKED_COMMENTS_IDS_ATTRIBUTE)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showProjectPageWhenProjectDisabledAndBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_VIEW_URL, PROJECT3_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(model().attributeExists(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE))
                .andExpect(model().attributeExists(COMMENTS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_COMMENTS_IDS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_ATTRIBUTE))
                .andExpect(model().attribute(LIKED_ATTRIBUTE, false))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER
                        .assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECT_ATTRIBUTE), project3, "views", "author.roles",
                                "author.password", "author.registered", "descriptionElements.project", "comments.created",
                                "comments.author.roles", "comments.author.password", "comments.author.registered"))
                .andExpect(result -> assertEquals(project3.getViews() + 1, ((Project) Objects.requireNonNull(result
                        .getModelAndView()).getModel().get(PROJECT_ATTRIBUTE)).getViews()))
                .andExpect(result -> assertFalse((Boolean) Objects.requireNonNull(result.getModelAndView()).getModel()
                        .get(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE)))
                .andExpect(result -> assertEquals(Map.of(), Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)))
                .andExpect(result -> assertEquals(Set.of(),
                        Objects.requireNonNull(result.getModelAndView()).getModel().get(LIKED_COMMENTS_IDS_ATTRIBUTE)));
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void showProjectPageWhenProjectDisabledAndNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_VIEW_URL, PROJECT3_ID)))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("project.forbidden-view-disabled", null, getLocale()),
                        IllegalRequestDataException.class));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showProjectPageWhenProjectDisabledAndNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_VIEW_URL, PROJECT3_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(model().attributeExists(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE))
                .andExpect(model().attributeExists(COMMENTS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_COMMENTS_IDS_ATTRIBUTE))
                .andExpect(model().attributeExists(LIKED_ATTRIBUTE))
                .andExpect(model().attribute(LIKED_ATTRIBUTE, false))
                .andExpect(view().name(PROJECT_VIEW))
                .andExpect(result -> PROJECT_MATCHER
                        .assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECT_ATTRIBUTE), project3, "views", "author.roles",
                                "author.password", "author.registered", "descriptionElements.project", "comments.created",
                                "comments.author.roles", "comments.author.password", "comments.author.registered"))
                .andExpect(result -> assertEquals(project3.getViews() + 1, ((Project) Objects.requireNonNull(result
                        .getModelAndView()).getModel().get(PROJECT_ATTRIBUTE)).getViews()))
                .andExpect(result -> assertFalse((Boolean) Objects.requireNonNull(result.getModelAndView()).getModel()
                        .get(HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE)))
                .andExpect(result -> assertEquals(Map.of(), Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(COMMENTS_ATTRIBUTE)))
                .andExpect(result -> assertEquals(Set.of(),
                        Objects.requireNonNull(result.getModelAndView()).getModel().get(LIKED_COMMENTS_IDS_ATTRIBUTE)));
    }

    @Test
    void showProjectPageNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_VIEW_URL, NOT_EXISTING_ID)))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showProjectDataPage() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_DATA_URL, PROJECT1_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(view().name(PROJECT_DATA_VIEW))
                .andExpect(result -> PROJECT_MATCHER
                        .assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECT_ATTRIBUTE), project1, "author.roles", "author.password",
                                "author.registered", "likes", "descriptionElements.project", "comments"));
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void showProjectDataPageNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_DATA_URL, PROJECT1_ID)))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("project.forbidden-view-data-not-belong", null, getLocale()),
                        IllegalRequestDataException.class));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showProjectDataPageNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_DATA_URL, PROJECT1_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(view().name(PROJECT_DATA_VIEW))
                .andExpect(result -> PROJECT_MATCHER
                        .assertMatchIgnoreFields((Project) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECT_ATTRIBUTE), project1, "author.roles", "author.password",
                                "author.registered", "likes", "descriptionElements.project", "comments"));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showProjectDataPageNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_DATA_URL, NOT_EXISTING_ID)))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void showProjectDataPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROJECTS_DATA_URL, PROJECT1_ID)))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddPage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_ADD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_TO_ATTRIBUTE))
                .andExpect(model().attribute(TECHNOLOGIES_ATTRIBUTE, List.of(TechnologyTestData.technology3,
                        TechnologyTestData.technology1, TechnologyTestData.technology2, TechnologyTestData.technology4)))
                .andExpect(model().attribute(PRIORITIES_ATTRIBUTE, Priority.values()))
                .andExpect(model().attribute(ARCHITECTURES_ATTRIBUTE, List.of(architecture2, architecture1)))
                .andExpect(view().name(PROJECT_FORM_VIEW));
    }

    @Test
    void showAddPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_ADD_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditPage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_URL + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PROJECT_TO_ATTRIBUTE, projectUtil.asTo(project1)))
                .andExpect(model().attribute(TECHNOLOGIES_ATTRIBUTE, List.of(TechnologyTestData.technology3,
                        TechnologyTestData.technology1, TechnologyTestData.technology2, TechnologyTestData.technology4)))
                .andExpect(model().attribute(PRIORITIES_ATTRIBUTE, Priority.values()))
                .andExpect(model().attribute(ARCHITECTURES_ATTRIBUTE, List.of(architecture2, architecture1)))
                .andExpect(view().name(PROJECT_FORM_VIEW));
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void showEditPageNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_URL + PROJECT1_ID))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("project.forbidden-edit-not-belong", null, getLocale()),
                        IllegalRequestDataException.class));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditPageNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_URL + PROJECT1_ID))
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
    void showEditPageNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_URL + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void showEditPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECTS_EDIT_URL + PROJECT1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void create() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            ProjectTo newProjectTo = getNewTo();
            Project newProject = getNew(projectFilesPath);
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(NEW_LOGO_FILE)
                    .file(NEW_DOCKER_COMPOSE_FILE)
                    .file(NEW_CARD_IMAGE_FILE)
                    .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                    .params(getNewParams())
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProjectTo.getName()}, getLocale())));
            Project created = projectService.getByName(newProjectTo.getName());
            newProject.setId(created.getId());
            created = projectService.getWithAllInformation(created.id(), Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "author.roles", "author.password",
                    "author.registered", "descriptionElements.id", "descriptionElements.project");
            actions.andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, created.getId())));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(getNewDe3().getImage().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createWhenLogoAndCardImageAndDockerComposeAreByteArrays() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.add(LOGO_FILE_NAME_PARAM, NEW_LOGO_FILE.getOriginalFilename());
        newParams.add(LOGO_FILE_AS_BYTES_PARAM,  Arrays.toString(NEW_LOGO_FILE.getBytes()));
        newParams.add(CARD_IMAGE_FILE_NAME_PARAM, NEW_CARD_IMAGE_FILE.getOriginalFilename());
        newParams.add(CARD_IMAGE_FILE_AS_BYTES_PARAM,  Arrays.toString(NEW_CARD_IMAGE_FILE.getBytes()));
        newParams.add(DOCKER_COMPOSE_FILE_NAME_PARAM, NEW_DOCKER_COMPOSE_FILE.getOriginalFilename());
        newParams.add(DOCKER_COMPOSE_FILE_AS_BYTES_PARAM,  Arrays.toString(NEW_DOCKER_COMPOSE_FILE.getBytes()));
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            ProjectTo newProjectTo = getNewTo();
            Project newProject = getNew(projectFilesPath);
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                    .params(newParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProjectTo.getName()}, getLocale())));
            Project created = projectService.getByName(newProjectTo.getName());
            newProject.setId(created.getId());
            created = projectService.getWithAllInformation(created.id(), Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "author.roles", "author.password",
                    "author.registered", "descriptionElements.id", "descriptionElements.project");
            actions.andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, created.getId())));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(getNewDe3().getImage().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createWhenDeImageIsBytesArray() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        MultipartFile imageFile = getNewDeTo3().getImage().getInputtedFile();
        newParams.add("descriptionElementTos[2].image.inputtedFileBytes", Arrays.toString(imageFile.getBytes()));
        newParams.add("descriptionElementTos[2].image.fileName", imageFile.getOriginalFilename());

        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project newProject = getNew(projectFilesPath);
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(NEW_LOGO_FILE)
                    .file(NEW_DOCKER_COMPOSE_FILE)
                    .file(NEW_CARD_IMAGE_FILE)
                    .params(newParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProject.getName()}, getLocale())));
            Project created = projectService.getByName(newProject.getName());
            newProject.setId(created.getId());
            created = projectService.getWithAllInformation(created.id(), Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "author.roles", "author.password",
                    "author.registered", "descriptionElements.id", "descriptionElements.project");
            actions.andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, created.getId())));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(getNewDe3().getImage().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createWithSameDeImages() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.add("descriptionElementTos[3].type", IMAGE.name());
        newParams.add("descriptionElementTos[3].index", String.valueOf(4));
        MultipartFile newDeTo3ImageFile = getNewDeTo3().getImage().getInputtedFile();
        Project newProject = getNew(projectFilesPath);
        newProject.addDescriptionElement(new DescriptionElement(null, IMAGE, (byte) 4, null, new File("deImage.png",
                "./content/projects/new_project_name/description/images/" + PREPARED_UUID_STRING + "_deimage.png")));

        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(NEW_LOGO_FILE)
                .file(NEW_DOCKER_COMPOSE_FILE)
                .file(NEW_CARD_IMAGE_FILE)
                .file((MockMultipartFile) newDeTo3ImageFile)
                .file(new MockMultipartFile("descriptionElementTos[3].image.inputtedFile",
                        newDeTo3ImageFile.getOriginalFilename(), MediaType.IMAGE_PNG_VALUE, newDeTo3ImageFile.getBytes()))
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                        new Object[]{newProject.getName()}, getLocale())));
        Project created = projectService.getByName(newProject.getName());
        newProject.setId(created.getId());
        created = projectService.getWithAllInformation(created.id(), Comparator.naturalOrder());
        PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "author.roles", "author.password",
                "author.registered", "descriptionElements.id", "descriptionElements.project",
                "descriptionElements.image.fileLink");
        actions.andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, created.getId())));
        assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
        try (Stream<Path> pathStream = Files.list(Paths.get(projectFilesPath + "user@gmail.com/new_project_name/description/images"))) {
            long fileCounter = pathStream
                    .peek(path ->
                            assertTrue(path.toString()
                                    .endsWith(FileUtil.normalizePath(newDeTo3ImageFile.getOriginalFilename()))))
                    .count();
            assertEquals(2, fileCounter);
        }
    }

    @Test
    void createUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(NEW_LOGO_FILE)
                .file(NEW_DOCKER_COMPOSE_FILE)
                .file(NEW_CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> projectService.getByName(getNewTo().getName()));
        assertTrue(Files.notExists(Paths.get(getNew(projectFilesPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(projectFilesPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(projectFilesPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(NEW_LOGO_FILE)
                .file(NEW_DOCKER_COMPOSE_FILE)
                .file(NEW_CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PROJECT_TO_ATTRIBUTE, NAME_PARAM, SHORT_DESCRIPTION_PARAM,
                        START_DATE_PARAM, END_DATE_PARAM, DEPLOYMENT_URL_PARAM, BACKEND_SRC_URL_PARAM,
                        FRONTEND_SRC_URL_PARAM, OPEN_API_URL_PARAM, "descriptionElementTos[0].text",
                        "descriptionElementTos[1].text"));
        assertArrayEquals(getNewTo().getLogo().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(getNewTo().getLogo().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getLogo().getFileLink());

        assertArrayEquals(getNewTo().getCardImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getInputtedFileBytes());
        assertEquals(getNewTo().getCardImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView())
                        .getModel().get(PROJECT_TO_ATTRIBUTE)).getCardImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROJECT_TO_ATTRIBUTE)).getCardImage().getFileLink());

        assertArrayEquals(getNewTo().getDockerCompose().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getInputtedFileBytes());
        assertEquals(getNewTo().getDockerCompose().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getFileLink());

        assertArrayEquals(getNewDeTo3().getImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getInputtedFileBytes());
        assertEquals(getNewDeTo3().getImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getFileLink());

        assertThrows(NotFoundException.class, () -> projectService.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(projectFilesPath, USER_MAIL, newInvalidParams.get(NAME_PARAM).get(0) + LOGO_DIR +
                NEW_LOGO_FILE.getOriginalFilename())));
        assertTrue(Files.notExists(Paths.get(projectFilesPath, USER_MAIL, newInvalidParams.get(NAME_PARAM).get(0) + DOCKER_DIR +
                NEW_DOCKER_COMPOSE_FILE.getOriginalFilename())));
        assertTrue(Files.notExists(Paths.get(projectFilesPath, USER_MAIL,
                FileUtil.normalizePath(newInvalidParams.get(NAME_PARAM).get(0) + CARD_IMG_DIR +
                        NEW_CARD_IMAGE_FILE.getOriginalFilename()))));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createWithoutLogo() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(NEW_DOCKER_COMPOSE_FILE)
                .file(NEW_CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(newParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("project.logo-not-present", null, getLocale()),
                        IllegalRequestDataException.class));
        assertThrows(NotFoundException.class, () -> projectService.getByName(newParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(getNew(projectFilesPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(projectFilesPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createWithoutCardImage() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(NEW_LOGO_FILE)
                .file(NEW_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(newParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("project.card-image-not-present", null, getLocale()),
                        IllegalRequestDataException.class));
        assertThrows(NotFoundException.class, () -> projectService.getByName(newParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(getNew(projectFilesPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(projectFilesPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createWithoutDockerCompose() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            ProjectTo newProjectTo = getNewTo();
            Project newProject = getNew(projectFilesPath);
            newProject.setDockerCompose(null);
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(NEW_LOGO_FILE)
                    .file(NEW_CARD_IMAGE_FILE)
                    .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                    .params(getNewParams())
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProjectTo.getName()}, getLocale())));
            Project created = projectService.getByName(newProjectTo.getName());
            newProject.setId(created.getId());
            created = projectService.getWithAllInformation(created.id(), Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "author.roles", "author.password",
                    "author.registered", "descriptionElements.id", "descriptionElements.project");
            actions.andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, created.getId())));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(getNewDe3().getImage().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(NAME_PARAM, project1.getName());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(NEW_LOGO_FILE)
                .file(NEW_DOCKER_COMPOSE_FILE)
                .file(NEW_CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(PROJECT_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(PROJECT_FORM_VIEW));
        assertArrayEquals(getNewTo().getLogo().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(getNewTo().getLogo().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getLogo().getFileLink());

        assertArrayEquals(getNewTo().getCardImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getInputtedFileBytes());
        assertEquals(getNewTo().getCardImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getFileLink());

        assertArrayEquals(getNewTo().getDockerCompose().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getInputtedFileBytes());
        assertEquals(getNewTo().getDockerCompose().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getFileLink());

        assertArrayEquals(getNewDeTo3().getImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getInputtedFileBytes());
        assertEquals(getNewDeTo3().getImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getFileLink());

        assertNotEquals(getNew(projectFilesPath).getShortDescription(),
                projectService.getByName(project1.getName()).getShortDescription());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createAnotherUserProjectDuplicateName() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            ProjectTo newProjectTo = getNewTo();
            newProjectTo.setName(project2.getName());
            Project newProject = getNew(projectFilesPath);
            newProject.getLogo().setFileLink(projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" + project2.getName() + LOGO_DIR +
                    NEW_LOGO_FILE.getOriginalFilename()));
            newProject.getCardImage().setFileLink(projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" + project2.getName() +
                    CARD_IMG_DIR + NEW_CARD_IMAGE_FILE.getOriginalFilename()));
            newProject.getDockerCompose().setFileLink(projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" + project2.getName() + DOCKER_DIR +
                    NEW_DOCKER_COMPOSE_FILE.getOriginalFilename()));
            String deFileLink = "./content/projects/user@gmail.com/skill_aggregator/description/images/" +
                    PREPARED_UUID_STRING + "_deimage.png";
            newProject.getDescriptionElements().stream()
                    .filter(de -> de.getType() == IMAGE)
                    .findFirst().orElseThrow().getImage()
                    .setFileLink(deFileLink);
            newProject.setName(project2.getName());
            MultiValueMap<String, String> newParams = getNewParams();
            newParams.set(NAME_PARAM, project2.getName());
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(NEW_LOGO_FILE)
                    .file(NEW_DOCKER_COMPOSE_FILE)
                    .file(NEW_CARD_IMAGE_FILE)
                    .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                    .params(newParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProjectTo.getName()}, getLocale())));
            Project created = projectService.getByAuthorAndName(USER_ID, newProjectTo.getName());
            newProject.setId(created.getId());
            created = projectService.getWithAllInformation(created.id(), Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "author.roles", "author.password",
                    "author.registered", "descriptionElements.id", "descriptionElements.project");
            actions.andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, created.getId())));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(deFileLink)));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void update() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project updatedProject = getUpdated(projectFilesPath);
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(UPDATED_LOGO_FILE)
                    .file(UPDATED_CARD_IMAGE_FILE)
                    .file(UPDATED_DOCKER_COMPOSE_FILE)
                    .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                    .params(getUpdatedParams(projectFilesPath))
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, updatedProject.getId())))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, getLocale())));

            Project project = projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(project, updatedProject, "author.roles", "author.password",
                    "author.registered", "descriptionElements.id", "descriptionElements.project", "comments");
            assertEquals(project1.getLikes(), project.getLikes());
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6.getImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdate.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de6.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
            assertTrue(Files.notExists(
                    Paths.get("./content/projects/updatedprojectname/description/images/restaurant_aggregator_schema.png")));
            assertTrue(Files.notExists(Paths.get(projectFilesPath + FileUtil.normalizePath(project1.getName()))));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWhenLogoAndCardImageAndDockerComposeAreByteArrays() throws Exception {
        Project updatedProject = getUpdated(projectFilesPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(projectFilesPath);
        updatedParams.add(LOGO_FILE_AS_BYTES_PARAM, Arrays.toString(UPDATED_LOGO_FILE.getBytes()));
        updatedParams.add(CARD_IMAGE_FILE_AS_BYTES_PARAM, Arrays.toString(UPDATED_CARD_IMAGE_FILE.getBytes()));
        updatedParams.add(DOCKER_COMPOSE_FILE_AS_BYTES_PARAM, Arrays.toString(UPDATED_DOCKER_COMPOSE_FILE.getBytes()));
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                    .params(updatedParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, updatedProject.getId())))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, getLocale())));
            Project project = projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(project, updatedProject, "author.roles", "author.password",
                    "author.registered", "comments", "descriptionElements.id", "descriptionElements.project");
            assertEquals(project1.getLikes(), project.getLikes());
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6.getImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdate.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de6.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
            assertTrue(Files.notExists(
                    Paths.get("./content/projects/updatedprojectname/description/images/restaurant_aggregator_schema.png")));
            assertTrue(Files.notExists(Paths.get(projectFilesPath + FileUtil.normalizePath(project1.getName()))));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWithoutChangingName() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project updatedProject = getUpdatedWithOldName(projectFilesPath);
            MultiValueMap<String, String> updatedParams = getUpdatedParams(projectFilesPath);
            updatedParams.set(NAME_PARAM, project1.getName());
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(UPDATED_LOGO_FILE)
                    .file(UPDATED_CARD_IMAGE_FILE)
                    .file(UPDATED_DOCKER_COMPOSE_FILE)
                    .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                    .params(updatedParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, updatedProject.getId())))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, getLocale())));
            Project project = projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(project, updatedProject, "author.roles", "author.password",
                    "author.registered", "comments", "descriptionElements.id", "descriptionElements.project");
            assertEquals(project1.getLikes(), project.getLikes());
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6WhenProjectNameNotUpdated.getImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(de6.getImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdateWithoutNameChanging.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWithoutChangingNameAndWithoutDeImages() throws Exception {
        Project updatedProject = getUpdatedWithOldName(projectFilesPath);
        updatedProject.setDescriptionElements( new TreeSet<>(Set.of(updatedDe2, updatedDe1,
                updatedDe4)));
        MultiValueMap<String, String> updatedParams = getUpdatedParams(projectFilesPath);
        updatedParams.set(NAME_PARAM, project1.getName());
        updatedParams.remove("descriptionElementTos[3].id");
        updatedParams.remove("descriptionElementTos[3].type");
        updatedParams.remove("descriptionElementTos[3].index");
        updatedParams.remove("descriptionElementTos[4].id");
        updatedParams.remove("descriptionElementTos[4].type");
        updatedParams.remove("descriptionElementTos[4].index");
        updatedParams.remove("descriptionElementTos[4].text");
        updatedParams.remove("descriptionElementTos[5].type");
        updatedParams.remove("descriptionElementTos[5].index");

        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, updatedProject.getId())))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                        new Object[]{updatedProject.getName()}, getLocale())));
        Project project = projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder());
        PROJECT_MATCHER.assertMatchIgnoreFields(project, updatedProject, "author.roles", "author.password",
                "author.registered", "comments", "descriptionElements.project");
        assertEquals(project1.getLikes(), project.getLikes());
        assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de6.getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWithoutChangingNameAndWithDeImageUpdate() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project updatedProject = getUpdatedWithOldName(projectFilesPath);
            DescriptionElementTo updatedDeToNew = getNewDeToForProjectUpdate();
            updatedDeToNew.setId(de3.getId());
            MultiValueMap<String, String> updatedParams = getUpdatedParams(projectFilesPath);
            updatedParams.set(NAME_PARAM, project1.getName());
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(UPDATED_LOGO_FILE)
                    .file(UPDATED_CARD_IMAGE_FILE)
                    .file(UPDATED_DOCKER_COMPOSE_FILE)
                    .file((MockMultipartFile) updatedDeToNew.getImage().getInputtedFile())
                    .params(updatedParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, updatedProject.getId())))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, getLocale())));
            Project project = projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(project, updatedProject, "author.roles", "author.password",
                    "author.registered", "comments", "descriptionElements.id", "descriptionElements.project");
            assertEquals(project1.getLikes(), project.getLikes());
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6WhenProjectNameNotUpdated.getImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdateWithoutNameChanging.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateWithoutChangingFiles() throws Exception {
        Project updatedProject = getUpdatedWithOldFiles(projectFilesPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(projectFilesPath);
        updatedParams.remove("descriptionElementTos[5].type");
        updatedParams.remove("descriptionElementTos[5].index");
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, updatedProject.getId())))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                        new Object[]{updatedProject.getName()}, getLocale())));

        Project project = projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder());
        PROJECT_MATCHER.assertMatchIgnoreFields(project, updatedProject, "author.roles", "author.password",
                "author.registered", "comments", "descriptionElements.project");
        assertEquals(project1.getLikes(), project.getLikes());
        assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedDe6.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de6.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.notExists(
                Paths.get("./content/projects/updatedprojectname/description/images/restaurant_aggregator_schema.png")));
        assertTrue(Files.notExists(Paths.get(projectFilesPath + FileUtil.normalizePath(project1.getName()))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(projectFilesPath);
        updatedParams.set(ID_PARAM, String.valueOf(NOT_EXISTING_ID));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void updateUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                .params(getUpdatedParams(projectFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(projectFilesPath).getName());
        assertTrue(Files.exists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(newDeForProjectUpdate.getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void updateNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                .params(getUpdatedParams(projectFilesPath))
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("project.forbidden-edit-not-belong", null, getLocale()),
                        IllegalRequestDataException.class));
        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(projectFilesPath).getName());
        assertTrue(Files.exists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(projectFilesPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(projectFilesPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(projectFilesPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(newDeForProjectUpdate.getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotBelongsByAdmin() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project updatedProject = getUpdated(projectFilesPath);
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(UPDATED_LOGO_FILE)
                    .file(UPDATED_CARD_IMAGE_FILE)
                    .file(UPDATED_DOCKER_COMPOSE_FILE)
                    .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                    .params(getUpdatedParams(projectFilesPath))
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, updatedProject.getId())))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, getLocale())));

            Project project = projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(project, updatedProject, "author.roles", "author.password",
                    "author.registered", "descriptionElements.id", "descriptionElements.project", "comments");
            assertEquals(project1.getLikes(), project.getLikes());
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6.getImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdate.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de6.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
            assertTrue(Files.notExists(
                    Paths.get("./content/projects/updatedprojectname/description/images/restaurant_aggregator_schema.png")));
            assertTrue(Files.notExists(Paths.get(projectFilesPath + FileUtil.normalizePath(project1.getName()))));
        }
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedInvalidParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PROJECT_TO_ATTRIBUTE, NAME_PARAM, SHORT_DESCRIPTION_PARAM,
                        START_DATE_PARAM, END_DATE_PARAM, DEPLOYMENT_URL_PARAM, BACKEND_SRC_URL_PARAM,
                        FRONTEND_SRC_URL_PARAM, OPEN_API_URL_PARAM, "descriptionElementTos[0].text",
                        "descriptionElementTos[1].text"))
                .andExpect(view().name(PROJECT_FORM_VIEW));
        assertArrayEquals(UPDATED_LOGO_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(UPDATED_LOGO_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getLogo().getFileLink());

        assertArrayEquals(UPDATED_CARD_IMAGE_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getInputtedFileBytes());
        assertEquals(UPDATED_CARD_IMAGE_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getFileLink());

        assertArrayEquals(UPDATED_DOCKER_COMPOSE_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getInputtedFileBytes());
        assertEquals(UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getFileLink());

        assertArrayEquals(getNewDeTo3().getImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getInputtedFileBytes());
        assertEquals(getNewDeTo3().getImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(2).getImage().getFileLink());

        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(projectFilesPath).getName());
        assertTrue(Files.exists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getCardImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(de6.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(projectFilesPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(projectFilesPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(projectFilesPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(projectFilesPath);
        updatedParams.set(NAME_PARAM, project3.getName());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(PROJECT_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(PROJECT_FORM_VIEW));

        assertArrayEquals(UPDATED_LOGO_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getLogo().getInputtedFileBytes());
        assertEquals(UPDATED_LOGO_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getLogo().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getLogo().getFileLink());

        assertArrayEquals(UPDATED_CARD_IMAGE_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getInputtedFileBytes());
        assertEquals(UPDATED_CARD_IMAGE_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getCardImage().getFileLink());

        assertArrayEquals(UPDATED_DOCKER_COMPOSE_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getInputtedFileBytes());
        assertEquals(UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getDockerCompose().getFileLink());

        assertArrayEquals(getNewDeToForProjectUpdate().getImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(5).getImage().getInputtedFileBytes());
        assertEquals(getNewDeToForProjectUpdate().getImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(5).getImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROJECT_TO_ATTRIBUTE)).getDescriptionElementTos().get(5).getImage().getFileLink());

        assertNotEquals(projectService.get(PROJECT1_ID).getName(), project3.getName());
        assertTrue(Files.exists(Paths.get(project3.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(project3.getCardImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(de3.getImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(de6.getImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(projectFilesPath + FileUtil.normalizePath(project3.getName() + LOGO_DIR +
                UPDATED_LOGO_FILE.getOriginalFilename()))));
        assertTrue(Files.notExists(Paths.get(projectFilesPath + FileUtil.normalizePath(project3.getName() + CARD_IMG_DIR +
                UPDATED_CARD_IMAGE_FILE.getOriginalFilename()))));
        assertTrue(Files.notExists(Paths.get(projectFilesPath + FileUtil.normalizePath(project3.getName() + DOCKER_DIR +
                UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename()))));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getImage().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateAnotherUserProjectDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(projectFilesPath);
        updatedParams.set(NAME_PARAM, project2.getName());
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project updatedProject = getUpdated(projectFilesPath);
            updatedProject.getLogo().setFileLink(projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" + project2.getName() + LOGO_DIR +
                    UPDATED_LOGO_FILE.getOriginalFilename()));
            updatedProject.getCardImage().setFileLink(projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" + project2.getName() +
                    CARD_IMG_DIR + UPDATED_CARD_IMAGE_FILE.getOriginalFilename()));
            updatedProject.getDockerCompose().setFileLink(projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" + project2.getName() + DOCKER_DIR +
                    UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename()));
            updatedProject.getDescriptionElements().stream()
                    .filter(de -> de.getType() == IMAGE)
                    .forEach(de -> de.getImage().setFileLink(de.getImage().getFileLink()
                            .replace("updatedprojectname", "skill_aggregator")));
            updatedProject.setName(project2.getName());
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECTS_URL)
                    .file(UPDATED_LOGO_FILE)
                    .file(UPDATED_CARD_IMAGE_FILE)
                    .file(UPDATED_DOCKER_COMPOSE_FILE)
                    .file((MockMultipartFile) getNewDeToForProjectUpdate().getImage().getInputtedFile())
                    .params(updatedParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(String.format(PROJECTS_DATA_URL, updatedProject.getId())))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, getLocale())));

            Project project = projectService.getWithAllInformation(PROJECT1_ID, Comparator.naturalOrder());
            PROJECT_MATCHER.assertMatchIgnoreFields(project, updatedProject, "author.roles", "author.password",
                    "author.registered", "descriptionElements.id", "descriptionElements.project", "comments");
            assertEquals(project1.getLikes(), project.getLikes());
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6.getImage().getFileLink()
                    .replace("updatedprojectname", "skill_aggregator"))));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdate.getImage().getFileLink()
                    .replace("updatedprojectname", "skill_aggregator"))));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de6.getImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getImage().getFileLink())));
            assertTrue(Files.notExists(
                    Paths.get("./content/projects/user@gmail.com/skill_aggregator/description/images/restaurant_aggregator_schema.png")));
            assertTrue(Files.notExists(Paths.get(projectFilesPath + FileUtil.normalizePath(project1.getName()))));
        }
    }
}
