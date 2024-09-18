package ru.javaprojects.projector.projects;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.projects.model.DescriptionElement;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.to.DescriptionElementTo;
import ru.javaprojects.projector.projects.to.ProjectTo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    public static final String LOGO_FILE_NAME_PARAM = "logo.fileName";
    public static final String LOGO_FILE_LINK_PARAM = "logo.fileLink";
    public static final String LOGO_FILE_AS_BYTES_PARAM = "logo.inputtedFileBytes";
    public static final String CARD_IMAGE_FILE_NAME_PARAM = "cardImage.fileName";
    public static final String CARD_IMAGE_FILE_LINK_PARAM = "cardImage.fileLink";
    public static final String CARD_IMAGE_FILE_AS_BYTES_PARAM = "cardImage.inputtedFileBytes";
    public static final String DOCKER_COMPOSE_FILE_NAME_PARAM = "dockerCompose.fileName";
    public static final String DOCKER_COMPOSE_FILE_LINK_PARAM = "dockerCompose.fileLink";
    public static final String DOCKER_COMPOSE_FILE_AS_BYTES_PARAM = "dockerCompose.inputtedFileBytes";

    public static final long DESCRIPTION_ELEMENT1_ID = 100020;
    public static final long DESCRIPTION_ELEMENT2_ID = 100021;
    public static final long DESCRIPTION_ELEMENT3_ID = 100022;
    public static final long DESCRIPTION_ELEMENT4_ID = 100023;
    public static final long DESCRIPTION_ELEMENT5_ID = 100024;
    public static final long DESCRIPTION_ELEMENT6_ID = 100025;

    public static final String INVALID_SHORT_DESCRIPTION = "<p>short description html</p>";

    public static final Project project1 = new Project(PROJECT1_ID, "Restaurant aggregator",
            "The app offers users to get information about restaurants and vote for their favorite one.", true, ULTRA,
            LocalDate.of(2021, MARCH, 24), LocalDate.of(2021, MAY, 2), architecture1,
            new File("restaurant_aggregator_logo.png", "./content/projects/restaurant_aggregator/logo/restaurant_aggregator_logo.png"),
            new File("docker-compose.yaml", "./content/projects/restaurant_aggregator/docker/docker-compose.yaml"),
            new File("restaurant_aggregator_card_img.png", "./content/projects/restaurant_aggregator/card_img/restaurant_aggregator_card_img.png"),
            "https://projector.ru/restaurant-aggregator", "https://github.com/ishlyakhtenkov/votingsystem",
            "https://github.com/ishlyakhtenkov/angular-votingsystem", "https://projector.ru/restaurant-aggregator/swagger-ui.html");

    public static final DescriptionElement de1 = new DescriptionElement(DESCRIPTION_ELEMENT1_ID, TITLE,
            (byte) 0, "App description", null, null, project1);

    public static final DescriptionElement de2 = new DescriptionElement(DESCRIPTION_ELEMENT2_ID, PARAGRAPH,
            (byte) 1, "This application allows users to receive information about restaurants and their daily lunch menus, " +
            "as well as vote for their favorite restaurant once a day.", null, null, project1);

    public static final DescriptionElement de3 = new DescriptionElement(DESCRIPTION_ELEMENT3_ID, IMAGE,
            (byte) 2, null, "restaurant_aggregator_schema.png",
            "./content/projects/restaurant_aggregator/description/images/restaurant_aggregator_schema.png", project1);

    public static final DescriptionElement de4 = new DescriptionElement(DESCRIPTION_ELEMENT4_ID, TITLE,
            (byte) 3, "Registration, profile", null, null, project1);

    public static final DescriptionElement de5 = new DescriptionElement(DESCRIPTION_ELEMENT5_ID, PARAGRAPH,
            (byte) 4, "Users can register for the app by filling in their account details on the registration page.",
            null, null, project1);

    public static final DescriptionElement de6 = new DescriptionElement(DESCRIPTION_ELEMENT6_ID, IMAGE,
            (byte) 5, null, "registration_and_profile.png",
            "./content/projects/restaurant_aggregator/description/images/registration_and_profile.png", project1);

    static {
        project1.getTechnologies().addAll(Set.of(technology1, technology2, technology3));
        project1.getDescriptionElements().addAll(Set.of(de1, de2, de3, de4, de5, de6));
    }

    public static final Project project2 = new Project(PROJECT2_ID, "Skill aggregator",
            "The app creates a list of required key skills for a user-specified profession.", true, VERY_HIGH,
            LocalDate.of(2022, JULY, 17), LocalDate.of(2022, SEPTEMBER, 23), architecture1,
            new File("skill_aggregator_logo.png", "./content/projects/skill_aggregator/logo/skill_aggregator_logo.png"),
            new File("docker-compose.yaml", "./content/projects/skill_aggregator/docker/docker-compose.yaml"),
            new File("skill_aggregator_card_img.png", "./content/projects/skill_aggregator/card_img/skill_aggregator_card_img.png"),
            "https://projector.ru/skill-aggregator", "https://github.com/ishlyakhtenkov/skillaggregator",
            null, null);

    public static final Project project3 = new Project(PROJECT3_ID, "Copy maker",
            "The app creates copies of electronic documents by analyzing selected invoices and documentation inventories.",
            false, MEDIUM, LocalDate.of(2022, OCTOBER, 11), LocalDate.of(2022, DECEMBER, 29),
            architecture1, new File("copy_maker_logo.png", "./content/projects/copy_maker/logo/copy_maker_logo.png"), null,
            new File("copy_maker_card_img.png", "./content/projects/copy_maker/card_img/copy_maker_card_img.png"),
            null, "https://github.com/ishlyakhtenkov/doccopymaker", null, null);

    public static final MockMultipartFile LOGO_FILE = new MockMultipartFile("logo.inputtedFile", "New project logo.png",
            MediaType.IMAGE_PNG_VALUE, "new project logo file content bytes".getBytes());

    public static final MockMultipartFile CARD_IMAGE_FILE = new MockMultipartFile("cardImage.inputtedFile", "New project card image.png",
            MediaType.IMAGE_PNG_VALUE, "new project card image file content bytes".getBytes());

    public static final MockMultipartFile DOCKER_COMPOSE_FILE = new MockMultipartFile("dockerCompose.inputtedFile", "docker-compose.yaml",
            MediaType.TEXT_PLAIN_VALUE, "new project docker compose file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_LOGO_FILE = new MockMultipartFile("logo.inputtedFile", "updated project logo.png",
            MediaType.IMAGE_PNG_VALUE, "updated project logo file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_CARD_IMAGE_FILE = new MockMultipartFile("cardImage.inputtedFile", "Updated project card image.png",
            MediaType.IMAGE_PNG_VALUE, "updated project card image file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_DOCKER_COMPOSE_FILE = new MockMultipartFile("dockerCompose.inputtedFile", "docker-compose-updated.yaml",
            MediaType.TEXT_PLAIN_VALUE, "updated project docker compose file content bytes".getBytes());


    public static DescriptionElementTo getNewDeTo1() {
        return new DescriptionElementTo(null, TITLE, (byte) 0, "Some title", null, null);
    }

    public static DescriptionElementTo getNewDeTo2() {
        return new DescriptionElementTo(null, PARAGRAPH, (byte) 1, "Some paragraph", null, null);
    }

    public static DescriptionElementTo getNewDeTo3() {
        return new DescriptionElementTo(null, IMAGE, (byte) 2,
                new MockMultipartFile("descriptionElementTos[2].image.inputtedFile", "deImage.png", MediaType.IMAGE_PNG_VALUE,
                        "description element image file content bytes".getBytes()));
    }

    public static DescriptionElement getNewDe1() {
        return new DescriptionElement(null, TITLE, (byte) 0, "Some title", null, null);
    }

    public static DescriptionElement getNewDe2() {
        return new DescriptionElement(null, PARAGRAPH, (byte) 1, "Some paragraph", null, null);
    }

    public static final String PREPARED_UUID_STRING = "51bd80d0-d529-421e-a6fa-ae4f55d20d7b";

    public static DescriptionElement getNewDe3() {
        return new DescriptionElement(null, IMAGE, (byte) 2, null, "deImage.png",
                "./content/projects/new_project_name/description/images/" + PREPARED_UUID_STRING + "_deimage.png");
    }

    public static ProjectTo getNewTo() {
        return new ProjectTo(null, "New project name", "New project short description", true, HIGH,
                LocalDate.of(2022, MARCH, 11), LocalDate.of(2022, DECEMBER, 25), architecture1,
                LOGO_FILE, DOCKER_COMPOSE_FILE, CARD_IMAGE_FILE, "https://newprojectname.ru",
                "https://github.com/ishlyakhtenkov/newprojectname", null,
                "https://newprojectname.ru/swagger-ui.html", Set.of(TECHNOLOGY1_ID, TECHNOLOGY2_ID, TECHNOLOGY3_ID),
                new ArrayList<>(List.of(getNewDeTo1(), getNewDeTo2(), getNewDeTo3())));
    }

    public static Project getNew(String contentPath) {
        ProjectTo newTo = getNewTo();
        return new Project(null, newTo.getName(), newTo.getShortDescription(), newTo.isEnabled(), newTo.getPriority(),
                newTo.getStartDate(), newTo.getEndDate(), newTo.getArchitecture(),
                new File("new_project_logo.png", contentPath + "new_project_name" + LOGO_DIR + "new_project_logo.png"),
                new File("docker-compose.yaml", contentPath + "new_project_name" + DOCKER_DIR + "docker-compose.yaml"),
                new File("new_project_card_image.png", contentPath + "new_project_name" + CARD_IMG_DIR + "new_project_card_image.png"),
                newTo.getDeploymentUrl(), newTo.getBackendSrcUrl(), newTo.getFrontendSrcUrl(), newTo.getOpenApiUrl(),
                new TreeSet<>(Set.of(technology1, technology2, technology3)),
                new TreeSet<>(Set.of(getNewDe1(), getNewDe2(), getNewDe3())));
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ProjectTo newTo = getNewTo();
        DescriptionElementTo newDeTo1 = getNewDeTo1();
        DescriptionElementTo newDeTo2 = getNewDeTo2();
        DescriptionElementTo newDeTo3 = getNewDeTo3();

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
        params.add("descriptionElementTos[0].type", newDeTo1.getType().name());
        params.add("descriptionElementTos[0].index", newDeTo1.getIndex().toString());
        params.add("descriptionElementTos[0].text", newDeTo1.getText());
        params.add("descriptionElementTos[1].type", newDeTo2.getType().name());
        params.add("descriptionElementTos[1].index", newDeTo2.getIndex().toString());
        params.add("descriptionElementTos[1].text", newDeTo2.getText());
        params.add("descriptionElementTos[2].type", newDeTo3.getType().name());
        params.add("descriptionElementTos[2].index", newDeTo3.getIndex().toString());
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        DescriptionElementTo newDeTo1 = getNewDeTo1();
        DescriptionElementTo newDeTo2 = getNewDeTo2();
        DescriptionElementTo newDeTo3 = getNewDeTo3();

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
        params.add("descriptionElementTos[0].type", newDeTo1.getType().name());
        params.add("descriptionElementTos[0].index", newDeTo1.getIndex().toString());
        params.add("descriptionElementTos[0].text", INVALID_SHORT_DESCRIPTION);
        params.add("descriptionElementTos[1].type", newDeTo2.getType().name());
        params.add("descriptionElementTos[1].index", newDeTo2.getIndex().toString());
        params.add("descriptionElementTos[1].text", INVALID_SHORT_DESCRIPTION);
        params.add("descriptionElementTos[2].type", newDeTo3.getType().name());
        params.add("descriptionElementTos[2].index", newDeTo3.getIndex().toString());
        return params;
    }

    public static Project getUpdated(String contentPath) {
        String updatedName = "updatedProjectName";
        return new Project(PROJECT1_ID, updatedName, "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2,
                new File("updated_project_logo.png", contentPath + updatedName.toLowerCase() + LOGO_DIR +
                        "updated_project_logo.png"),
                new File("docker-compose-updated.yaml", contentPath + updatedName.toLowerCase() + DOCKER_DIR +
                        "docker-compose-updated.yaml"),
                new File("updated_project_card_image.png", contentPath + updatedName.toLowerCase() + CARD_IMG_DIR +
                        "updated_project_card_image.png"),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front", "https://updatedProjectName.ru/swagger-ui.html",
                new TreeSet<>(Set.of(technology1)), new TreeSet<>(Set.of(updatedDe2, updatedDe1,
                updatedDe4, updatedDe6, updatedDe5, newDeForProjectUpdate)));
    }

    public static Project getUpdatedWhenOldName(String contentPath) {
        return new Project(PROJECT1_ID, project1.getName(), "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2,
                new File("updated_project_logo.png", contentPath + project1.getName().toLowerCase().replace(' ', '_') +
                        LOGO_DIR + "updated_project_logo.png"),
                new File("docker-compose-updated.yaml", contentPath + project1.getName().toLowerCase().replace(' ', '_') +
                        DOCKER_DIR + "docker-compose-updated.yaml"),
                new File("updated_project_card_image.png", contentPath + project1.getName().toLowerCase().replace(' ', '_') +
                        CARD_IMG_DIR + "updated_project_card_image.png"),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front", "https://updatedProjectName.ru/swagger-ui.html",
                new TreeSet<>(Set.of(technology1)), new TreeSet<>(Set.of(updatedDe2, updatedDe1,
                updatedDe4, updatedDe6WhenProjectHasOldName, updatedDe5,
                newDeForProjectUpdateWithOldName)));
    }

    public static Project getUpdatedWhenOldFiles(String contentPath) {
        String updatedName = "updatedProjectName";
        return new Project(PROJECT1_ID, updatedName, "updated project description", false, LOW,
                LocalDate.of(2023, FEBRUARY, 15), LocalDate.of(2023, JULY, 12), architecture2,
                new File(project1.getLogo().getFileName(), contentPath + updatedName.toLowerCase().replace(' ', '_') +
                        LOGO_DIR + project1.getLogo().getFileName()),
                new File(project1.getDockerCompose().getFileName(), contentPath + updatedName.toLowerCase().replace(' ', '_') +
                        DOCKER_DIR + project1.getDockerCompose().getFileName()),
                new File(project1.getCardImage().getFileName(), contentPath + updatedName.toLowerCase().replace(' ', '_') +
                        CARD_IMG_DIR + project1.getCardImage().getFileName()),
                "https://updatedProjectName.ru", "https://github.com/ishlyakhtenkov/updatedProjectName",
                "https://github.com/ishlyakhtenkov/updatedProjectName/front", "https://updatedProjectName.ru/swagger-ui.html",
                new TreeSet<>(Set.of(technology1)), new TreeSet<>(Set.of(updatedDe2, updatedDe1,
                updatedDe4, updatedDe6, updatedDe5)));
    }

    public static final DescriptionElement updatedDe1 = new DescriptionElement(DESCRIPTION_ELEMENT1_ID, TITLE,
            (byte) 1, "Updated App description", null, null, project1);

    public static final DescriptionElement updatedDe2 = new DescriptionElement(DESCRIPTION_ELEMENT2_ID, PARAGRAPH,
            (byte) 0, "Updated This application allows users to receive information about restaurants and their daily lunch menus, " +
            "as well as vote for their favorite restaurant once a day.", null, null, project1);

    public static final DescriptionElement updatedDe4 = new DescriptionElement(DESCRIPTION_ELEMENT4_ID, TITLE,
            (byte) 2, "Registration, profile", null, null, project1);

    public static final DescriptionElement updatedDe5 = new DescriptionElement(DESCRIPTION_ELEMENT5_ID, PARAGRAPH,
            (byte) 4, "Users can register for the app by filling in their account details on the registration page.",
            null, null, project1);

    public static final DescriptionElement updatedDe6 = new DescriptionElement(DESCRIPTION_ELEMENT6_ID, IMAGE,
            (byte) 3, null, "registration_and_profile.png",
            "./content/projects/updatedprojectname/description/images/registration_and_profile.png", project1);

    public static final DescriptionElement updatedDe6WhenProjectHasOldName = new DescriptionElement(DESCRIPTION_ELEMENT6_ID,
            IMAGE, (byte) 3, null, "registration_and_profile.png",
            "./content/projects/restaurant_aggregator/description/images/registration_and_profile.png", project1);

    public static final DescriptionElement newDeForProjectUpdate = new DescriptionElement(null, IMAGE,
            (byte) 5, null, "updatedDeNewImage.png",
            "./content/projects/updatedprojectname/description/images/" + PREPARED_UUID_STRING +
                    "_updateddenewimage.png", project1);

    public static final DescriptionElement newDeForProjectUpdateWithOldName = new DescriptionElement(null, IMAGE,
            (byte) 5, null, "updatedDeNewImage.png",
            "./content/projects/restaurant_aggregator/description/images/" + PREPARED_UUID_STRING +
                    "_updateddenewimage.png", project1);

    public static DescriptionElementTo getNewDeForProjectUpdate() {
        return new DescriptionElementTo(null, IMAGE, (byte) 5,
                new MockMultipartFile("descriptionElementTos[5].image.inputtedFile", "updatedDeNewImage.png", MediaType.IMAGE_PNG_VALUE,
                        "description element image file content bytes".getBytes()));
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
        params.add(LOGO_FILE_NAME_PARAM, updated.getLogo().getFileName());
        params.add(LOGO_FILE_LINK_PARAM, updated.getLogo().getFileLink());
        params.add(CARD_IMAGE_FILE_NAME_PARAM, updated.getCardImage().getFileName());
        params.add(CARD_IMAGE_FILE_LINK_PARAM, updated.getCardImage().getFileLink());
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
        params.add("descriptionElementTos[5].type", getNewDeForProjectUpdate().getType().name());
        params.add("descriptionElementTos[5].index", String.valueOf(getNewDeForProjectUpdate().getIndex()));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> invalidParams = getNewInvalidParams();
        invalidParams.add(ID_PARAM, String.valueOf(PROJECT1_ID));
        return invalidParams;
    }
}
