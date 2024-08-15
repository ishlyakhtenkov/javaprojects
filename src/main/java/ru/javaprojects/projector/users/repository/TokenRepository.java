package ru.javaprojects.projector.users.repository;

import org.springframework.data.repository.NoRepositoryBean;
import ru.javaprojects.projector.common.BaseRepository;
import ru.javaprojects.projector.users.model.Token;

import java.util.Optional;

@NoRepositoryBean
public interface TokenRepository<T extends Token> extends BaseRepository<T> {

    Optional<T> findByToken(String token);
}
