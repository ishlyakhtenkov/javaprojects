package ru.javaprojects.projector.projects;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.NamedRepository;
import ru.javaprojects.projector.projects.model.Project;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ProjectRepository extends NamedRepository<Project> {

    @EntityGraph(attributePaths = "architecture")
    List<Project> findAllByOrderByName();

    List<Project> findAllByEnabledIsTrueOrderByName();

    @EntityGraph(attributePaths = {"architecture", "technologies"})
    List<Project> findAllWithArchitectureAndTechnologiesByEnabledIsTrue();

    @EntityGraph(attributePaths = {"architecture", "technologies"})
    Optional<Project> findWithTechnologiesById(long id);

    @EntityGraph(attributePaths = {"architecture", "technologies", "descriptionElements"})
    Optional<Project> findWithTechnologiesAndDescriptionById(long id);

    @EntityGraph(attributePaths = {"descriptionElements"})
    Optional<Project> findWithDescriptionById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Project> findForAddViewsById(long id);
}
