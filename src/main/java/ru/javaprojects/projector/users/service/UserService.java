package ru.javaprojects.projector.users.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.UserRepository;

import static ru.javaprojects.projector.common.config.SecurityConfig.PASSWORD_ENCODER;

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
}
