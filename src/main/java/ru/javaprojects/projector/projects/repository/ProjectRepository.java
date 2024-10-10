package ru.javaprojects.projector.projects.repository;

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
    List<Project> findAllWithArchitectureByOrderByName();

    List<Project> findAllByEnabledIsTrueOrderByName();

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes"})
    List<Project> findAllWithArchAndTechnologiesAndLikesByEnabledIsTrue();

    @EntityGraph(attributePaths = {"architecture", "technologies"})
    Optional<Project> findWithArchitectureAndTechnologiesById(long id);

    @EntityGraph(attributePaths = {"architecture", "technologies", "descriptionElements", "likes"})
    Optional<Project> findWithAllInformationById(long id);

    @EntityGraph(attributePaths = {"descriptionElements"})
    Optional<Project> findWithDescriptionById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Project> findForAddViewsById(long id);
}
