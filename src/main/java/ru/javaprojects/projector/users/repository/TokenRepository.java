package ru.javaprojects.projector.users.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.BaseRepository;
import ru.javaprojects.projector.users.model.Token;

import java.util.Optional;

@NoRepositoryBean
public interface TokenRepository<T extends Token> extends BaseRepository<T> {

    Optional<T> findByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.expiryDate < CURRENT_TIMESTAMP")
    int deleteExpired();
}
