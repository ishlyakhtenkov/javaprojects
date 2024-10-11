package ru.javaprojects.projector.reference.architectures;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.util.FileUtil;

import static ru.javaprojects.projector.common.CommonTestData.*;

public class ArchitectureTestData {
    public static final MatcherFactory.Matcher<Architecture> ARCHITECTURE_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Architecture.class);

    public static final String ARCHITECTURE_TO_ATTRIBUTE = "architectureTo";

    public static final String DESCRIPTION_PARAM = "description";

    public static final long ARCHITECTURE1_ID = 100015;
    public static final long ARCHITECTURE2_ID = 100016;

    public static final String ARCHITECTURES_TEST_DATA_FILES_PATH = "src/test/test-data-files/architectures";

    public static final Architecture architecture1 = new Architecture(ARCHITECTURE1_ID, "Modular Monolith",
            "A modular monolith is an architectural pattern that structures the application into independent modules or " +
                    "components with well-defined boundaries.", new File("modular_monolith.png",
            "./content/architectures/modular_monolith/modular_monolith.png"));

    public static final Architecture architecture2 = new Architecture(ARCHITECTURE2_ID, "Microservices", "Microservices " +
            "architecture allow a large application to be separated into smaller independent parts, with each part having " +
            "its own realm of responsibility.", new File("microservices.png",
            "./content/architectures/microservices/microservices.png"));

    public static final MockMultipartFile NEW_LOGO_FILE = new MockMultipartFile("logo.inputtedFile", "Event driven.png",
            MediaType.IMAGE_PNG_VALUE, "logo file content bytes".getBytes());

    public static final MockMultipartFile UPDATED_LOGO_FILE = new MockMultipartFile("logo.inputtedFile", "updated.png",
            MediaType.IMAGE_PNG_VALUE, "updated logo file content bytes".getBytes());

    public static ArchitectureTo getNewTo() {
        return new ArchitectureTo(null, "Event Driven", "Event Driven architecture description", NEW_LOGO_FILE);
    }

    public static Architecture getNew(String architectureFilesPath) {
        ArchitectureTo newTo = getNewTo();
        return new Architecture(null, newTo.getName(), newTo.getDescription(), new File("event_driven.png",
                architectureFilesPath + FileUtil.normalizePath(newTo.getName() + "/" + "event_driven.png")));
    }

    public static Architecture getUpdated(String architectureFilesPath) {
        String updatedName = "updated architecture name";
        return new Architecture(ARCHITECTURE1_ID, updatedName, "updated architecture description",
                new File(UPDATED_LOGO_FILE.getOriginalFilename(), architectureFilesPath +
                        FileUtil.normalizePath(updatedName + "/" + UPDATED_LOGO_FILE.getOriginalFilename())));
    }

    public static Architecture getUpdatedWithOldLogo(String architectureFilesPath) {
        String updatedName = "updated architecture name";
        return new Architecture(ARCHITECTURE1_ID, updatedName, "updated architecture description",
                new File(architecture1.getLogo().getFileName(), architectureFilesPath +
                        FileUtil.normalizePath(updatedName + "/" + architecture1.getLogo().getFileName())));
    }

    public static Architecture getUpdatedWithOldName(String architectureFilesPath) {
        return new Architecture(ARCHITECTURE1_ID, architecture1.getName(), "updated architecture description",
                new File(UPDATED_LOGO_FILE.getOriginalFilename(),
                        architectureFilesPath + FileUtil.normalizePath(architecture1.getName() + "/" +
                        UPDATED_LOGO_FILE.getOriginalFilename())));
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ArchitectureTo newTo = getNewTo();
        params.add(NAME_PARAM, newTo.getName());
        params.add(DESCRIPTION_PARAM, newTo.getDescription());
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, LONG_STRING);
        params.add(DESCRIPTION_PARAM, EMPTY_STRING);
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedParams(String architectureFilesPath) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Architecture updated = getUpdated(architectureFilesPath);
        params.add(ID_PARAM, String.valueOf(updated.getId()));
        params.add(NAME_PARAM, updated.getName());
        params.add(DESCRIPTION_PARAM, updated.getDescription());
        params.add(LOGO_FILE_NAME_PARAM, updated.getLogo().getFileName());
        params.add(LOGO_FILE_LINK_PARAM, updated.getLogo().getFileLink());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams(String architectureFilesPath) {
        MultiValueMap<String, String> invalidParams = getNewInvalidParams();
        Architecture updated = getUpdated(architectureFilesPath);
        invalidParams.add(ID_PARAM, String.valueOf(ARCHITECTURE1_ID));
        invalidParams.add(LOGO_FILE_NAME_PARAM, updated.getLogo().getFileName());
        invalidParams.add(LOGO_FILE_LINK_PARAM, updated.getLogo().getFileLink());
        return invalidParams;
    }
}
