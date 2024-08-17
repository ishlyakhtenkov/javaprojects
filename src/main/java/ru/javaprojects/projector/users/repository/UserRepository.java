package ru.javaprojects.projector.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.BaseRepository;
import ru.javaprojects.projector.users.model.User;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u ORDER BY u.name, u.email")
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE UPPER(u.name) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
            "UPPER(u.email) LIKE UPPER(CONCAT('%', :keyword, '%')) ORDER BY u.name, u.email")
    Page<User> findAllByKeyword(String keyword, Pageable pageable);

}
