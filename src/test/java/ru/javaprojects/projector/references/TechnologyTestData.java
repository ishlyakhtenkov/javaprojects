package ru.javaprojects.projector.references;

import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.references.model.ImageFile;
import ru.javaprojects.projector.references.model.Technology;
import ru.javaprojects.projector.references.model.Usage;

import static ru.javaprojects.projector.references.model.Usage.BACKEND;
import static ru.javaprojects.projector.references.model.Usage.FRONTEND;

public class TechnologyTestData {
    public static final MatcherFactory.Matcher<Technology> TECHNOLOGY_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Technology.class);

    public static final String TECHNOLOGIES_ATTRIBUTE = "technologies";

    public static final long TECHNOLOGY1_ID = 100011;
    public static final long TECHNOLOGY2_ID = 100012;
    public static final long TECHNOLOGY3_ID = 100013;

    public static final Technology technology1 = new Technology(TECHNOLOGY1_ID, "Java",
            "https://www.oracle.com/java", BACKEND, new ImageFile("java.svg", "content/technologies/java/java.svg"));

    public static final Technology technology2 = new Technology(TECHNOLOGY2_ID, "Spring",
            "https://spring.io", BACKEND, new ImageFile("spring.svg", "content/technologies/spring/spring.svg"));

    public static final Technology technology3 = new Technology(TECHNOLOGY3_ID, "Angular",
            "https://angular.dev", FRONTEND, new ImageFile("angular.svg", "content/technologies/angular/angular.svg"));



}
