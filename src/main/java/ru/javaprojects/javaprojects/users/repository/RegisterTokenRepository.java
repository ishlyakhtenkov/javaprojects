package ru.javaprojects.javaprojects.users.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.javaprojects.users.model.token.RegisterToken;

import java.util.Optional;

@Transactional(readOnly = true)
public interface RegisterTokenRepository extends TokenRepository<RegisterToken> {

    Optional<RegisterToken> findByEmailIgnoreCase(String email);
}
