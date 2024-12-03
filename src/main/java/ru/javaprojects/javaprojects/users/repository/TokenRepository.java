package ru.javaprojects.javaprojects.users.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.javaprojects.common.repository.BaseRepository;
import ru.javaprojects.javaprojects.users.model.token.Token;

import java.util.Optional;

@NoRepositoryBean
public interface TokenRepository<T extends Token> extends BaseRepository<T> {

    Optional<T> findByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.expiryTimestamp < CURRENT_TIMESTAMP")
    int deleteExpired();
}