package ru.javaprojects.projector.common;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CommonTestData {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String HOME_URL = "/";

    public static final String ACTION_ATTRIBUTE = "action";
    public static final String PRIORITIES_ATTRIBUTE = "priorities";
    public static final String TECHNOLOGIES_ATTRIBUTE = "technologies";
    public static final String ARCHITECTURES_ATTRIBUTE = "architectures";

    public static final String ID_PARAM = "id";
    public static final String NAME_PARAM = "name";
    public static final String LOGO_FILE_NAME_PARAM = "logo.fileName";
    public static final String LOGO_FILE_LINK_PARAM = "logo.fileLink";
    public static final String LOGO_INPUTTED_FILE_BYTES_PARAM = "logo.inputtedFileBytes";

    public static final long NOT_EXISTING_ID = 100;
    public static final String HTML_TEXT = "<h1>name</h1>";
    public static final String LONG_STRING = "MoreThan32CharactersLengthxxxxxxxxaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    public static final String INVALID_URL = "some-invalid-url.com";
    public static final String EMPTY_STRING = "";

    public static MultiValueMap<String, String> getPageableParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "2");
        return params;
    }

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static LocalDateTime parseLocalDateTime(String date) {
        return DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).parse(date, LocalDateTime::from);
    }
}
