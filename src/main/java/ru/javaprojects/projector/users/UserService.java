package ru.javaprojects.projector.users;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.javaprojects.projector.common.error.NotFoundException;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final MessageSource messageSource;

    public User getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new NotFoundException("Not found user with email=" + email, "notfound.user", new Object[]{email}));
    }
}
