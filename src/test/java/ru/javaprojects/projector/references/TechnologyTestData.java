package ru.javaprojects.projector.references;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.references.model.ImageFile;
import ru.javaprojects.projector.references.model.Technology;
import ru.javaprojects.projector.users.model.User;

import java.util.stream.Collectors;

import static ru.javaprojects.projector.references.model.Usage.BACKEND;
import static ru.javaprojects.projector.references.model.Usage.FRONTEND;
import static ru.javaprojects.projector.users.UserTestData.INVALID_NAME;
import static ru.javaprojects.projector.users.UserTestData.NAME_PARAM;

public class TechnologyTestData {
    public static final MatcherFactory.Matcher<Technology> TECHNOLOGY_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Technology.class);

    public static final String TECHNOLOGIES_ATTRIBUTE = "technologies";
    public static final String TECHNOLOGY_TO_ATTRIBUTE = "technologyTo";
    public static final String USAGES_ATTRIBUTE = "usages";

    public static final String URL_PARAM = "url";
    public static final String USAGE_PARAM = "usage";

    public static final String INVALID_NAME = "a";
    public static final String INVALID_URL = "dsfdsfdfs";

    public static final long TECHNOLOGY1_ID = 100011;
    public static final long TECHNOLOGY2_ID = 100012;
    public static final long TECHNOLOGY3_ID = 100013;

    public static final Technology technology1 = new Technology(TECHNOLOGY1_ID, "Java",
            "https://www.oracle.com/java", BACKEND, new ImageFile("java.svg", "content/technologies/java/java.svg"));

    public static final Technology technology2 = new Technology(TECHNOLOGY2_ID, "Spring",
            "https://spring.io", BACKEND, new ImageFile("spring.svg", "content/technologies/spring/spring.svg"));

    public static final Technology technology3 = new Technology(TECHNOLOGY3_ID, "Angular",
            "https://angular.dev", FRONTEND, new ImageFile("angular.svg", "content/technologies/angular/angular.svg"));

    public static final MockMultipartFile IMAGE_FILE = new MockMultipartFile("imageFile", "tomcat.png",
            MediaType.IMAGE_PNG_VALUE, "image content bytes".getBytes());

    public static TechnologyTo getNewTo() {
        return new TechnologyTo(null, "Tomcat", "https://tomcat.com", BACKEND, IMAGE_FILE);
    }

    public static Technology getNew(String contentPath) {
        TechnologyTo newTo = getNewTo();
        return new Technology(null, newTo.getName(), newTo.getUrl(), newTo.getUsage(),
                new ImageFile(newTo.getImageFile().getOriginalFilename(),
                        contentPath + newTo.getName().toLowerCase() + "/" + newTo.getImageFile().getOriginalFilename()));
    }

    public static MultiValueMap<String, String> getNewToParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        TechnologyTo newTo = getNewTo();
        params.add(NAME_PARAM, newTo.getName());
        params.add(URL_PARAM, newTo.getUrl());
        params.add(USAGE_PARAM, newTo.getUsage().name());
        return params;
    }

    public static MultiValueMap<String, String> getNewToInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, INVALID_NAME);
        params.add(URL_PARAM, INVALID_URL);
        params.add(USAGE_PARAM, BACKEND.name());
        return params;
    }
}
