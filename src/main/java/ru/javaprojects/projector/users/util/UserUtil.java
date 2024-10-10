package ru.javaprojects.projector.users.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import ru.javaprojects.projector.common.HasEmailAndPassword;
import ru.javaprojects.projector.common.util.Util;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.to.ProfileTo;
import ru.javaprojects.projector.users.to.UserTo;

import static ru.javaprojects.projector.app.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;

@UtilityClass
public class UserUtil {

    public static HasEmailAndPassword prepareToSave(HasEmailAndPassword user) {
        String password = user.getPassword();
        user.setPassword(StringUtils.hasText(password) ? PASSWORD_ENCODER.encode(password) : password);
        user.setEmail(user.getEmail().toLowerCase());
        return user;
    }

    public static UserTo asTo(User user) {
        return new UserTo(user.getId(), user.getEmail(), user.getName(), user.getRoles());
    }

    public static ProfileTo asProfileTo(User user) {
        String avatarFileName = user.getAvatar() != null ? user.getAvatar().getFileName() : null;
        String avatarFileLink = user.getAvatar() != null ? user.getAvatar().getFileLink() : null;
        return new ProfileTo(user.getId(), user.getEmail(), user.getName(), avatarFileName, avatarFileLink);
    }

    public static User updateFromTo(User user, UserTo userTo, String avatarFilesPath) {
        String oldEmail = user.getEmail();
        user.setEmail(userTo.getEmail());
        user.setName(userTo.getName());
        user.setRoles(userTo.getRoles());
        if (!user.getEmail().equalsIgnoreCase(oldEmail) && user.getAvatar() != null) {
            user.getAvatar().setFileLink(avatarFilesPath + normalizePath(user.getEmail() + "/" + user.getAvatar().getFileName()));
        }
        return user;
    }

    public static User updateFromTo(User user, ProfileTo profileTo, String avatarFilesPath) {
        user.setName(profileTo.getName());
        if (profileTo.getAvatar() != null && !profileTo.getAvatar().isEmpty()) {
            user.setAvatar(Util.createFile(profileTo::getAvatar, avatarFilesPath, user.getEmail() + "/"));
        }
        return user;
    }
}
