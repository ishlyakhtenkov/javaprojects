package ru.javaprojects.projector.users.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.UserRepository;
import ru.javaprojects.projector.users.to.UserTo;

import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.projector.users.util.UserUtil.prepareToSave;
import static ru.javaprojects.projector.users.util.UserUtil.updateFromTo;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;

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
        updateFromTo(user, userTo);
    }

    @Transactional
    public void enable(long id, boolean enabled) {
        User user = get(id);
        user.setEnabled(enabled);
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
