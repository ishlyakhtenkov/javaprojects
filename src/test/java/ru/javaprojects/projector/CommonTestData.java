package ru.javaprojects.projector;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonTestData {
    public static final String HOME_URL = "/";
    public static final String ACTION_ATTRIBUTE = "action";
    public static final String PRIORITIES_ATTRIBUTE = "priorities";
    public static final String TECHNOLOGIES_ATTRIBUTE = "technologies";
    public static final String ARCHITECTURES_ATTRIBUTE = "architectures";

    public static final String ID_PARAM = "id";
    public static final String NAME_PARAM = "name";
    public static final String ENABLED_PARAM = "enabled";

    public static final String LOGO_FILE_NAME_PARAM = "logo.fileName";
    public static final String LOGO_FILE_LINK_PARAM = "logo.fileLink";
    public static final String LOGO_FILE_AS_BYTES_PARAM = "logo.inputtedFileBytes";

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

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd Hh:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
