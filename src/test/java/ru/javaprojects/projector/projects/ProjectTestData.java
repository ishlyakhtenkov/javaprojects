package ru.javaprojects.projector.projects;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.common.model.LogoFile;
import ru.javaprojects.projector.projects.model.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import static java.time.Month.*;
import static ru.javaprojects.projector.CommonTestData.*;
import static ru.javaprojects.projector.common.model.Priority.*;
import static ru.javaprojects.projector.projects.ProjectService.*;
import static ru.javaprojects.projector.projects.model.ElementType.*;
import static ru.javaprojects.projector.references.architectures.ArchitectureTestData.architecture1;
import static ru.javaprojects.projector.references.architectures.ArchitectureTestData.architecture2;
import static ru.javaprojects.projector.references.technologies.TechnologyTestData.*;

public class ProjectTestData {
    public static final MatcherFactory.Matcher<Project> PROJECT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Project.class);

    public static final String PROJECTS_ATTRIBUTE = "projects";
    public static final String PROJECT_ATTRIBUTE = "project";
    public static final String PROJECT_TO_ATTRIBUTE = "projectTo";

    public static final long PROJECT1_ID = 100017;
    public static final long PROJECT2_ID = 100018;
    public static final long PROJECT3_ID = 100019;

    public static final String SHORT_DESCRIPTION_PARAM = "shortDescription";
    public static final String START_DATE_PARAM = "startDate";
    public static final String END_DATE_PARAM = "endDate";
    public static final String ARCHITECTURE_PARAM = "architecture";
    public static final String DEPLOYMENT_URL_PARAM = "deploymentUrl";
    public static final String BACKEND_SRC_URL_PARAM = "backendSrcUrl";
    public static final String FRONTEND_SRC_URL_PARAM = "frontendSrcUrl";
    public static final String OPEN_API_URL_PARAM = "openApiUrl";
    public static final String TECHNOLOGIES_IDS_PARAM = "technologiesIds";

    public static final String INVALID_SHORT_DESCRIPTION = "<p>short description html</p>";

    public static final long DESCRIPTION_ELEMENT1_ID = 100020;
    public static final long DESCRIPTION_ELEMENT2_ID = 100021;
    public static final long DESCRIPTION_ELEMENT3_ID = 100022;
    public static final long DESCRIPTION_ELEMENT4_ID = 100023;
    public static final long DESCRIPTION_ELEMENT5_ID = 100024;
    public static final long DESCRIPTION_ELEMENT6_ID = 100025;

    public static final Project project1 = new Project(PROJECT1_ID, "Restaurant aggregator",
            "The app offers users to get information about restaurants and vote for their favorite one.", true, ULTRA,
            LocalDate.of(2021, MARCH, 24), LocalDate.of(2021, MAY, 2), architecture1,
            new LogoFile("restaurant_aggregator_logo.png", "content/projects/restaurant_aggregator/logo/restaurant_aggregator_logo.png"),
            new DockerComposeFile("docker-compose.yaml", "content/projects/restaurant_aggregator/docker/docker-compose.yaml"),
            new CardImageFile("restaurant_aggregator_card_img.png", "content/projects/restaurant_aggregator/card_img/restaurant_aggregator_card_img.png"),
            "https://projector.ru/restaurant-aggregator", "https://github.com/ishlyakhtenkov/votingsystem",
            "https://github.com/ishlyakhtenkov/angular-votingsystem", "https://projector.ru/restaurant-aggregator/swagger-ui.html");

    public static final DescriptionElement descriptionElement1 = new DescriptionElement(DESCRIPTION_ELEMENT1_ID, TITLE,
            (byte) 0, "App description", null, null, project1);

    public static final DescriptionElement descriptionElement2 = new DescriptionElement(DESCRIPTION_ELEMENT2_ID, PARAGRAPH,
            (byte) 1, "This application allows users to receive information about restaurants and their daily lunch menus, " +
            "as well as vote for their favorite restaurant once a day.", null, null, project1);

    public static final DescriptionElement descriptionElement3 = new DescriptionElement(DESCRIPTION_ELEMENT3_ID, IMAGE,
            (byte) 2, null, "restaurant_aggregator_schema.png",
            "content/projects/restaurant_aggregator/description/images/restaurant_aggregator_schema.png", project1);

    public static final DescriptionElement descriptionElement4 = new DescriptionElement(DESCRIPTION_ELEMENT4_ID, TITLE,
            (byte) 3, "Registration, profile", null, null, project1);

    public static final DescriptionElement descriptionElement5 = new DescriptionElement(DESCRIPTION_ELEMENT5_ID, PARAGRAPH,
            (byte) 4, "Users can register for the app by filling in their account details on the registration page.",
            null, null, project1);

    public static final DescriptionElement descriptionElement6 = new DescriptionElement(DESCRIPTION_ELEMENT6_ID, IMAGE,
            (byte) 5, null, "registration_and_profile.png",
            "content/projects/restaurant_aggregator/description/images/registration_and_profile.png", project1);

    static {
        project1.getTechnologies().addAll(Set.of(technology1, technology2, technology3));
        project1.getDescriptionElements().addAll(Set.of(descriptionElement1, descriptionElement2, descriptionElement3,
                descriptionElement4, descriptionElement5, descriptionElement6));
    }

    public static final Project project2 = new Project(PROJECT2_ID, "Skill aggregator",
            "The app creates a list of required key skills for a user-specified profession.", true, VERY_HIGH,
            LocalDate.of(2022, JULY, 17), LocalDate.of(2022, SEPTEMBER, 23), architecture1,
            new LogoFile("skill_aggregator_logo.png", "content/projects/skill_aggregator/logo/skill_aggregator_logo.png"),
            new DockerComposeFile("docker-compose.yaml", "content/projects/skill_aggregator/docker/docker-compose.yaml"),
            new CardImageFile("skill_aggregator_card_img.png", "content/projects/skill_aggregator/card_img/skill_aggregator_card_img.png"),
            "https://projector.ru/skill-aggregator", "https://github.com/ishlyakhtenkov/skillaggregator",
            null, null);

    public static final Project project3 = new Project(PROJECT3_ID, "Copy maker",
            "The app creates copies of electronic documents by analyzing selected invoices and documentation inventories.",
            false, MEDIUM, LocalDate.of(2022, OCTOBER, 11), LocalDate.of(2022, DECEMBER, 29),
            architecture1, new LogoFile("copy_maker_logo.png", "content/projects/copy_maker/logo/copy_maker_logo.png"), null,
            new CardImageFile("copy_maker_card_img.png", "content/projects/copy_maker/card_img/copy_maker_card_img.png"),
            null, "https://github.com/ishlyakhtenkov/doccopymaker", null, null);

    public static final MockMultipartFile LOGO_FILE = new MockMultipartFile("logoFile", "New project logo.png",
            MediaType.IMAGE_PNG_VALUE, "new project logo file content bytes".getBytes());

    public static final MockMultipartFile CARD_IMAGE_FILE = new MockMultipartFile("cardImageFile", "New project card image.png",
            MediaType.IMAGE_PNG_VALUE, "new project card image file content bytes".getBytes());

    public static final MockMultipartFile DOCKER_COMPOSE_FILE = new MockMultipartFile("dockerComposeFile", "docker-compose.yaml",
            MediaType.TEXT_PLAIN_VALUE, "new project docker compose file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_LOGO_FILE = new MockMultipartFile("logoFile", "updated project logo.png",
            MediaType.IMAGE_PNG_VALUE, "updated project logo file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_CARD_IMAGE_FILE = new MockMultipartFile("cardImageFile", "Updated project card image.png",
            MediaType.IMAGE_PNG_VALUE, "updated project card image file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_DOCKER_COMPOSE_FILE = new MockMultipartFile("dockerComposeFile", "docker-compose-updated.yaml",
            MediaType.TEXT_PLAIN_VALUE, "updated project docker compose file content bytes".getBytes());

    public static ProjectTo getNewTo() {
        return new ProjectTo(null, "New project name", "New project short description", true, HIGH,
                LocalDate.of(2022, MARCH, 11), LocalDate.of(2022, DECEMBER, 25), architecture1,
                LOGO_FILE, DOCKER_COMPOSE_FILE, CARD_IMAGE_FILE, "https://newprojectname.ru",
                "https://github.com/ishlyakhtenkov/newprojectname", null,
                "https://newprojectname.ru/swagger-ui.html", Set.of(TECHNOLOGY1_ID, TECHNOLOGY2_ID, TECHNOLOGY3_ID), new TreeSet<>());
    }

    public static Project getNew(String contentPath) {
        ProjectTo newTo = getNewTo();
        return new Project(null, newTo.getName(), newTo.getShortDescription(), newTo.isEnabled(), newTo.getPriority(),
                newTo.getStartDate(), newTo.getEndDate(), newTo.getArchitecture(),
                new LogoFile("new_project_logo.png", contentPath + "new_project_name" + LOGO_DIR + "new_project_logo.png"),
                new DockerComposeFile("docker-compose.yaml", contentPath + "new_project_name" + DOCKER_DIR + "docker-compose.yaml"),
                new CardImageFile("new_project_card_image.png", contentPath + "new_project_name" + CARD_IMG_DIR + "new_project_card_image.png"),
                newTo.getDeploymentUrl(), newTo.getBackendSrcUrl(), newTo.getFrontendSrcUrl(), newTo.getOpenApiUrl(),
                new TreeSet<>(Set.of(technology1, technology2, technology3)));
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ProjectTo newTo = getNewTo();
        params.add(NAME_PARAM, newTo.getName());
        params.add(SHORT_DESCRIPTION_PARAM, newTo.getShortDescription());
        params.add(ENABLED_PARAM, String.valueOf(newTo.isEnabled()));
        params.add(PRIORITY_PARAM, newTo.getPriority().name());
        params.add(START_DATE_PARAM, newTo.getStartDate().toString());
        params.add(END_DATE_PARAM, newTo.getEndDate().toString());
        params.add(ARCHITECTURE_PARAM, String.valueOf(newTo.getArchitecture().getId()));
        params.add(DEPLOYMENT_URL_PARAM, newTo.getDeploymentUrl());
        params.add(BACKEND_SRC_URL_PARAM, newTo.getBackendSrcUrl());
        params.add(FRONTEND_SRC_URL_PARAM, newTo.getFrontendSrcUrl());
        params.add(OPEN_API_URL_PARAM, newTo.getOpenApiUrl());
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY1_ID));
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY2_ID));
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY3_ID));
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, INVALID_NAME);
        params.add(SHORT_DESCRIPTION_PARAM, INVALID_SHORT_DESCRIPTION);
        params.add(ENABLED_PARAM, String.valueOf(true));
        params.add(PRIORITY_PARAM, HIGH.name());
        params.add(START_DATE_PARAM, null);
        params.add(END_DATE_PARAM, null);
        params.add(ARCHITECTURE_PARAM, String.valueOf(architecture1.getId()));
        params.add(DEPLOYMENT_URL_PARAM, INVALID_URL);
        params.add(BACKEND_SRC_URL_PARAM, INVALID_URL);
        params.add(FRONTEND_SRC_URL_PARAM, INVALID_URL);
        params.add(OPEN_API_URL_PARAM, INVALID_URL);
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY1_ID));
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY2_ID));
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY3_ID));
        return params;
    }

    public static Project getUpdated(String contentPath) {
        String updatedName = "updatedProjectName";
        return new Project(PROJECT1_ID, updatedName, "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2,
                new LogoFile("updated_project_logo.png", contentPath + updatedName.toLowerCase() + LOGO_DIR + "updated_project_logo.png"),
                new DockerComposeFile("docker-compose-updated.yaml", contentPath + updatedName.toLowerCase() + DOCKER_DIR + "docker-compose-updated.yaml"),
                new CardImageFile("updated_project_card_image.png", contentPath + updatedName.toLowerCase() + CARD_IMG_DIR + "updated_project_card_image.png"),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front", "https://updatedProjectName.ru/swagger-ui.html",
                new TreeSet<>(Set.of(technology1)));
    }

    public static Project getUpdatedWhenOldName(String contentPath) {
        return new Project(PROJECT1_ID, project1.getName(), "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2,
                new LogoFile("updated_project_logo.png", contentPath + project1.getName().toLowerCase().replace(' ', '_') + LOGO_DIR + "updated_project_logo.png"),
                new DockerComposeFile("docker-compose-updated.yaml", contentPath + project1.getName().toLowerCase().replace(' ', '_') + DOCKER_DIR + "docker-compose-updated.yaml"),
                new CardImageFile("updated_project_card_image.png", contentPath + project1.getName().toLowerCase().replace(' ', '_') + CARD_IMG_DIR + "updated_project_card_image.png"),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front", "https://updatedProjectName.ru/swagger-ui.html",
                new TreeSet<>(Set.of(technology1)));
    }

    public static Project getUpdatedWhenOldFiles(String contentPath) {
        String updatedName = "updatedProjectName";
        return new Project(PROJECT1_ID, updatedName, "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2,
                new LogoFile(project1.getLogoFile().getFileName(), contentPath + updatedName.toLowerCase().replace(' ', '_') + LOGO_DIR + project1.getLogoFile().getFileName()),
                new DockerComposeFile(project1.getDockerComposeFile().getFileName(), contentPath + updatedName.toLowerCase().replace(' ', '_') + DOCKER_DIR + project1.getDockerComposeFile().getFileName()),
                new CardImageFile(project1.getCardImageFile().getFileName(), contentPath + updatedName.toLowerCase().replace(' ', '_') + CARD_IMG_DIR + project1.getCardImageFile().getFileName()),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front", "https://updatedProjectName.ru/swagger-ui.html",
                new TreeSet<>(Set.of(technology1)));
    }

    public static MultiValueMap<String, String> getUpdatedParams(String contentPath) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Project updated = getUpdated(contentPath);
        params.add(ID_PARAM, String.valueOf(updated.getId()));
        params.add(NAME_PARAM, updated.getName());
        params.add(SHORT_DESCRIPTION_PARAM, updated.getShortDescription());
        params.add(ENABLED_PARAM, String.valueOf(updated.isEnabled()));
        params.add(PRIORITY_PARAM, updated.getPriority().name());
        params.add(START_DATE_PARAM, updated.getStartDate().toString());
        params.add(END_DATE_PARAM, updated.getEndDate().toString());
        params.add(ARCHITECTURE_PARAM, String.valueOf(updated.getArchitecture().getId()));
        params.add(DEPLOYMENT_URL_PARAM, updated.getDeploymentUrl());
        params.add(BACKEND_SRC_URL_PARAM, updated.getBackendSrcUrl());
        params.add(FRONTEND_SRC_URL_PARAM, updated.getFrontendSrcUrl());
        params.add(OPEN_API_URL_PARAM, updated.getOpenApiUrl());
        params.add(TECHNOLOGIES_IDS_PARAM, String.valueOf(TECHNOLOGY1_ID));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> invalidParams = getNewInvalidParams();
        invalidParams.add(ID_PARAM, String.valueOf(PROJECT1_ID));
        return invalidParams;
    }
}
