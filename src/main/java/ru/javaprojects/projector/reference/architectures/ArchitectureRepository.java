package ru.javaprojects.projector.reference.architectures;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.NamedRepository;

@Transactional(readOnly = true)
public interface ArchitectureRepository extends NamedRepository<Architecture> {}
