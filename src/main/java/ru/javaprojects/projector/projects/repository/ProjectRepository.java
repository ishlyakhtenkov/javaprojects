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

    @Query("SELECT p.id FROM Project p")
    Page<Long> findAllIds(Pageable pageable);

    @Query("SELECT p.id FROM Project p WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :keyword, '%'))")
    Page<Long> findAllIdsByKeyword(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"architecture", "author", "likes"})
    List<Project> findAllWithArchitectureAndAuthorAndLikesByIdInOrderByName(List<Long> projectsIds);

    @EntityGraph(attributePaths = {"architecture", "author", "likes", "technologies"})
    List<Project> findAllWithArchitectureAndAuthorAndTechnologiesAndLikesByIdIn(List<Long> projectsIds);

    @Query("SELECT p.id FROM Project p WHERE p.visible = TRUE")
    Page<Long> findAllIdsByVisibleIsTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"architecture", "author", "likes", "technologies"})
    List<Project> findAllWithArchitectureAndAuthorAndTechnologiesAndLikesByIdInOrderByCreatedDesc(List<Long> projectsIds);

    @Query("SELECT p.id FROM Project p LEFT JOIN p.likes l LEFT JOIN p.comments c WHERE p.visible = TRUE GROUP BY p.id " +
            "ORDER BY (COUNT(DISTINCT l.id) + COUNT(DISTINCT c.id)) DESC")
    Page<Long> findAllIdsOrderByPopularity(Pageable pageable);

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author"})
    List<Project> findAllWithAllInformationByAuthor_IdAndVisibleIsTrue(long userId);

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author"})
    List<Project> findAllWithAllInformationByAuthor_Id(long userId);

    @EntityGraph(attributePaths = {"architecture", "technologies", "likes", "author", "descriptionElements", "tags"})
    Optional<Project> findWithAllInformationAndDescriptionById(long id);

    @EntityGraph(attributePaths = {"author", "descriptionElements"})
    Optional<Project> findWithDescriptionAndAuthorById(long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Project> findForAddViewsById(long id);

    Optional<Project> findByAuthor_IdAndName(long userId, String name);

    @EntityGraph(attributePaths = "author")
    Optional<Project> findWithAuthorById(long id);

    @Query("SELECT p.id FROM Project p LEFT JOIN p.tags t WHERE p.visible = TRUE AND UPPER(t.name) = UPPER(:tag)")
    Page<Long> findAllIdsByTag(String tag, Pageable pageable);
}
