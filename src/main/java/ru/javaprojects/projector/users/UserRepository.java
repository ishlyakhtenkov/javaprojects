package ru.javaprojects.projector.users;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.BaseRepository;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmailIgnoreCase(String email);
}
