package ru.javaprojects.projector.projects;

import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.common.model.LogoFile;
import ru.javaprojects.projector.projects.model.CardImageFile;
import ru.javaprojects.projector.projects.model.DockerComposeFile;
import ru.javaprojects.projector.projects.model.Project;

import java.time.LocalDate;
import java.util.Set;

import static java.time.Month.*;
import static ru.javaprojects.projector.common.model.Priority.*;
import static ru.javaprojects.projector.references.architectures.ArchitectureTestData.architecture1;
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

    public static final Project project1 = new Project(PROJECT1_ID, "Restaurant aggregator",
            "The app offers users to get information about restaurants and vote for their favorite one.", true, ULTRA,
            LocalDate.of(2021, MARCH, 24), LocalDate.of(2021, MAY, 2), architecture1,
            new LogoFile("restaurant_aggregator_logo.png", "content/projects/restaurant_aggregator/logo/restaurant_aggregator_logo.png"),
            new DockerComposeFile("docker-compose.yaml", "content/projects/restaurant_aggregator/docker/docker-compose.yaml"),
            new CardImageFile("restaurant_aggregator_card_img.png", "content/projects/restaurant_aggregator/card_img/restaurant_aggregator_card_img.png"),
            "https://projector.ru/restaurant-aggregator", "https://github.com/ishlyakhtenkov/votingsystem",
            "https://github.com/ishlyakhtenkov/angular-votingsystem", "https://projector.ru/restaurant-aggregator/swagger-ui.html");

    static {
        project1.getTechnologies().addAll(Set.of(technology1, technology2, technology3));
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



}
