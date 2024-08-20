package ru.javaprojects.projector.references;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.references.model.LogoFile;
import ru.javaprojects.projector.references.model.Technology;

import static ru.javaprojects.projector.references.model.Priority.*;
import static ru.javaprojects.projector.references.model.Usage.BACKEND;
import static ru.javaprojects.projector.references.model.Usage.FRONTEND;
import static ru.javaprojects.projector.users.UserTestData.NAME_PARAM;

public class TechnologyTestData {
    public static final MatcherFactory.Matcher<Technology> TECHNOLOGY_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Technology.class);

    public static final String TECHNOLOGIES_ATTRIBUTE = "technologies";
    public static final String TECHNOLOGY_TO_ATTRIBUTE = "technologyTo";
    public static final String USAGES_ATTRIBUTE = "usages";
    public static final String PRIORITIES_ATTRIBUTE = "priorities";

    public static final String URL_PARAM = "url";
    public static final String USAGE_PARAM = "usage";
    public static final String PRIORITY_PARAM = "priority";

    public static final String INVALID_NAME = "a";
    public static final String INVALID_URL = "dsfdsfdfs";

    public static final long TECHNOLOGY1_ID = 100011;
    public static final long TECHNOLOGY2_ID = 100012;
    public static final long TECHNOLOGY3_ID = 100013;

    public static final Technology technology1 = new Technology(TECHNOLOGY1_ID, "Java",
            "https://www.oracle.com/java", BACKEND, ULTRA, new LogoFile("java.svg", "content/technologies/java/java.svg"));

    public static final Technology technology2 = new Technology(TECHNOLOGY2_ID, "Spring",
            "https://spring.io", BACKEND, VERY_HIGH, new LogoFile("spring.svg", "content/technologies/spring/spring.svg"));

    public static final Technology technology3 = new Technology(TECHNOLOGY3_ID, "Angular",
            "https://angular.dev", FRONTEND, HIGH, new LogoFile("angular.svg", "content/technologies/angular/angular.svg"));

    public static final MockMultipartFile LOGO_FILE = new MockMultipartFile("logoFile", "tomcat.png",
            MediaType.IMAGE_PNG_VALUE, "logo file content bytes".getBytes());

    public static TechnologyTo getNewTo() {
        return new TechnologyTo(null, "Tomcat", "https://tomcat.com", BACKEND, MEDIUM, LOGO_FILE);
    }

    public static Technology getNew(String contentPath) {
        TechnologyTo newTo = getNewTo();
        return new Technology(null, newTo.getName(), newTo.getUrl(), newTo.getUsage(), newTo.getPriority(),
                new LogoFile(newTo.getLogoFile().getOriginalFilename(),
                        contentPath + newTo.getName().toLowerCase() + "/" + newTo.getLogoFile().getOriginalFilename()));
    }

    public static MultiValueMap<String, String> getNewToParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        TechnologyTo newTo = getNewTo();
        params.add(NAME_PARAM, newTo.getName());
        params.add(URL_PARAM, newTo.getUrl());
        params.add(USAGE_PARAM, newTo.getUsage().name());
        params.add(PRIORITY_PARAM, newTo.getPriority().name());
        return params;
    }

    public static MultiValueMap<String, String> getNewToInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, INVALID_NAME);
        params.add(URL_PARAM, INVALID_URL);
        params.add(USAGE_PARAM, BACKEND.name());
        params.add(PRIORITY_PARAM, MEDIUM.name());
        return params;
    }
}
