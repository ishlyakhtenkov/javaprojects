package ru.javaprojects.projector.users;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import ru.javaprojects.projector.common.HasEmailAndPassword;

import java.util.Set;

import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.users.Role.USER;

@UtilityClass
public class UserUtil {

    public static HasEmailAndPassword prepareToSave(HasEmailAndPassword user) {
        String password = user.getPassword();
        user.setPassword(StringUtils.hasText(password) ? PASSWORD_ENCODER.encode(password) : password);
        user.setEmail(user.getEmail().toLowerCase());
        return user;
    }

    public static User createNewFromTo(UserTo userTo) {
        return new User(null, userTo.getEmail(), userTo.getName(), userTo.getPassword(), false, Set.of(USER));
    }
}