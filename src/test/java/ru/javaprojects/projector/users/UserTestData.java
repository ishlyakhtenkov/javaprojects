package ru.javaprojects.projector.users;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.projector.MatcherFactory;
import ru.javaprojects.projector.users.model.*;
import ru.javaprojects.projector.users.to.PasswordResetTo;
import ru.javaprojects.projector.users.to.RegisterTo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static ru.javaprojects.projector.users.model.Role.USER;

public class UserTestData {
    public static final MatcherFactory.Matcher<User> USER_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(User.class, "password", "registered");

    public static final MatcherFactory.Matcher<RegisterTo> USER_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(RegisterTo.class);

    public static final MatcherFactory.Matcher<PasswordResetTo> PASSWORD_RESET_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(PasswordResetTo.class);

    public static final String USER_ATTRIBUTE = "user";
    public static final String REGISTER_TO_ATTRIBUTE = "registerTo";
    public static final String PASSWORD_RESET_TO_ATTRIBUTE = "passwordResetTo";

    public static final String PASSWORD_PARAM = "password";
    public static final String NAME_PARAM = "name";
    public static final String EMAIL_PARAM = "email";
    public static final String NEW_EMAIL_PARAM = "newEmail";
    public static final String TOKEN_PARAM = "token";
    public static final String NOT_EXISTING_EMAIL = "notExisting@gmail.com";

    public static final String NOT_EXISTING_TOKEN = UUID.randomUUID().toString();
    public static final String NEW_PASSWORD = "newPassword";
    public static final String NEW_EMAIL = "newEmail@gmail.com";
    public static final String NEW_EMAIL_SOMEONE_HAS_TOKEN = "someNew@gmail.com";
    public static final String UPDATED_NAME = "someNewName";
    public static final String INVALID_PASSWORD = "pass";
    public static final String INVALID_NAME = "<h1>name</h1>";
    public static final String INVALID_EMAIL = "invEmail.gmail.ru";

    public static final String USER_MAIL = "user@gmail.com";
    public static final String DISABLED_USER_MAIL = "userDisabled@gmail.com";
    public static final String ADMIN_MAIL = "admin@gmail.com";
    public static final String USER2_MAIL = "user2@gmail.com";

    public static final long USER_ID = 100000;
    public static final long USER2_ID = 100002;
    public static final long DISABLED_USER_ID = 100003;
    public static final long ADMIN_ID = 100001;

    public static final User user = new User(USER_ID, USER_MAIL, "John Doe", "password", true, Set.of(Role.USER));
    public static final User admin = new User(ADMIN_ID, ADMIN_MAIL, "Jack", "admin", true, Set.of(Role.USER, Role.ADMIN));
    public static final User user2 = new User(USER2_ID, USER2_MAIL, "Alice Key", "somePassword", true, Set.of(Role.USER));
    public static final User disabledUser = new User(DISABLED_USER_ID, DISABLED_USER_MAIL, "Freeman25", "password", false, Set.of(Role.USER));

    public static User getNew() {
        return new User(null, "new@gmail.com", "newName", "newPassword", true, Set.of(USER));
    }

    public static RegisterTo getNewTo() {
        return new RegisterTo(null, "new@gmail.com", "newName", "newPassword");
    }

    public static MultiValueMap<String, String> getNewToParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        RegisterTo newRegisterTo = getNewTo();
        params.add(EMAIL_PARAM, newRegisterTo.getEmail());
        params.add(NAME_PARAM, newRegisterTo.getName());
        params.add(PASSWORD_PARAM, newRegisterTo.getPassword());
        return params;
    }

    public static MultiValueMap<String, String> getNewToInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(EMAIL_PARAM, "1234abc");
        params.add(NAME_PARAM, "<p>html name</p>");
        params.add(PASSWORD_PARAM, " ");
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

    public static final ChangeEmailToken changeEmailToken = new ChangeEmailToken(100010L,
            "1a43dx02-x23x-42xx-8r42-x6ff44275y67", parseDate("2052-01-22 06:17:32"), "someNew@gmail.com", user2);

    public static final ChangeEmailToken expiredChangeEmailToken = new ChangeEmailToken(100009L,
            "5a49dd09-g23f-44bb-8d41-b6ff44275s56", parseDate("2024-08-05 21:49:01"), "some@gmail.com", admin);

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd Hh:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
