package ru.javaprojects.projector.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.users.AuthUser;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.UserRepository;
import ru.javaprojects.projector.users.to.ProfileTo;
import ru.javaprojects.projector.users.to.UserTo;

import java.util.Set;
import java.util.stream.Collectors;

import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;
import static ru.javaprojects.projector.users.util.UserUtil.prepareToSave;
import static ru.javaprojects.projector.users.util.UserUtil.updateFromTo;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final SessionRegistry sessionRegistry;
    private final ChangeEmailService changeEmailService;

    @Value("${content-path.avatars}")
    private String contentPath;

    public User getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new NotFoundException("Not found user with email=" + email, "notfound.user", new Object[]{email}));
    }

    public User get(long id) {
        return repository.getExisted(id);
    }

    @Transactional
    public void changePassword(long id, String password) {
        Assert.notNull(password, "password must not be null");
        User user = get(id);
        user.setPassword(PASSWORD_ENCODER.encode(password));
    }

    @Transactional
    public void changePassword(long id, String currentPassword, String newPassword) {
        Assert.notNull(currentPassword, "currentPassword must not be null");
        Assert.notNull(newPassword, "newPassword must not be null");
        User user = get(id);
        if (PASSWORD_ENCODER.matches(currentPassword, user.getPassword())) {
            user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        } else {
            throw new IllegalRequestDataException("Current password for user with id=" + id + " is incorrect",
                    "profile.incorrect-password", null);
        }
    }

    @Transactional
    public void update(long id, String name) {
        Assert.notNull(name, "name must not be null");
        User user = get(id);
        user.setName(name);
    }

    public Page<User> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> getAll(Pageable pageable, String keyword) {
        return repository.findAllByKeyword(keyword, pageable);
    }

    public void create(User user) {
        Assert.notNull(user, "user must not be null");
        repository.save((User) prepareToSave(user));
    }

    @Transactional
    public void update(UserTo userTo) {
        Assert.notNull(userTo, "userTo must not be null");
        User user = get(userTo.getId());
        String oldEmail = user.getEmail();
        String avatarFileLink = user.getAvatar() != null ? user.getAvatar().getFileLink() : null;
        repository.saveAndFlush(updateFromTo(user, userTo, contentPath));
        if (avatarFileLink != null && !user.getEmail().equalsIgnoreCase(oldEmail)) {
            FileUtil.moveFile(avatarFileLink, contentPath + FileUtil.normalizePath(user.getEmail()));
        }
    }

    @Transactional
    public User update(ProfileTo profileTo) {
        Assert.notNull(profileTo, "profileTo must not be null");
        User user = get(profileTo.getId());
        File oldAvatar = user.getAvatar();
        repository.saveAndFlush(updateFromTo(user, profileTo, contentPath));
        if (!profileTo.getAvatar().isEmpty()) {
            uploadImage(profileTo, user.getEmail());
            if (oldAvatar != null && !oldAvatar.hasExternalLink() &&
                    !oldAvatar.getFileLink().equalsIgnoreCase(user.getAvatar().getFileLink())) {
                FileUtil.deleteFile(oldAvatar.getFileLink());
            }
        }
        if (!profileTo.getEmail().equalsIgnoreCase(user.getEmail())) {
            changeEmailService.changeEmail(user.id(), profileTo.getEmail());
        }
        return user;
    }

    private void uploadImage(ProfileTo profileTo, String userEmail) {
        FileTo avatar = profileTo.getAvatar();
        String filename = normalizePath(avatar.getInputtedFile() != null && !avatar.getInputtedFile().isEmpty() ?
                avatar.getInputtedFile().getOriginalFilename() : avatar.getFileName());
        FileUtil.upload(avatar, contentPath + FileUtil.normalizePath(userEmail) + "/", filename);
    }

    @Transactional
    public void enable(long id, boolean enabled) {
        User user = get(id);
        user.setEnabled(enabled);
        if (!enabled) {
            sessionRegistry.getAllPrincipals().stream()
                    .filter(principal -> ((AuthUser) principal).id() == id)
                    .findFirst().
                    ifPresent(o -> sessionRegistry.getAllSessions(o, false)
                            .forEach(SessionInformation::expireNow));
        }
    }

    @Transactional
    public void delete(long id) {
        User user = get(id);
        repository.delete(user);
        repository.flush();
        if (user.getAvatar() != null) {
            FileUtil.deleteFile(user.getAvatar().getFileLink());
        }
    }

    public Set<Long> getOnlineUsersIds() {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> !sessionRegistry.getAllSessions(principal, false).isEmpty())
                .map(principal -> ((AuthUser) principal).id())
                .collect(Collectors.toSet());
    }
}
