package ru.javaprojects.javaprojects.reference.technologies.web;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.javaprojects.javaprojects.common.validation.UniqueNameValidator;
import ru.javaprojects.javaprojects.reference.technologies.TechnologyRepository;
import ru.javaprojects.javaprojects.reference.technologies.model.Technology;

@Component
public class UniqueTechnologyNameValidator extends UniqueNameValidator<Technology, TechnologyRepository> {
    public UniqueTechnologyNameValidator(TechnologyRepository repository, MessageSource messageSource) {
        super(repository, messageSource, "error.duplicate.technology-name");
    }
}
