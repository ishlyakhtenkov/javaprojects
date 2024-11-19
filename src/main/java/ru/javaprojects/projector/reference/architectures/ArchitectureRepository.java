package ru.javaprojects.projector.reference.architectures;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.NamedRepository;
import ru.javaprojects.projector.reference.ReferenceRepository;

@Transactional(readOnly = true)
public interface ArchitectureRepository extends ReferenceRepository<Architecture> {}
