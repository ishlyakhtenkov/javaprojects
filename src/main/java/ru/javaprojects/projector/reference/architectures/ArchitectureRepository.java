package ru.javaprojects.projector.reference.architectures;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.NamedRepository;
import ru.javaprojects.projector.reference.architectures.model.Architecture;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ArchitectureRepository extends NamedRepository<Architecture> {

    @EntityGraph(attributePaths = "localizedFields")
    List<Architecture> findAllByOrderByName();

    @EntityGraph(attributePaths = "localizedFields")
    Optional<Architecture> findByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = "localizedFields")
    Optional<Architecture> findById(long id);
}
