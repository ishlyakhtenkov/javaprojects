package ru.javaprojects.projector.users.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.BaseRepository;
import ru.javaprojects.projector.users.model.ChangeEmailToken;

import java.util.Optional;

@Transactional(readOnly = true)
public interface ChangeEmailTokenRepository extends BaseRepository<ChangeEmailToken> {

    Optional<ChangeEmailToken> findByUser_Id(long id);

    @Query("SELECT c FROM ChangeEmailToken c LEFT JOIN FETCH c.user WHERE c.token =:token")
    Optional<ChangeEmailToken> findByToken(String token);
}
