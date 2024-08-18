package ru.javaprojects.projector;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class CommonTestData {
    public static final String HOME_URL = "/";
    public static final String ACTION_ATTRIBUTE = "action";

    public static MultiValueMap<String, String> getPageableParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "2");
        return params;
    }
}
