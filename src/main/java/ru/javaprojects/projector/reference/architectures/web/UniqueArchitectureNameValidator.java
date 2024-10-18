package ru.javaprojects.projector.reference.architectures.web;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.common.validation.UniqueNameValidator;
import ru.javaprojects.projector.reference.architectures.Architecture;
import ru.javaprojects.projector.reference.architectures.ArchitectureRepository;

@Component
public class UniqueArchitectureNameValidator extends UniqueNameValidator<Architecture, ArchitectureRepository> {
    public UniqueArchitectureNameValidator(ArchitectureRepository repository, MessageSource messageSource) {
        super(repository, messageSource, "error.duplicate.architecture-name");
    }
}
