package ru.javaprojects.projector.users;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.BaseRepository;

import java.util.Optional;

@Transactional(readOnly = true)
public interface RegisterTokenRepository extends BaseRepository<RegisterToken> {

    Optional<RegisterToken> findByEmailIgnoreCase(String email);

    Optional<RegisterToken> findByToken(String token);
}
