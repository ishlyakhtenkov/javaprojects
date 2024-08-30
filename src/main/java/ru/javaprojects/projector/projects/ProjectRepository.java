package ru.javaprojects.projector.projects;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.NamedRepository;
import ru.javaprojects.projector.projects.model.Project;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ProjectRepository extends NamedRepository<Project> {

    @EntityGraph(attributePaths = "architecture")
    List<Project> findAllByOrderByName();

    @EntityGraph(attributePaths = {"architecture", "technologies"})
    Optional<Project> findWithTechnologiesById(long id);
}
