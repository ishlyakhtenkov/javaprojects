package ru.javaprojects.projector.users.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import ru.javaprojects.projector.common.HasEmailAndPassword;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.to.ProfileTo;
import ru.javaprojects.projector.users.to.UserTo;

import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.common.util.FileUtil.isFileToEmpty;
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

    public static User updateFromTo(User user, UserTo userTo, String contentPath) {
        String oldEmail = user.getEmail();
        user.setEmail(userTo.getEmail());
        user.setName(userTo.getName());
        user.setRoles(userTo.getRoles());
        if (user.getAvatar() != null && !user.getEmail().equalsIgnoreCase(oldEmail)) {
            user.getAvatar().setFileLink(contentPath + normalizePath(user.getEmail() + "/" + user.getAvatar().getFileName()));
        }
        return user;
    }

    public static User updateFromTo(User user, ProfileTo profileTo, String contentPath) {
        user.setName(profileTo.getName());
        if (!isFileToEmpty(profileTo.getAvatar())) {
            user.setAvatar(createAvatarFile(profileTo, user.getEmail(), contentPath));
        }
        return user;
    }

    private File createAvatarFile(ProfileTo profileTo, String currentEmail, String contentPath) {
        FileTo avatar = profileTo.getAvatar();
        Assert.notNull(avatar, "avatar must not be null");
        String filename = normalizePath(avatar.getInputtedFile() != null && !avatar.getInputtedFile().isEmpty() ?
                avatar.getInputtedFile().getOriginalFilename() : avatar.getFileName());
        return new File(filename, contentPath + normalizePath(currentEmail + "/" + filename));
    }
}
