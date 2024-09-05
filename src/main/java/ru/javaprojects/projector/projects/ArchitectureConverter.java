package ru.javaprojects.projector.projects;

import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.references.architectures.Architecture;
import ru.javaprojects.projector.references.architectures.ArchitectureRepository;

@Component
@AllArgsConstructor
public class ArchitectureConverter implements Converter<String, Architecture> {
    private final ArchitectureRepository repository;

    @Override
    public Architecture convert(String id) {
        return id.isBlank() ? null : repository.getReferenceById(Long.parseLong(id));
    }
}