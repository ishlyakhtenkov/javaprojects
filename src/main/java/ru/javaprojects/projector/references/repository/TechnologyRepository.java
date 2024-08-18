package ru.javaprojects.projector.references.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.BaseRepository;
import ru.javaprojects.projector.references.model.Technology;

@Transactional(readOnly = true)
public interface TechnologyRepository extends BaseRepository<Technology> {

    Page<Technology> findAllByOrderByName(Pageable pageable);

    @Query("SELECT t FROM Technology t WHERE UPPER(t.name) LIKE UPPER(CONCAT('%', :keyword, '%')) ORDER BY t.name")
    Page<Technology> findAllByKeyword(String keyword, Pageable pageable);
}
