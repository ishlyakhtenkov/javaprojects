package ru.javaprojects.projector.reference;

import org.springframework.data.repository.NoRepositoryBean;
import ru.javaprojects.projector.common.repository.NamedRepository;

@NoRepositoryBean
public interface ReferenceRepository<T extends Reference> extends NamedRepository<T> {
}
