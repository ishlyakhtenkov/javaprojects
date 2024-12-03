package ru.javaprojects.javaprojects.reference.technologies;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.javaprojects.reference.ReferenceRepository;
import ru.javaprojects.javaprojects.reference.technologies.model.Technology;

@Transactional(readOnly = true)
public interface TechnologyRepository extends ReferenceRepository<Technology> {

    Page<Technology> findAll(Pageable pageable);

    @Query("SELECT t FROM Technology t WHERE UPPER(t.name) LIKE UPPER(CONCAT('%', :keyword, '%'))")
    Page<Technology> findAllByKeyword(String keyword, Pageable pageable);
}
