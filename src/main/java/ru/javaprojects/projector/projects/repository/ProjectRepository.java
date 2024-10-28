package ru.javaprojects.projector.projects.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.NamedRepository;
import ru.javaprojects.projector.projects.model.Project;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ProjectRepository extends NamedRepository<Project> {

    @EntityGraph(attributePaths = {"architecture", "author"})
    List<Project> findAllWithArchitectureAndAuthorByOrderByName();

    List<Project> findAllByVisibleIsTrueOrderByName();

    @Query("SELECT p.id FROM Project p ORDER BY p.name")
    Page<Long> findAllIdsOrderByName(Pageable pageable);

    @Query("SELECT p.id FROM Project p WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :keyword, '%')) ORDER BY p.name")
    Page<Long> findAllIdsByKeywordOrderByName(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"architecture", "author", "likes"})
    List<Project> findAllWithArchitectureAndAuthorAndLikesByIdInOrderByName(List<Long> projectsIds);

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author"})
    List<Project> findAllWithAllInformationByVisibleIsTrue();

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author"})
    List<Project> findAllWithAllInformationByAuthor_IdAndVisibleIsTrue(long userId);

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author"})
    List<Project> findAllWithAllInformationByAuthor_Id(long userId);

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author", "descriptionElements"})
    Optional<Project> findWithAllInformationAndDescriptionById(long id);

    @EntityGraph(attributePaths = {"author", "descriptionElements"})
    Optional<Project> findWithDescriptionAndAuthorById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Project> findForAddViewsById(long id);

    Optional<Project> findByAuthor_IdAndName(long userId, String name);

    @EntityGraph(attributePaths = "author")
    Optional<Project> findWithAuthorById(long id);
}
