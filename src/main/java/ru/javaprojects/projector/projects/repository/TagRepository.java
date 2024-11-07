package ru.javaprojects.projector.projects.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.BaseRepository;
import ru.javaprojects.projector.projects.model.Tag;

import java.util.Set;

@Transactional(readOnly = true)
public interface TagRepository extends BaseRepository<Tag> {

    Set<Tag> findAllByNameIn(Set<String> names);

    @Query("SELECT t FROM Tag t WHERE UPPER(t.name) LIKE UPPER(CONCAT('%', :keyword, '%'))")
    Page<Tag> findAllByKeyword(String keyword, Pageable pageable);
}
