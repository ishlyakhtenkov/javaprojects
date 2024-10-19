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

    @EntityGraph(attributePaths = {"architecture", "author"})
    List<Project> findAllWithArchitectureAndAuthorByOrderByName();

    List<Project> findAllByEnabledIsTrueOrderByName();

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author"})
    List<Project> findAllWithAllInformationByEnabledIsTrue();

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author", "descriptionElements"})
    Optional<Project> findWithAllInformationAndDescriptionById(long id);

    @EntityGraph(attributePaths = {"author", "descriptionElements"})
    Optional<Project> findWithDescriptionAndAuthorById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Project> findForAddViewsById(long id);

    Optional<Project> findByAuthor_IdAndName(long authorId, String name);

    @EntityGraph(attributePaths = "author")
    Optional<Project> findWithAuthorById(long id);
}
