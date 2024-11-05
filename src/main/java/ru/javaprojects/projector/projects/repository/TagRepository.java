package ru.javaprojects.projector.projects.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.BaseRepository;
import ru.javaprojects.projector.projects.model.Tag;

import java.util.Set;

@Transactional(readOnly = true)
public interface TagRepository extends BaseRepository<Tag> {

    Set<Tag> findAllByNameIn(Set<String> names);
}
