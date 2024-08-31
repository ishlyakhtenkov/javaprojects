package ru.javaprojects.projector;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class CommonTestData {
    public static final String HOME_URL = "/";
    public static final String ACTION_ATTRIBUTE = "action";
    public static final String PRIORITIES_ATTRIBUTE = "priorities";
    public static final String TECHNOLOGIES_ATTRIBUTE = "technologies";
    public static final String ARCHITECTURES_ATTRIBUTE = "architectures";

    public static final String ID_PARAM = "id";
    public static final String NAME_PARAM = "name";
    public static final String ENABLED_PARAM = "enabled";

    public static final String INVALID_NAME_WITH_HTML = "<h1>name</h1>";
    public static final String INVALID_NAME = "a";
    public static final String INVALID_URL = "some-invalid-url.com";

    public static final long NOT_EXISTING_ID = 100;

    public static MultiValueMap<String, String> getPageableParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "2");
        return params;
    }
}
