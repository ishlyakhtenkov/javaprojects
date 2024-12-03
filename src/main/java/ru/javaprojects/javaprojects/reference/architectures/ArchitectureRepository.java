package ru.javaprojects.javaprojects.reference.architectures;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.javaprojects.reference.ReferenceRepository;

@Transactional(readOnly = true)
public interface ArchitectureRepository extends ReferenceRepository<Architecture> {}
