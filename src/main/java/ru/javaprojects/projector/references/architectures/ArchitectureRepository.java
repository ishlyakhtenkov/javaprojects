package ru.javaprojects.projector.references.architectures;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.NamedRepository;

import java.util.List;

@Transactional(readOnly = true)
public interface ArchitectureRepository extends NamedRepository<Architecture> {

    List<Architecture> findAllByOrderByName();
}
