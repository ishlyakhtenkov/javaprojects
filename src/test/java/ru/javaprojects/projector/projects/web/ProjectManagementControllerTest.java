package ru.javaprojects.projector.projects.web;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import ru.javaprojects.projector.common.model.Priority;
import ru.javaprojects.projector.projects.ProjectService;
import ru.javaprojects.projector.projects.ProjectUtil;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.projector.CommonTestData.*;
import static ru.javaprojects.projector.common.util.validation.UniqueNameValidator.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.projector.projects.ProjectService.*;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.projects.model.ElementType.IMAGE;
import static ru.javaprojects.projector.projects.web.ProjectManagementController.PROJECT_MANAGEMENT_URL;
import static ru.javaprojects.projector.reference.architectures.ArchitectureTestData.architecture1;
import static ru.javaprojects.projector.reference.architectures.ArchitectureTestData.architecture2;
import static ru.javaprojects.projector.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.projector.users.UserTestData.USER_MAIL;
import static ru.javaprojects.projector.users.web.LoginController.LOGIN_URL;

class ProjectManagementControllerTest extends AbstractControllerTest implements TestContentFilesManager {
    private static final String PROJECTS_MANAGEMENT_VIEW = "management/projects/projects";
    private static final String PROJECTS_ADD_FORM_URL = PROJECT_MANAGEMENT_URL + "/add";
    private static final String PROJECT_FORM_VIEW = "management/projects/project-form";
    static final String PROJECT_MANAGEMENT_URL_SLASH = PROJECT_MANAGEMENT_URL + "/";
    private static final String PROJECT_MANAGEMENT_VIEW = "management/projects/project";
    private static final String PROJECTS_EDIT_FORM_URL = PROJECT_MANAGEMENT_URL + "/edit/";

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
        perform(MockMvcRequestBuilders.get(PROJECT_MANAGEMENT_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(PROJECTS_MANAGEMENT_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatchIgnoreFields((List<Project>) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project3, project1, project2), "technologies", "descriptionElements"));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECT_MANAGEMENT_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECT_MANAGEMENT_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECT_MANAGEMENT_URL_SLASH + PROJECT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECT_ATTRIBUTE))
                .andExpect(view().name(PROJECT_MANAGEMENT_VIEW))
                .andExpect(result -> PROJECT_MATCHER.assertMatch((Project) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(PROJECT_ATTRIBUTE), project1));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECT_MANAGEMENT_URL_SLASH + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void getUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROJECT_MANAGEMENT_URL_SLASH + PROJECT1_ID))
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
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            ProjectTo newProjectTo = getNewTo();
            Project newProject = getNew(contentPath);
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                    .file(LOGO_FILE)
                    .file(DOCKER_COMPOSE_FILE)
                    .file(CARD_IMAGE_FILE)
                    .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                    .params(getNewParams())
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProjectTo.getName()}, LocaleContextHolder.getLocale())));
            Project created = projectService.getByName(newProjectTo.getName());
            newProject.setId(created.getId());
            created = projectService.getWithTechnologiesAndDescription(created.id(), true);
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "descriptionElements.id",
                    "descriptionElements.project");
            actions.andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + created.getId()));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(getNewDe3().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWhenLogoAndCardImageAndDockerComposeAreByteArrays() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.add(LOGO_FILE_NAME_PARAM, LOGO_FILE.getOriginalFilename());
        newParams.add(LOGO_FILE_AS_BYTES_PARAM,  Arrays.toString(LOGO_FILE.getBytes()));
        newParams.add(CARD_IMAGE_FILE_NAME_PARAM, CARD_IMAGE_FILE.getOriginalFilename());
        newParams.add(CARD_IMAGE_FILE_AS_BYTES_PARAM,  Arrays.toString(CARD_IMAGE_FILE.getBytes()));
        newParams.add(DOCKER_COMPOSE_FILE_NAME_PARAM, DOCKER_COMPOSE_FILE.getOriginalFilename());
        newParams.add(DOCKER_COMPOSE_FILE_AS_BYTES_PARAM,  Arrays.toString(DOCKER_COMPOSE_FILE.getBytes()));
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            ProjectTo newProjectTo = getNewTo();
            Project newProject = getNew(contentPath);
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                    .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                    .params(newParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProjectTo.getName()}, LocaleContextHolder.getLocale())));
            Project created = projectService.getByName(newProjectTo.getName());
            newProject.setId(created.getId());
            created = projectService.getWithTechnologiesAndDescription(created.id(), true);
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "descriptionElements.id",
                    "descriptionElements.project");
            actions.andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + created.getId()));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(getNewDe3().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWhenDescriptionElementImageIsBytesArray() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        MultipartFile imageFile = getNewDeTo3().getImage().getInputtedFile();
        newParams.add("descriptionElementTos[2].image.inputtedFileBytes", Arrays.toString(imageFile.getBytes()));
        newParams.add("descriptionElementTos[2].image.fileName", imageFile.getOriginalFilename());

        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project newProject = getNew(contentPath);
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                    .file(LOGO_FILE)
                    .file(DOCKER_COMPOSE_FILE)
                    .file(CARD_IMAGE_FILE)
                    .params(newParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProject.getName()}, LocaleContextHolder.getLocale())));
            Project created = projectService.getByName(newProject.getName());
            newProject.setId(created.getId());
            created = projectService.getWithTechnologiesAndDescription(created.id(), true);
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "descriptionElements.id",
                    "descriptionElements.project");
            actions.andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + created.getId()));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(getNewDe3().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithIdenticalDescriptionElementImages() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.add("descriptionElementTos[3].type", IMAGE.name());
        newParams.add("descriptionElementTos[3].index", String.valueOf(4));
        MultipartFile newDeTo3ImageFile = getNewDeTo3().getImage().getInputtedFile();
        Project newProject = getNew(contentPath);
        newProject.addDescriptionElement(new DescriptionElement(null, IMAGE, (byte) 4, null, "deImage.png",
                "./content/projects/new_project_name/description/images/" + PREPARED_UUID_STRING + "_deimage.png"));

        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .file((MockMultipartFile) newDeTo3ImageFile)
                .file(new MockMultipartFile("descriptionElementTos[3].image.inputtedFile", newDeTo3ImageFile.getOriginalFilename(),
                        MediaType.IMAGE_PNG_VALUE, newDeTo3ImageFile.getBytes()))
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                        new Object[]{newProject.getName()}, LocaleContextHolder.getLocale())));
        Project created = projectService.getByName(newProject.getName());
        newProject.setId(created.getId());
        created = projectService.getWithTechnologiesAndDescription(created.id(), true);
        PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "descriptionElements.id", "descriptionElements.project",
                "descriptionElements.fileLink");
        actions.andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + created.getId()));
        assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(created.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
        try (Stream<Path> pathStream = Files.list(Paths.get(contentPath + "new_project_name/description/images"))) {
            long fileCounter = pathStream
                    .peek(path ->
                            assertTrue(path.toString().endsWith(newDeTo3ImageFile.getOriginalFilename().toLowerCase())))
                    .count();
            assertEquals(2, fileCounter);
        }
    }

    @Test
    void createUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> projectService.getByName(getNewTo().getName()));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> projectService.getByName(getNewTo().getName()));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PROJECT_TO_ATTRIBUTE, NAME_PARAM, SHORT_DESCRIPTION_PARAM,
                        START_DATE_PARAM, END_DATE_PARAM, DEPLOYMENT_URL_PARAM, BACKEND_SRC_URL_PARAM,
                        FRONTEND_SRC_URL_PARAM, OPEN_API_URL_PARAM, "descriptionElementTos[0].text",
                        "descriptionElementTos[1].text"));

        assertArrayEquals(getNewTo().getLogo().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getLogo().getInputtedFileBytes());
        assertEquals(getNewTo().getLogo().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getLogo().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getLogo().getFileLink());

        assertArrayEquals(getNewTo().getCardImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getCardImage().getInputtedFileBytes());
        assertEquals(getNewTo().getCardImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getCardImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getCardImage().getFileLink());

        assertArrayEquals(getNewTo().getDockerCompose().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDockerCompose().getInputtedFileBytes());
        assertEquals(getNewTo().getDockerCompose().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDockerCompose().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getDockerCompose().getFileLink());

        assertArrayEquals(getNewDeTo3().getImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDescriptionElementTos().get(2).getImage().getInputtedFileBytes());
        assertEquals(getNewDeTo3().getImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDescriptionElementTos().get(2).getImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getDescriptionElementTos().get(2).getImage().getFileLink());

        assertThrows(NotFoundException.class, () -> projectService.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + LOGO_DIR +
                LOGO_FILE.getOriginalFilename())));
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + DOCKER_DIR +
                DOCKER_COMPOSE_FILE.getOriginalFilename())));
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + CARD_IMG_DIR +
                CARD_IMAGE_FILE.getOriginalFilename())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithoutLogoFile() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(newParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("project.logo-not-present",
                        null, LocaleContextHolder.getLocale()), IllegalRequestDataException.class));
        assertThrows(NotFoundException.class, () -> projectService.getByName(newParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithoutCardImageFile() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(newParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("project.card-image-not-present",
                        null, LocaleContextHolder.getLocale()), IllegalRequestDataException.class));
        assertThrows(NotFoundException.class, () -> projectService.getByName(newParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNew(contentPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getFileLink())));

    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createWithoutDockerComposeFile() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            ProjectTo newProjectTo = getNewTo();
            Project newProject = getNew(contentPath);
            newProject.setDockerCompose(null);
            ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                    .file(LOGO_FILE)
                    .file(CARD_IMAGE_FILE)
                    .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                    .params(getNewParams())
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.created",
                            new Object[]{newProjectTo.getName()}, LocaleContextHolder.getLocale())));
            Project created = projectService.getByName(newProjectTo.getName());
            newProject.setId(created.getId());
            created = projectService.getWithTechnologiesAndDescription(created.id(), true);
            PROJECT_MATCHER.assertMatchIgnoreFields(created, newProject, "descriptionElements.id", "descriptionElements.project");
            actions.andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + created.getId()));
            assertTrue(Files.exists(Paths.get(created.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(created.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(getNewDe3().getFileLink())));
        }
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(NAME_PARAM, project1.getName());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(LOGO_FILE)
                .file(DOCKER_COMPOSE_FILE)
                .file(CARD_IMAGE_FILE)
                .file((MockMultipartFile) getNewDeTo3().getImage().getInputtedFile())
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(PROJECT_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(PROJECT_FORM_VIEW));

        assertArrayEquals(getNewTo().getLogo().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getLogo().getInputtedFileBytes());
        assertEquals(getNewTo().getLogo().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getLogo().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getLogo().getFileLink());

        assertArrayEquals(getNewTo().getCardImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getCardImage().getInputtedFileBytes());
        assertEquals(getNewTo().getCardImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getCardImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getCardImage().getFileLink());

        assertArrayEquals(getNewTo().getDockerCompose().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDockerCompose().getInputtedFileBytes());
        assertEquals(getNewTo().getDockerCompose().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDockerCompose().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getDockerCompose().getFileLink());

        assertArrayEquals(getNewDeTo3().getImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDescriptionElementTos().get(2).getImage().getInputtedFileBytes());
        assertEquals(getNewDeTo3().getImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDescriptionElementTos().get(2).getImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getDescriptionElementTos().get(2).getImage().getFileLink());

        assertNotEquals(getNew(contentPath).getShortDescription(), projectService.getByName(project1.getName()).getShortDescription());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project updatedProject = getUpdated(contentPath);
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                    .file(UPDATED_LOGO_FILE)
                    .file(UPDATED_CARD_IMAGE_FILE)
                    .file(UPDATED_DOCKER_COMPOSE_FILE)
                    .file((MockMultipartFile) getNewDeForProjectUpdate().getImage().getInputtedFile())
                    .params(getUpdatedParams(contentPath))
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + updatedProject.getId()))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

            PROJECT_MATCHER.assertMatchIgnoreFields(projectService.getWithTechnologiesAndDescription(PROJECT1_ID, true),
                    updatedProject, "descriptionElements.id", "descriptionElements.project");
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6.getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdate.getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de6.getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getFileLink())));
            assertTrue(Files.notExists(
                    Paths.get("./content/projects/updatedprojectname/description/images/restaurant_aggregator_schema.png")));
            assertTrue(Files.notExists(Paths.get(contentPath + project1.getName().toLowerCase().replace(' ', '_'))));
        }
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenLogoAndCardImageAndDockerComposeAreByteArrays() throws Exception {
        Project updatedProject = getUpdated(contentPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
        updatedParams.add(LOGO_FILE_AS_BYTES_PARAM, Arrays.toString(UPDATED_LOGO_FILE.getBytes()));
        updatedParams.add(CARD_IMAGE_FILE_AS_BYTES_PARAM, Arrays.toString(UPDATED_CARD_IMAGE_FILE.getBytes()));
        updatedParams.add(DOCKER_COMPOSE_FILE_AS_BYTES_PARAM, Arrays.toString(UPDATED_DOCKER_COMPOSE_FILE.getBytes()));
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                    .file((MockMultipartFile) getNewDeForProjectUpdate().getImage().getInputtedFile())
                    .params(updatedParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + updatedProject.getId()))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

            PROJECT_MATCHER.assertMatchIgnoreFields(projectService.getWithTechnologiesAndDescription(PROJECT1_ID, true),
                    updatedProject, "descriptionElements.id", "descriptionElements.project");
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6.getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdate.getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de6.getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getFileLink())));
            assertTrue(Files.notExists(
                    Paths.get("./content/projects/updatedprojectname/description/images/restaurant_aggregator_schema.png")));
            assertTrue(Files.notExists(Paths.get(contentPath + project1.getName().toLowerCase().replace(' ', '_'))));
        }
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenNameNotUpdated() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);

            Project updatedProject = getUpdatedWhenOldName(contentPath);
            MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
            updatedParams.set(NAME_PARAM, project1.getName());
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                    .file(UPDATED_LOGO_FILE)
                    .file(UPDATED_CARD_IMAGE_FILE)
                    .file(UPDATED_DOCKER_COMPOSE_FILE)
                    .file((MockMultipartFile) getNewDeForProjectUpdate().getImage().getInputtedFile())
                    .params(updatedParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + PROJECT1_ID))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

            PROJECT_MATCHER.assertMatchIgnoreFields(projectService.getWithTechnologiesAndDescription(PROJECT1_ID, true),
                    updatedProject, "descriptionElements.id", "descriptionElements.project");
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6WhenProjectHasOldName.getFileLink())));
            assertTrue(Files.exists(Paths.get(de6.getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdateWithOldName.getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getFileLink())));
        }
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenNameNotUpdatedAndWithoutDescriptionElementImages() throws Exception {
        Project updatedProject = getUpdatedWhenOldName(contentPath);
        updatedProject.setDescriptionElements( new TreeSet<>(Set.of(updatedDe2, updatedDe1,
                updatedDe4)));
        MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
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

        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + PROJECT1_ID))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                        new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

        PROJECT_MATCHER.assertMatchIgnoreFields(projectService.getWithTechnologiesAndDescription(PROJECT1_ID, true),
                updatedProject, "descriptionElements.project");
        assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de3.getFileLink())));
        assertTrue(Files.notExists(Paths.get(de6.getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenNameNotUpdatedAndDescriptionElementImageUpdated() throws Exception {
        UUID preparedUuid = UUID.fromString(PREPARED_UUID_STRING);
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            uuid.when(UUID::randomUUID).thenReturn(preparedUuid);
            Project updatedProject = getUpdatedWhenOldName(contentPath);
            DescriptionElementTo updatedDeToNew = getNewDeForProjectUpdate();
            updatedDeToNew.setId(de3.getId());
            MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
            updatedParams.set(NAME_PARAM, project1.getName());
            perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                    .file(UPDATED_LOGO_FILE)
                    .file(UPDATED_CARD_IMAGE_FILE)
                    .file(UPDATED_DOCKER_COMPOSE_FILE)
                    .file((MockMultipartFile) updatedDeToNew.getImage().getInputtedFile())
                    .params(updatedParams)
                    .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + PROJECT1_ID))
                    .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                            new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

            PROJECT_MATCHER.assertMatchIgnoreFields(projectService.getWithTechnologiesAndDescription(PROJECT1_ID, true),
                    updatedProject, "descriptionElements.id", "descriptionElements.project");
            assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
            assertTrue(Files.exists(Paths.get(updatedDe6WhenProjectHasOldName.getFileLink())));
            assertTrue(Files.exists(Paths.get(newDeForProjectUpdateWithOldName.getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
            assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
            assertTrue(Files.notExists(Paths.get(de3.getFileLink())));
        }
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateWhenFilesNotUpdated() throws Exception {
        Project updatedProject = getUpdatedWhenOldFiles(contentPath);
        MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
        updatedParams.remove("descriptionElementTos[5].type");
        updatedParams.remove("descriptionElementTos[5].index");
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(PROJECT_MANAGEMENT_URL_SLASH + PROJECT1_ID))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("project.updated",
                        new Object[]{updatedProject.getName()}, LocaleContextHolder.getLocale())));

        PROJECT_MATCHER.assertMatchIgnoreFields(projectService.getWithTechnologiesAndDescription(PROJECT1_ID, true),
                updatedProject, "descriptionElements.project");

        assertTrue(Files.exists(Paths.get(updatedProject.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getCardImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedProject.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(updatedDe6.getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(de6.getFileLink())));
        assertTrue(Files.notExists(Paths.get(de3.getFileLink())));
        assertTrue(Files.notExists(
                Paths.get("./content/projects/updatedprojectname/description/images/restaurant_aggregator_schema.png")));
        assertTrue(Files.notExists(Paths.get(contentPath + project1.getName().toLowerCase().replace(' ', '_'))));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
        updatedParams.set(ID_PARAM, String.valueOf(NOT_EXISTING_ID));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeForProjectUpdate().getImage().getInputtedFile())
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, LocaleContextHolder.getLocale()), NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeForProjectUpdate().getImage().getInputtedFile())
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(contentPath).getName());
        assertTrue(Files.exists(Paths.get(de3.getFileLink())));
        assertTrue(Files.notExists(Paths.get(newDeForProjectUpdate.getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeForProjectUpdate().getImage().getInputtedFile())
                .params(getUpdatedParams(contentPath))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(contentPath).getName());
        assertTrue(Files.exists(Paths.get(de3.getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(newDeForProjectUpdate.getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedInvalidParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
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
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getLogo().getInputtedFileBytes());
        assertEquals(UPDATED_LOGO_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getLogo().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getLogo().getFileLink());

        assertArrayEquals(UPDATED_CARD_IMAGE_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getCardImage().getInputtedFileBytes());
        assertEquals(UPDATED_CARD_IMAGE_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getCardImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getCardImage().getFileLink());

        assertArrayEquals(UPDATED_DOCKER_COMPOSE_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDockerCompose().getInputtedFileBytes());
        assertEquals(UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDockerCompose().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getDockerCompose().getFileLink());

        assertArrayEquals(getNewDeTo3().getImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDescriptionElementTos().get(2).getImage().getInputtedFileBytes());
        assertEquals(getNewDeTo3().getImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDescriptionElementTos().get(2).getImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getDescriptionElementTos().get(2).getImage().getFileLink());

        assertNotEquals(projectService.get(PROJECT1_ID).getName(), getUpdated(contentPath).getName());

        assertTrue(Files.exists(Paths.get(project1.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getCardImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(project1.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(de3.getFileLink())));
        assertTrue(Files.exists(Paths.get(de6.getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getLogo().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getCardImage().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUpdated(contentPath).getDockerCompose().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams(contentPath);
        updatedParams.set(NAME_PARAM, project2.getName());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROJECT_MANAGEMENT_URL)
                .file(UPDATED_LOGO_FILE)
                .file(UPDATED_CARD_IMAGE_FILE)
                .file(UPDATED_DOCKER_COMPOSE_FILE)
                .file((MockMultipartFile) getNewDeForProjectUpdate().getImage().getInputtedFile())
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(PROJECT_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(PROJECT_FORM_VIEW));

        assertArrayEquals(UPDATED_LOGO_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getLogo().getInputtedFileBytes());
        assertEquals(UPDATED_LOGO_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getLogo().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getLogo().getFileLink());

        assertArrayEquals(UPDATED_CARD_IMAGE_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getCardImage().getInputtedFileBytes());
        assertEquals(UPDATED_CARD_IMAGE_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getCardImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getCardImage().getFileLink());

        assertArrayEquals(UPDATED_DOCKER_COMPOSE_FILE.getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDockerCompose().getInputtedFileBytes());
        assertEquals(UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDockerCompose().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getDockerCompose().getFileLink());

        assertArrayEquals(getNewDeForProjectUpdate().getImage().getInputtedFile().getBytes(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDescriptionElementTos().get(5).getImage().getInputtedFileBytes());
        assertEquals(getNewDeForProjectUpdate().getImage().getInputtedFile().getOriginalFilename(),
                ((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                        .getDescriptionElementTos().get(5).getImage().getFileName());
        assertNull(((ProjectTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(PROJECT_TO_ATTRIBUTE))
                .getDescriptionElementTos().get(5).getImage().getFileLink());

        assertNotEquals(projectService.get(PROJECT1_ID).getName(), project2.getName());
        assertTrue(Files.exists(Paths.get(project2.getLogo().getFileLink())));
        assertTrue(Files.exists(Paths.get(project2.getCardImage().getFileLink())));
        assertTrue(Files.exists(Paths.get(project2.getDockerCompose().getFileLink())));
        assertTrue(Files.exists(Paths.get(de3.getFileLink())));
        assertTrue(Files.exists(Paths.get(de6.getFileLink())));
        assertTrue(Files.notExists(Paths.get(contentPath + project2.getName() + LOGO_DIR +
                UPDATED_LOGO_FILE.getOriginalFilename().toLowerCase().replace(' ', '_'))));
        assertTrue(Files.notExists(Paths.get(contentPath + project2.getName() + CARD_IMG_DIR +
                UPDATED_CARD_IMAGE_FILE.getOriginalFilename().toLowerCase().replace(' ', '_'))));
        assertTrue(Files.notExists(Paths.get(contentPath + project2.getName() + DOCKER_DIR +
                UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename().toLowerCase().replace(' ', '_'))));
        assertTrue(Files.notExists(Paths.get(getNewDe3().getFileLink())));
    }
}
