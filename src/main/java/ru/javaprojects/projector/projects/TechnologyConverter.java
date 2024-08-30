package ru.javaprojects.projector.projects;

import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.references.technologies.TechnologyRepository;
import ru.javaprojects.projector.references.technologies.model.Technology;

@Component
@AllArgsConstructor
public class TechnologyConverter implements Converter<String, Technology> {
    private final TechnologyRepository repository;

    @Override
    public Technology convert(String id) {
        return id.isBlank() ? null : repository.getExisted(Long.parseLong(id));
    }
}
