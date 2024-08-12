package ru.javaprojects.projector.users;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.users.model.PasswordResetToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static ru.javaprojects.projector.CommonTestData.DISABLED_USER_MAIL;
import static ru.javaprojects.projector.CommonTestData.USER_MAIL;
import static ru.javaprojects.projector.users.Role.USER;

public class UserTestData {
    public static final MatcherFactory.Matcher<User> USER_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(User.class, "password", "registered");

    public static final MatcherFactory.Matcher<UserTo> USER_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(UserTo.class);

    public static final MatcherFactory.Matcher<PasswordResetTo> PASSWORD_RESET_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(PasswordResetTo.class);

    public static final String USER_TO_ATTRIBUTE = "userTo";
    public static final String PASSWORD = "password";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String TOKEN = "token";
    public static final String NOT_EXISTING_EMAIL = "notExisting@gmail.com";
    public static final String PASSWORD_RESET_TO_ATTRIBUTE = "passwordResetTo";
    public static final String NOT_EXISTING_TOKEN = UUID.randomUUID().toString();
    public static final String NEW_PASSWORD = "newPassword";
    public static final String INVALID_PASSWORD = "pass";

    public static final long USER_ID = 100000;
    public static final long ADMIN_ID = 100001;

    public static final User user = new User(USER_ID, USER_MAIL, "John Doe", "password", true, Set.of(Role.USER));

    public static final User user2 = new User(100002L, "user2@gmail.com", "Alice Key", "somePassword", true, Set.of(Role.USER));

    public static final User disabledUser = new User(100003L, DISABLED_USER_MAIL, "Freeman25", "password", false, Set.of(Role.USER));

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

    public static final RegisterToken expiredRegisterToken = new RegisterToken(100004L, "5a99dd09-d23f-44bb-8d41-b6ff44275d01",
            parseDate("2024-08-06 19:35:56"), "some@gmail.com", "someName", "{noop}somePassword");

    public static final RegisterToken registerToken = new RegisterToken(100005L, "52bde839-9779-4005-b81c-9131c9590d79",
            parseDate("2052-05-24 16:42:03"), "new@gmail.com", "newName", "{noop}newPassword");

    public static final PasswordResetToken passwordResetToken = new PasswordResetToken(100006L, "5a99dd09-d23f-44bb-8d41-b6ff44275x97",
            parseDate("2052-02-05 12:10:00"), user);

    public static final PasswordResetToken expiredPasswordResetToken = new PasswordResetToken(100007L, "52bde839-9779-4005-b81c-9131c9590b41",
            parseDate("2022-02-06 19:35:56"), user2);

    public static final PasswordResetToken disabledUserPasswordResetToken = new PasswordResetToken(100008L,
            "54ghh534-9778-4005-b81c-9131c9590c63", parseDate("2052-04-25 13:48:14"), disabledUser);

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd Hh:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}