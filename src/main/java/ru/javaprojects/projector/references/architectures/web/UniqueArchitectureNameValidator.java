package ru.javaprojects.projector.references.architectures.web;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.common.util.validation.UniqueNameValidator;
import ru.javaprojects.projector.references.architectures.Architecture;
import ru.javaprojects.projector.references.architectures.ArchitectureRepository;

@Component
public class UniqueArchitectureNameValidator extends UniqueNameValidator<Architecture, ArchitectureRepository> {
    public UniqueArchitectureNameValidator(ArchitectureRepository repository, MessageSource messageSource) {
        super(repository, messageSource, "duplicate.architecture-name");
    }
}
