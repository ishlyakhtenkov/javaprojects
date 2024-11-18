package ru.javaprojects.projector.projects;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.projects.model.*;
import ru.javaprojects.projector.projects.to.CommentTo;
import ru.javaprojects.projector.projects.to.DescriptionElementTo;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;
import ru.javaprojects.projector.projects.to.ProjectTo;

import java.time.LocalDate;
import java.util.*;

import static java.time.Month.*;
import static ru.javaprojects.projector.common.CommonTestData.*;
import static ru.javaprojects.projector.common.model.Priority.*;
import static ru.javaprojects.projector.projects.ProjectService.*;
import static ru.javaprojects.projector.projects.model.ElementType.*;
import static ru.javaprojects.projector.projects.model.ObjectType.COMMENT;
import static ru.javaprojects.projector.projects.model.ObjectType.PROJECT;
import static ru.javaprojects.projector.reference.architectures.ArchitectureTestData.*;
import static ru.javaprojects.projector.reference.technologies.TechnologyTestData.*;
import static ru.javaprojects.projector.users.UserTestData.*;

public class ProjectTestData {
    public static final MatcherFactory.Matcher<Project> PROJECT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Project.class);

    public static final MatcherFactory.Matcher<ProjectPreviewTo> PROJECT_PREVIEW_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(ProjectPreviewTo.class);

    public static final MatcherFactory.Matcher<Comment> COMMENT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Comment.class, "created", "updated", "author.password",
                    "author.registered", "author.roles");

    public static final MatcherFactory.Matcher<Tag> TAG_MATCHER = MatcherFactory.usingIgnoringFieldsComparator(Tag.class);

    public static final String PROJECTS_TEST_CONTENT_FILES_PATH = "src/test/test-content-files/projects";

    public static final String PROJECT_ATTRIBUTE = "project";
    public static final String PROJECTS_ATTRIBUTE = "projects";
    public static final String PROJECTS_PAGE_ATTRIBUTE = "projectsPage";
    public static final String TAGS_PAGE_ATTRIBUTE = "tagsPage";
    public static final String PROJECT_TO_ATTRIBUTE = "projectTo";
    public static final String HAS_FRONTEND_TECHNOLOGIES_ATTRIBUTE = "hasFrontendTechnologies";
    public static final String LIKED_ATTRIBUTE = "liked";
    public static final String LIKED_COMMENTS_IDS_ATTRIBUTE = "likedCommentsIds";
    public static final String COMMENTS_ATTRIBUTE = "comments";

    public static final String ANNOTATION_PARAM = "annotation";
    public static final String VISIBLE_PARAM = "visible";
    public static final String STARTED_PARAM = "started";
    public static final String FINISHED_PARAM = "finished";
    public static final String ARCHITECTURE_PARAM = "architecture";
    public static final String DEPLOYMENT_URL_PARAM = "deploymentUrl";
    public static final String BACKEND_SRC_URL_PARAM = "backendSrcUrl";
    public static final String FRONTEND_SRC_URL_PARAM = "frontendSrcUrl";
    public static final String OPEN_API_URL_PARAM = "openApiUrl";
    public static final String TECHNOLOGIES_IDS_PARAM = "technologiesIds";
    public static final String PREVIEW_FILE_NAME_PARAM = "preview.fileName";
    public static final String PREVIEW_FILE_LINK_PARAM = "preview.fileLink";
    public static final String PREVIEW_INPUTTED_FILE_BYTES_PARAM = "preview.inputtedFileBytes";
    public static final String DOCKER_COMPOSE_FILE_NAME_PARAM = "dockerCompose.fileName";
    public static final String DOCKER_COMPOSE_FILE_LINK_PARAM = "dockerCompose.fileLink";
    public static final String DOCKER_COMPOSE_INPUTTED_FILE_BYTES_PARAM = "dockerCompose.inputtedFileBytes";
    public static final String LIKED_PARAM = "liked";
    public static final String TEXT_PARAM = "text";
    public static final String USER_ID_PARAM = "userId";
    public static final String TAGS_PARAM = "tags";
    public static final String TAG_PARAM = "tag";

    public static final long PROJECT1_ID = 100017;
    public static final long PROJECT2_ID = 100018;
    public static final long PROJECT3_ID = 100019;

    public static final long DESCRIPTION_ELEMENT1_ID = 100020;
    public static final long DESCRIPTION_ELEMENT2_ID = 100021;
    public static final long DESCRIPTION_ELEMENT3_ID = 100022;
    public static final long DESCRIPTION_ELEMENT4_ID = 100023;
    public static final long DESCRIPTION_ELEMENT5_ID = 100024;
    public static final long DESCRIPTION_ELEMENT6_ID = 100025;

    public static final long PROJECT1_LIKE1_ID = 100026;
    public static final long PROJECT1_LIKE2_ID = 100027;
    public static final long PROJECT1_LIKE3_ID = 100028;
    public static final long PROJECT1_LIKE4_ID = 100029;
    public static final long PROJECT2_LIKE1_ID = 100030;
    public static final long PROJECT2_LIKE2_ID = 100031;

    public static final long PROJECT1_COMMENT1_ID = 100032;
    public static final long PROJECT1_COMMENT2_ID = 100033;
    public static final long PROJECT1_COMMENT3_ID = 100034;
    public static final long PROJECT1_COMMENT4_ID = 100035;
    public static final long PROJECT1_COMMENT5_ID = 100036;
    public static final long PROJECT1_COMMENT6_ID = 100037;
    public static final long PROJECT2_COMMENT1_ID = 100038;

    public static final long PROJECT1_COMMENT1_LIKE1_ID = 100039;
    public static final long PROJECT1_COMMENT1_LIKE2_ID = 100040;
    public static final long PROJECT1_COMMENT1_LIKE3_ID = 100041;
    public static final long PROJECT1_COMMENT4_LIKE1_ID = 100042;
    public static final long PROJECT1_COMMENT4_LIKE2_ID = 100043;
    public static final long PROJECT2_COMMENT1_LIKE1_ID = 100044;

    public static final long TAG1_ID = 100045;
    public static final long TAG2_ID = 100046;
    public static final long TAG3_ID = 100047;
    public static final long TAG4_ID = 100048;

    public static final String AGGREGATOR_KEYWORD = "aggregator";

    public static final String UPDATED_COMMENT_TEXT = "updated comment text";
    public static final String PREPARED_UUID_STRING = "51bd80d0-d529-421e-a6fa-ae4f55d20d7b";

    public static final Tag tag1 = new Tag(TAG1_ID, "spring");
    public static final Tag tag2 = new Tag(TAG2_ID, "mvc");
    public static final Tag tag3 = new Tag(TAG3_ID, "desktop");
    public static final Tag tag4 = new Tag(TAG4_ID, "monolith");

    public static final Project project1 = new Project(PROJECT1_ID, "Restaurant aggregator",
            "The app offers users to get information about restaurants and vote for their favorite one.", true, ULTRA,
            LocalDate.of(2021, MARCH, 24), LocalDate.of(2021, MAY, 2), architecture1EnLocalized,
            new File("restaurant_aggregator_logo.png",
                    "./content/projects/user@gmail.com/restaurant_aggregator/logo/restaurant_aggregator_logo.png"),
            new File("docker-compose.yaml", "./content/projects/user@gmail.com/restaurant_aggregator/docker/docker-compose.yaml"),
            new File("restaurant_aggregator_preview.png",
                    "./content/projects/user@gmail.com/restaurant_aggregator/preview/restaurant_aggregator_preview.png"),
            "https://projector.ru/restaurant-aggregator", "https://github.com/ishlyakhtenkov/votingsystem",
            "https://github.com/ishlyakhtenkov/angular-votingsystem",
            "https://projector.ru/restaurant-aggregator/swagger-ui.html", 12, user);

    public static final DescriptionElement de1 = new DescriptionElement(DESCRIPTION_ELEMENT1_ID, TITLE,
            (byte) 0, "App description", null, project1);

    public static final DescriptionElement de2 = new DescriptionElement(DESCRIPTION_ELEMENT2_ID, PARAGRAPH,
            (byte) 1, "This application allows users to receive information about restaurants and their daily lunch menus, " +
            "as well as vote for their favorite restaurant once a day.", null, project1);

    public static final DescriptionElement de3 = new DescriptionElement(DESCRIPTION_ELEMENT3_ID, IMAGE,
            (byte) 2, null, new File("restaurant_aggregator_schema.png",
            "./content/projects/user@gmail.com/restaurant_aggregator/description/images/restaurant_aggregator_schema.png"),
            project1);

    public static final DescriptionElement de4 = new DescriptionElement(DESCRIPTION_ELEMENT4_ID, TITLE,
            (byte) 3, "Registration, profile", null, project1);

    public static final DescriptionElement de5 = new DescriptionElement(DESCRIPTION_ELEMENT5_ID, PARAGRAPH,
            (byte) 4, "Users can register for the app by filling in their account details on the registration page.",
            null, project1);

    public static final DescriptionElement de6 = new DescriptionElement(DESCRIPTION_ELEMENT6_ID, IMAGE,
            (byte) 5, null, new File("registration_and_profile.png",
            "./content/projects/user@gmail.com/restaurant_aggregator/description/images/registration_and_profile.png"),
            project1);

    public static final Like project1Like1 = new Like(PROJECT1_LIKE1_ID, PROJECT1_ID, USER_ID, PROJECT);
    public static final Like project1Like2 = new Like(PROJECT1_LIKE2_ID, PROJECT1_ID, ADMIN_ID, PROJECT);
    public static final Like project1Like3 = new Like(PROJECT1_LIKE3_ID, PROJECT1_ID, USER2_ID, PROJECT);
    public static final Like project1Like4 = new Like(PROJECT1_LIKE4_ID, PROJECT1_ID, DISABLED_USER_ID, PROJECT);

    public static final Like project1Comment1Like1 = new Like(PROJECT1_COMMENT1_LIKE1_ID, PROJECT1_COMMENT1_ID, ADMIN_ID,
            COMMENT);
    public static final Like project1Comment1Like2 = new Like(PROJECT1_COMMENT1_LIKE2_ID, PROJECT1_COMMENT1_ID, USER_ID,
            COMMENT);
    public static final Like project1Comment1Like3 = new Like(PROJECT1_COMMENT1_LIKE3_ID, PROJECT1_COMMENT1_ID, USER2_ID,
            COMMENT);
    public static final Like project1Comment4Like1 = new Like(PROJECT1_COMMENT4_LIKE1_ID, PROJECT1_COMMENT4_ID, ADMIN_ID,
            COMMENT);
    public static final Like project1Comment4Like2 = new Like(PROJECT1_COMMENT4_LIKE2_ID, PROJECT1_COMMENT4_ID, USER2_ID,
            COMMENT);

    public static final Comment project1Comment1 = new Comment(PROJECT1_COMMENT1_ID, PROJECT1_ID, admin, null,
            "admin 1st comment", parseLocalDateTime("2024-09-11 11:44:56"), null, false,
            Set.of(project1Comment1Like1, project1Comment1Like2, project1Comment1Like3));

    public static final Comment project1Comment2 = new Comment(PROJECT1_COMMENT2_ID, PROJECT1_ID, admin, null,
            "admin 2nd comment", parseLocalDateTime("2024-09-11 12:35:44"), parseLocalDateTime("2024-09-11 13:21:32"),
            false, Set.of());

    public static final Comment project1Comment3 = new Comment(PROJECT1_COMMENT3_ID, PROJECT1_ID, user, null,
            "user 1st comment", parseLocalDateTime("2024-09-11 11:55:37"), null, false, Set.of());

    public static final Comment project1Comment4 = new Comment(PROJECT1_COMMENT4_ID, PROJECT1_ID, user,
            PROJECT1_COMMENT1_ID, "user 2nd comment for admin 1st comment", parseLocalDateTime("2024-09-11 11:57:23"),
            null, false, Set.of(project1Comment4Like1, project1Comment4Like2));

    public static final Comment project1Comment5 = new Comment(PROJECT1_COMMENT5_ID, PROJECT1_ID, user,
            PROJECT1_COMMENT4_ID, "user 3rd comment for its user 2nd comment", parseLocalDateTime("2024-09-11 12:14:13"),
            null, false, Set.of());

    public static final Comment project1Comment6 = new Comment(PROJECT1_COMMENT6_ID, PROJECT1_ID, user,
            PROJECT1_COMMENT1_ID, "user 4th comment deleted", parseLocalDateTime("2024-09-11 13:18:53"), null, true,
            Set.of());

    public static final  Map<Comment, Integer> project1CommentIndents = new LinkedHashMap<>();

    static {
        project1CommentIndents.put(project1Comment1, 0);
        project1CommentIndents.put(project1Comment4, 1);
        project1CommentIndents.put(project1Comment5, 2);
        project1CommentIndents.put(project1Comment6, 1);
        project1CommentIndents.put(project1Comment3, 0);
        project1CommentIndents.put(project1Comment2, 0);
    }

    static {
        project1.setCreated(parseLocalDateTime("2021-07-02 11:24:54"));
        project1.getTechnologies().addAll(Set.of(technology1, technology2, technology3));
        project1.setDescriptionElements(new TreeSet<>(Set.of(de1, de2, de3, de4, de5, de6)));
        project1.setLikes(Set.of(project1Like1, project1Like2, project1Like3, project1Like4));
        project1.setComments(List.of(project1Comment1, project1Comment3, project1Comment4, project1Comment5,
                project1Comment2, project1Comment6));
        project1.setTags(Set.of(tag1, tag2, tag4));
    }

    public static final Project project2 = new Project(PROJECT2_ID, "Skill aggregator",
            "The app creates a list of required key skills for a user-specified profession.", true, VERY_HIGH,
            LocalDate.of(2022, JULY, 17), LocalDate.of(2022, SEPTEMBER, 23), architecture1EnLocalized,
            new File("skill_aggregator_logo.png",
                    "./content/projects/admin@gmail.com/skill_aggregator/logo/skill_aggregator_logo.png"),
            new File("docker-compose.yaml",
                    "./content/projects/admin@gmail.com/skill_aggregator/docker/docker-compose.yaml"),
            new File("skill_aggregator_preview.png",
                    "./content/projects/admin@gmail.com/skill_aggregator/preview/skill_aggregator_preview.png"),
            "https://projector.ru/skill-aggregator", "https://github.com/ishlyakhtenkov/skillaggregator",
            null, null, 21, admin);

    public static final Like project2Like1 = new Like(PROJECT2_LIKE1_ID, PROJECT2_ID, USER_ID, PROJECT);
    public static final Like project2Like2 = new Like(PROJECT2_LIKE2_ID, PROJECT2_ID, ADMIN_ID, PROJECT);

    public static final Like project2Comment1Like1 = new Like(PROJECT2_COMMENT1_LIKE1_ID, PROJECT2_COMMENT1_ID, USER_ID,
            COMMENT);

    public static final Comment project2Comment1 = new Comment(PROJECT2_COMMENT1_ID, PROJECT2_ID, admin, null,
            "admin comment for project 2", parseLocalDateTime("2024-09-11 14:15:39"), null, false,
            Set.of(project2Comment1Like1));

    static {
        project2.setCreated(parseLocalDateTime("2022-09-27 21:15:11"));
        project2.getTechnologies().addAll(Set.of(technology1, technology2, technology3));
        project2.setLikes(Set.of(project2Like1, project2Like2));
        project2.setComments(List.of(project2Comment1));
        project2.setTags(Set.of(tag1, tag2, tag3, tag4));
    }

    public static final Project project3 = new Project(PROJECT3_ID, "Copy maker",
            "The app creates copies of electronic documents by analyzing selected invoices and documentation inventories.",
            false, MEDIUM, LocalDate.of(2022, OCTOBER, 11), LocalDate.of(2022, DECEMBER, 29),
            architecture1EnLocalized, new File("copy_maker_logo.png",
            "./content/projects/user@gmail.com/copy_maker/logo/copy_maker_logo.png"), null,
            new File("copy_maker_preview.png",
                    "./content/projects/user@gmail.com/copy_maker/preview/copy_maker_preview.png"),
            null, "https://github.com/ishlyakhtenkov/doccopymaker", null, null, 7, user);

    static {
        project3.setCreated(parseLocalDateTime("2023-01-05 13:55:21"));
        project3.getTechnologies().addAll(Set.of(technology1, technology2));
    }

    public static final MockMultipartFile NEW_LOGO_FILE = new MockMultipartFile("logo.inputtedFile",
            "New project logo.png",  MediaType.IMAGE_PNG_VALUE, "new logo file content bytes".getBytes());

    public static final MockMultipartFile NEW_PREVIEW_FILE = new MockMultipartFile("preview.inputtedFile",
            "New project preview.png", MediaType.IMAGE_PNG_VALUE, "new preview file bytes".getBytes());

    public static final MockMultipartFile NEW_DOCKER_COMPOSE_FILE = new MockMultipartFile("dockerCompose.inputtedFile",
            "docker-compose.yaml", MediaType.TEXT_PLAIN_VALUE, "new docker compose file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_LOGO_FILE = new MockMultipartFile("logo.inputtedFile",
            "updated project logo.png", MediaType.IMAGE_PNG_VALUE, "updated logo file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_PREVIEW_FILE = new MockMultipartFile("preview.inputtedFile",
            "Updated project preview.png", MediaType.IMAGE_PNG_VALUE, "updated preview file bytes".getBytes());

    public static final MockMultipartFile UPDATED_DOCKER_COMPOSE_FILE = new MockMultipartFile("dockerCompose.inputtedFile",
            "docker-compose-updated.yaml", MediaType.TEXT_PLAIN_VALUE, "updated docker compose file bytes".getBytes());

    public static ProjectTo getNewTo() {
        return new ProjectTo(null, "New project name", "New project annotation", true, HIGH,
                LocalDate.of(2022, MARCH, 11), LocalDate.of(2022, DECEMBER, 25), architecture1EnLocalized,
                new FileTo(null, null, NEW_LOGO_FILE, null), new FileTo(null, null, NEW_DOCKER_COMPOSE_FILE, null),
                new FileTo(null, null, NEW_PREVIEW_FILE, null), "https://newprojectname.ru",
                "https://github.com/ishlyakhtenkov/newprojectname", null, "https://newprojectname.ru/swagger-ui.html",
                 Set.of(TECHNOLOGY1_ID, TECHNOLOGY2_ID, TECHNOLOGY3_ID),
                new ArrayList<>(List.of(getNewDeTo1(), getNewDeTo2(), getNewDeTo3())), null);
    }

    public static Project getNew(String contentPath) {
        ProjectTo newTo = getNewTo();
        return new Project(null, newTo.getName(), newTo.getAnnotation(), newTo.isVisible(), newTo.getPriority(),
                newTo.getStarted(), newTo.getFinished(), newTo.getArchitecture(),
                new File("new_project_logo.png", contentPath + FileUtil.normalizePath(USER_MAIL + "/" + newTo.getName() +
                        LOGO_DIR + NEW_LOGO_FILE.getOriginalFilename())),
                new File("docker-compose.yaml", contentPath + FileUtil.normalizePath(USER_MAIL + "/" + newTo.getName() +
                        DOCKER_DIR + NEW_DOCKER_COMPOSE_FILE.getOriginalFilename())),
                new File("new_project_preview.png", contentPath + FileUtil.normalizePath(USER_MAIL + "/" + newTo.getName() +
                        PREVIEW_DIR + NEW_PREVIEW_FILE.getOriginalFilename())),
                newTo.getDeploymentUrl(), newTo.getBackendSrcUrl(), newTo.getFrontendSrcUrl(), newTo.getOpenApiUrl(), 0,
                user, new TreeSet<>(Set.of(technology1, technology2, technology3)),
                new TreeSet<>(Set.of(getNewDe1(), getNewDe2(), getNewDe3())), Set.of(), List.of(),
                Set.of(tag1, tag2, new Tag(null, "hibernate"), new Tag(null, "fullstack")));
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ProjectTo newTo = getNewTo();
        DescriptionElementTo newDeTo1 = getNewDeTo1();
        DescriptionElementTo newDeTo2 = getNewDeTo2();
        DescriptionElementTo newDeTo3 = getNewDeTo3();
        params.add(NAME_PARAM, newTo.getName());
        params.add(ANNOTATION_PARAM, newTo.getAnnotation());
        params.add(VISIBLE_PARAM, String.valueOf(newTo.isVisible()));
        params.add(PRIORITY_PARAM, newTo.getPriority().name());
        params.add(STARTED_PARAM, newTo.getStarted().toString());
        params.add(FINISHED_PARAM, newTo.getFinished().toString());
        params.add(ARCHITECTURE_PARAM, String.valueOf(newTo.getArchitecture().getId()));
        params.add(DEPLOYMENT_URL_PARAM, newTo.getDeploymentUrl());
        params.add(BACKEND_SRC_URL_PARAM, newTo.getBackendSrcUrl());
        params.add(FRONTEND_SRC_URL_PARAM, newTo.getFrontendSrcUrl());
        params.add(OPEN_API_URL_PARAM, newTo.getOpenApiUrl());
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY1_ID));
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY2_ID));
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY3_ID));
        params.add("descriptionElementTos[0].type", newDeTo1.getType().name());
        params.add("descriptionElementTos[0].index", newDeTo1.getIndex().toString());
        params.add("descriptionElementTos[0].text", newDeTo1.getText());
        params.add("descriptionElementTos[1].type", newDeTo2.getType().name());
        params.add("descriptionElementTos[1].index", newDeTo2.getIndex().toString());
        params.add("descriptionElementTos[1].text", newDeTo2.getText());
        params.add("descriptionElementTos[2].type", newDeTo3.getType().name());
        params.add("descriptionElementTos[2].index", newDeTo3.getIndex().toString());
        params.add(TAGS_PARAM, "spring mvc hibernate fullstack");
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        DescriptionElementTo newDeTo1 = getNewDeTo1();
        DescriptionElementTo newDeTo2 = getNewDeTo2();
        DescriptionElementTo newDeTo3 = getNewDeTo3();
        params.add(NAME_PARAM, LONG_STRING);
        params.add(ANNOTATION_PARAM, HTML_TEXT);
        params.add(VISIBLE_PARAM, String.valueOf(true));
        params.add(PRIORITY_PARAM, HIGH.name());
        params.add(STARTED_PARAM, null);
        params.add(FINISHED_PARAM, null);
        params.add(ARCHITECTURE_PARAM, String.valueOf(architecture1.getId()));
        params.add(DEPLOYMENT_URL_PARAM, INVALID_URL);
        params.add(BACKEND_SRC_URL_PARAM, INVALID_URL);
        params.add(FRONTEND_SRC_URL_PARAM, INVALID_URL);
        params.add(OPEN_API_URL_PARAM, INVALID_URL);
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY1_ID));
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY2_ID));
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY3_ID));
        params.add("descriptionElementTos[0].type", newDeTo1.getType().name());
        params.add("descriptionElementTos[0].index", newDeTo1.getIndex().toString());
        params.add("descriptionElementTos[0].text", HTML_TEXT);
        params.add("descriptionElementTos[1].type", newDeTo2.getType().name());
        params.add("descriptionElementTos[1].index", newDeTo2.getIndex().toString());
        params.add("descriptionElementTos[1].text", HTML_TEXT);
        params.add("descriptionElementTos[2].type", newDeTo3.getType().name());
        params.add("descriptionElementTos[2].index", newDeTo3.getIndex().toString());
        params.add(TAGS_PARAM, HTML_TEXT);
        return params;
    }

    public static Project getUpdated(String projectFilesPath) {
        String updatedName = "updatedProjectName";
        Project updated = new Project(PROJECT1_ID, updatedName, "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2EnLocalized,
                new File("updated_project_logo.png", projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" +
                        updatedName + LOGO_DIR + UPDATED_LOGO_FILE.getOriginalFilename())),
                new File("docker-compose-updated.yaml", projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" +
                        updatedName + DOCKER_DIR + UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename())),
                new File("updated_project_preview.png", projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" +
                        updatedName + PREVIEW_DIR + UPDATED_PREVIEW_FILE.getOriginalFilename())),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front",
                "https://updatedProjectName.ru/swagger-ui.html", project1.getViews(), project1.getAuthor(),
                new TreeSet<>(Set.of(technology1)), new TreeSet<>(Set.of(updatedDe2, updatedDe1, updatedDe4,
                getUpdatedDe6(), updatedDe5, getNewDeForProjectUpdate())), project1.getLikes(),
                project1.getComments(), Set.of(tag1, tag2, new Tag(null, "hibernate")));
        updated.setCreated(project1.getCreated());
        return updated;
    }

    public static Project getUpdatedWithOldName(String projectFilesPath) {
        Project updated = new Project(PROJECT1_ID, project1.getName(), "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2EnLocalized,
                new File("updated_project_logo.png", projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" +
                        project1.getName() + LOGO_DIR + UPDATED_LOGO_FILE.getOriginalFilename())),
                new File("docker-compose-updated.yaml", projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" +
                        project1.getName() + DOCKER_DIR + UPDATED_DOCKER_COMPOSE_FILE.getOriginalFilename())),
                new File("updated_project_preview.png", projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" +
                        project1.getName() + PREVIEW_DIR + UPDATED_PREVIEW_FILE.getOriginalFilename())),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front",
                "https://updatedProjectName.ru/swagger-ui.html", project1.getViews(), project1.getAuthor(),
                new TreeSet<>(Set.of(technology1)), new TreeSet<>(Set.of(updatedDe2, updatedDe1, updatedDe4,
                updatedDe6WhenProjectNameNotUpdated, updatedDe5,
                newDeForProjectUpdateWithoutNameChanging)), project1.getLikes(), project1.getComments(),
                Set.of(tag1, tag2, new Tag(null, "hibernate")));
        updated.setCreated(project1.getCreated());
        return updated;
    }

    public static Project getUpdatedWithOldFiles(String projectFilesPath) {
        String updatedName = "updatedProjectName";
        Project updated = new Project(PROJECT1_ID, updatedName, "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2EnLocalized,
                new File(project1.getLogo().getFileName(), projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" +
                        updatedName + LOGO_DIR + project1.getLogo().getFileName())),
                new File(project1.getDockerCompose().getFileName(), projectFilesPath + FileUtil.normalizePath(USER_MAIL +
                        "/" + updatedName + DOCKER_DIR + project1.getDockerCompose().getFileName())),
                new File(project1.getPreview().getFileName(), projectFilesPath + FileUtil.normalizePath(USER_MAIL + "/" +
                        updatedName + PREVIEW_DIR + project1.getPreview().getFileName())),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front",
                "https://updatedProjectName.ru/swagger-ui.html", project1.getViews(), project1.getAuthor(),
                new TreeSet<>(Set.of(technology1)), new TreeSet<>(Set.of(updatedDe2, updatedDe1, updatedDe4, updatedDe6,
                updatedDe5)), project1.getLikes(), project1.getComments(), Set.of(tag1, tag2, new Tag(null, "hibernate")));
        updated.setCreated(project1.getCreated());
        return updated;
    }

    public static MultiValueMap<String, String> getUpdatedParams(String projectFilesPath) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Project updated = getUpdated(projectFilesPath);
        params.add(ID_PARAM, String.valueOf(updated.getId()));
        params.add(NAME_PARAM, updated.getName());
        params.add(ANNOTATION_PARAM, updated.getAnnotation());
        params.add(VISIBLE_PARAM, String.valueOf(updated.isVisible()));
        params.add(PRIORITY_PARAM, updated.getPriority().name());
        params.add(STARTED_PARAM, updated.getStarted().toString());
        params.add(FINISHED_PARAM, updated.getFinished().toString());
        params.add(ARCHITECTURE_PARAM, String.valueOf(updated.getArchitecture().getId()));
        params.add(DEPLOYMENT_URL_PARAM, updated.getDeploymentUrl());
        params.add(BACKEND_SRC_URL_PARAM, updated.getBackendSrcUrl());
        params.add(FRONTEND_SRC_URL_PARAM, updated.getFrontendSrcUrl());
        params.add(OPEN_API_URL_PARAM, updated.getOpenApiUrl());
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY1_ID));
        params.add(LOGO_FILE_NAME_PARAM, updated.getLogo().getFileName());
        params.add(LOGO_FILE_LINK_PARAM, updated.getLogo().getFileLink());
        params.add(PREVIEW_FILE_NAME_PARAM, updated.getPreview().getFileName());
        params.add(PREVIEW_FILE_LINK_PARAM, updated.getPreview().getFileLink());
        params.add(DOCKER_COMPOSE_FILE_NAME_PARAM, updated.getDockerCompose().getFileName());
        params.add(DOCKER_COMPOSE_FILE_LINK_PARAM, updated.getDockerCompose().getFileLink());
        params.add("descriptionElementTos[0].id", String.valueOf(updatedDe2.getId()));
        params.add("descriptionElementTos[0].type", updatedDe2.getType().name());
        params.add("descriptionElementTos[0].index", updatedDe2.getIndex().toString());
        params.add("descriptionElementTos[0].text", updatedDe2.getText());
        params.add("descriptionElementTos[1].id", String.valueOf(updatedDe1.getId()));
        params.add("descriptionElementTos[1].type", updatedDe1.getType().name());
        params.add("descriptionElementTos[1].index", updatedDe1.getIndex().toString());
        params.add("descriptionElementTos[1].text", updatedDe1.getText());
        params.add("descriptionElementTos[2].id", String.valueOf(updatedDe4.getId()));
        params.add("descriptionElementTos[2].type", updatedDe4.getType().name());
        params.add("descriptionElementTos[2].index", updatedDe4.getIndex().toString());
        params.add("descriptionElementTos[2].text", updatedDe4.getText());
        params.add("descriptionElementTos[3].id", String.valueOf(updatedDe6.getId()));
        params.add("descriptionElementTos[3].type", updatedDe6.getType().name());
        params.add("descriptionElementTos[3].index", updatedDe6.getIndex().toString());
        params.add("descriptionElementTos[4].id", String.valueOf(updatedDe5.getId()));
        params.add("descriptionElementTos[4].type", updatedDe5.getType().name());
        params.add("descriptionElementTos[4].index", updatedDe5.getIndex().toString());
        params.add("descriptionElementTos[4].text", updatedDe5.getText());
        params.add("descriptionElementTos[5].type", getNewDeToForProjectUpdate().getType().name());
        params.add("descriptionElementTos[5].index", String.valueOf(getNewDeToForProjectUpdate().getIndex()));
        params.add(TAGS_PARAM, "spring, mvc, hibernate");
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> invalidParams = getNewInvalidParams();
        invalidParams.add(ID_PARAM, String.valueOf(PROJECT1_ID));
        return invalidParams;
    }

    public static final ProjectPreviewTo project1PreviewTo = new ProjectPreviewTo(project1.getId(), project1.getAuthor(),
            project1.getName(), project1.getAnnotation(), project1.getCreated(), project1.isVisible(),
            project1.getArchitecture(), project1.getPreview(), project1.getTechnologies(), project1.getViews(),
            Set.of(project1Like1.getUserId(), project1Like2.getUserId(), project1Like3.getUserId(),
                    project1Like4.getUserId()), project1.getComments().size());

    public static final ProjectPreviewTo project2PreviewTo = new ProjectPreviewTo(project2.getId(), project2.getAuthor(),
            project2.getName(), project2.getAnnotation(), project2.getCreated(), project2.isVisible(),
            project2.getArchitecture(), project2.getPreview(), project2.getTechnologies(), project2.getViews(),
            Set.of(project2Like1.getUserId(), project2Like2.getUserId()), project2.getComments().size());

    public static final ProjectPreviewTo project3PreviewTo = new ProjectPreviewTo(project3.getId(), project3.getAuthor(),
            project3.getName(), project3.getAnnotation(), project3.getCreated(), project3.isVisible(),
            project3.getArchitecture(), project3.getPreview(), project3.getTechnologies(), project3.getViews(),
            Set.of(), project3.getComments().size());

    public static final DescriptionElement updatedDe1 = new DescriptionElement(DESCRIPTION_ELEMENT1_ID, TITLE,
            (byte) 1, "Updated App description", null, project1);

    public static final DescriptionElement updatedDe2 = new DescriptionElement(DESCRIPTION_ELEMENT2_ID, PARAGRAPH,
            (byte) 0, "Updated This application allows users to receive information about restaurants and their daily " +
            "lunch menus, " +
            "as well as vote for their favorite restaurant once a day.", null, project1);

    public static final DescriptionElement updatedDe4 = new DescriptionElement(DESCRIPTION_ELEMENT4_ID, TITLE,
            (byte) 2, "Registration, profile", null, project1);

    public static final DescriptionElement updatedDe5 = new DescriptionElement(DESCRIPTION_ELEMENT5_ID, PARAGRAPH,
            (byte) 4, "Users can register for the app by filling in their account details on the registration page.",
            null, project1);

    public static DescriptionElement getUpdatedDe6() {
        return new DescriptionElement(DESCRIPTION_ELEMENT6_ID, IMAGE,
                (byte) 3, null, new File("registration_and_profile.png",
                "./content/projects/user@gmail.com/updatedprojectname/description/images/registration_and_profile.png"),
                project1);
    }

    public static final DescriptionElement updatedDe6 = new DescriptionElement(DESCRIPTION_ELEMENT6_ID, IMAGE,
            (byte) 3, null, new File("registration_and_profile.png",
            "./content/projects/user@gmail.com/updatedprojectname/description/images/registration_and_profile.png"),
            project1);

    public static final DescriptionElement updatedDe6WhenProjectNameNotUpdated =
            new DescriptionElement(DESCRIPTION_ELEMENT6_ID, IMAGE, (byte) 3, null, new File("registration_and_profile.png",
            "./content/projects/user@gmail.com/restaurant_aggregator/description/images/registration_and_profile.png"),
                    project1);

    public static final DescriptionElement newDeForProjectUpdate = new DescriptionElement(null, IMAGE,
            (byte) 5, null, new File("updatedDeNewImage.png",
            "./content/projects/user@gmail.com/updatedprojectname/description/images/" + PREPARED_UUID_STRING +
                    "_updateddenewimage.png"), project1);

    public static DescriptionElement getNewDeForProjectUpdate() {
        return new DescriptionElement(null, IMAGE,
                (byte) 5, null, new File("updatedDeNewImage.png",
                "./content/projects/user@gmail.com/updatedprojectname/description/images/" + PREPARED_UUID_STRING +
                        "_updateddenewimage.png"), project1);
    }


    public static final DescriptionElement newDeForProjectUpdateWithoutNameChanging = new DescriptionElement(null, IMAGE,
            (byte) 5, null, new File("updatedDeNewImage.png",
            "./content/projects/user@gmail.com/restaurant_aggregator/description/images/" + PREPARED_UUID_STRING +
                    "_updateddenewimage.png"), project1);

    public static DescriptionElementTo getNewDeTo1() {
        return new DescriptionElementTo(null, TITLE, (byte) 0, "Some title", null);
    }

    public static DescriptionElementTo getNewDeTo2() {
        return new DescriptionElementTo(null, PARAGRAPH, (byte) 1, "Some paragraph", null);
    }

    public static DescriptionElementTo getNewDeTo3() {
        MockMultipartFile image = new MockMultipartFile("descriptionElementTos[2].image.inputtedFile", "deImage.png",
                MediaType.IMAGE_PNG_VALUE, "description element image file content bytes".getBytes());
        return new DescriptionElementTo(null, IMAGE, (byte) 2, null, new FileTo(null, null, image, null));
    }

    public static DescriptionElement getNewDe1() {
        return new DescriptionElement(null, TITLE, (byte) 0, "Some title", null, null);
    }

    public static DescriptionElement getNewDe2() {
        return new DescriptionElement(null, PARAGRAPH, (byte) 1, "Some paragraph", null, null);
    }

    public static DescriptionElement getNewDe3() {
        return new DescriptionElement(null, IMAGE, (byte) 2, null, new File("deImage.png",
                "./content/projects/user@gmail.com/new_project_name/description/images/" + PREPARED_UUID_STRING +
                        "_deimage.png"));
    }

    public static DescriptionElementTo getNewDeToForProjectUpdate() {
        MockMultipartFile image = new MockMultipartFile("descriptionElementTos[5].image.inputtedFile",
                "updatedDeNewImage.png", MediaType.IMAGE_PNG_VALUE,
                "description element image file content bytes".getBytes());
        return new DescriptionElementTo(null, IMAGE, (byte) 5, null, new FileTo(null, null, image, null));
    }

    public static CommentTo getNewCommentTo() {
        return new CommentTo(null, PROJECT1_ID, null, "new comment text");
    }

    public static Comment getNewComment() {
        return new Comment(null, PROJECT1_ID, user, null, "new comment text");
    }
}
