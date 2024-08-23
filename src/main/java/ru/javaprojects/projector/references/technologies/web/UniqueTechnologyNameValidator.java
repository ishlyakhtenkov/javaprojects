package ru.javaprojects.projector.references.technologies.web;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.common.util.validation.UniqueNameValidator;
import ru.javaprojects.projector.references.technologies.TechnologyRepository;
import ru.javaprojects.projector.references.technologies.model.Technology;

@Component
public class UniqueTechnologyNameValidator extends UniqueNameValidator<Technology, TechnologyRepository> {
    public UniqueTechnologyNameValidator(TechnologyRepository repository, MessageSource messageSource) {
        super(repository, messageSource, "duplicate.technology-name");
    }
}
