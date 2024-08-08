package ru.javaprojects.projector.users;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static ru.javaprojects.projector.users.Role.USER;

public class UserTestData {
    public static final MatcherFactory.Matcher<User> USER_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(User.class, "password", "registered");

    public static final MatcherFactory.Matcher<UserTo> USER_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(UserTo.class);

    public static final String USER_TO_ATTRIBUTE = "userTo";
    public static final String PASSWORD = "password";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String TOKEN = "token";

    public static User getNew() {
        return new User(null, "new@gmail.com", "newName", "newPassword", true, Set.of(USER));
    }

    public static UserTo getNewTo() {
        return new UserTo(null, "new@gmail.com", "newName", "newPassword");
    }

    public static MultiValueMap<String, String> getNewToParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        UserTo newUserTo = getNewTo();
        params.add(EMAIL, newUserTo.getEmail());
        params.add(NAME, newUserTo.getName());
        params.add(PASSWORD, newUserTo.getPassword());
        return params;
    }

    public static MultiValueMap<String, String> getNewToInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(EMAIL, "1234abc");
        params.add(NAME, "<p>html name</p>");
        params.add(PASSWORD, " ");
        return params;
    }

    public static final RegisterToken expiredRegisterToken = new RegisterToken(100003L, "5a99dd09-d23f-44bb-8d41-b6ff44275d01",
            parseDate("2024-08-06 19:35:56"), "some@gmail.com", "someName", "{noop}somePassword");

    public static final RegisterToken registerToken = new RegisterToken(100004L, "52bde839-9779-4005-b81c-9131c9590d79",
            parseDate("2052-05-24 16:42:03"), "new@gmail.com", "newName", "{noop}newPassword");

    private static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd Hh:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
