package ru.javaprojects.projector.references.architectures;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;

import static ru.javaprojects.projector.CommonTestData.*;

public class ArchitectureTestData {
    public static final MatcherFactory.Matcher<Architecture> ARCHITECTURE_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Architecture.class);


    public static final String ARCHITECTURE_ATTRIBUTE = "architecture";

    public static final String DESCRIPTION_PARAM = "description";

    public static final String INVALID_DESCRIPTION = "";

    public static final long ARCHITECTURE1_ID = 100015;
    public static final long ARCHITECTURE2_ID = 100016;

    public static final Architecture architecture1 = new Architecture(ARCHITECTURE1_ID, "Modular Monolith", "A modular monolith is an architectural pattern that structures the application into independent modules or components with well-defined boundaries.");
    public static final Architecture architecture2 = new Architecture(ARCHITECTURE2_ID, "Microservices", "Microservices architecture allow a large application to be separated into smaller independent parts, with each part having its own realm of responsibility.");


    public static Architecture getNew() {
        return new Architecture(null, "new architecture name", "new architecture description");
    }

    public static Architecture getUpdated() {
        return new Architecture(ARCHITECTURE1_ID, "updated architecture name", "updated architecture description");
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Architecture newArchitecture = getNew();
        params.add(NAME_PARAM, newArchitecture.getName());
        params.add(DESCRIPTION_PARAM, newArchitecture.getDescription());
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, INVALID_NAME_WITH_HTML);
        params.add(DESCRIPTION_PARAM, INVALID_DESCRIPTION);
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Architecture updated = getUpdated();
        params.add(ID_PARAM, String.valueOf(updated.getId()));
        params.add(NAME_PARAM, updated.getName());
        params.add(DESCRIPTION_PARAM, updated.getDescription());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> invalidParams = getNewInvalidParams();
        invalidParams.add(ID_PARAM, String.valueOf(ARCHITECTURE1_ID));
        return invalidParams;
    }
}
