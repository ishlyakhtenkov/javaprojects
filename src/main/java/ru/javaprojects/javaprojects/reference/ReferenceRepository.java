package ru.javaprojects.javaprojects.reference;

import org.springframework.data.repository.NoRepositoryBean;
import ru.javaprojects.javaprojects.common.repository.NamedRepository;

@NoRepositoryBean
public interface ReferenceRepository<T extends Reference> extends NamedRepository<T> {
}
