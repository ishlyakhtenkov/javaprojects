package ru.javaprojects.projector.users.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.users.model.RegisterToken;

import java.util.Optional;

@Transactional(readOnly = true)
public interface RegisterTokenRepository extends TokenRepository<RegisterToken> {

    Optional<RegisterToken> findByEmailIgnoreCase(String email);
}
