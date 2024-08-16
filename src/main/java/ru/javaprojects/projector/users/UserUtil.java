package ru.javaprojects.projector.users;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import ru.javaprojects.projector.common.HasEmailAndPassword;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.to.RegisterTo;

import java.util.Set;

import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.users.model.Role.USER;

@UtilityClass
public class UserUtil {

    public static HasEmailAndPassword prepareToSave(HasEmailAndPassword user) {
        String password = user.getPassword();
        user.setPassword(StringUtils.hasText(password) ? PASSWORD_ENCODER.encode(password) : password);
        user.setEmail(user.getEmail().toLowerCase());
        return user;
    }

    public static User createNewFromTo(RegisterTo registerTo) {
        return new User(null, registerTo.getEmail(), registerTo.getName(), registerTo.getPassword(), false, Set.of(USER));
    }
}
