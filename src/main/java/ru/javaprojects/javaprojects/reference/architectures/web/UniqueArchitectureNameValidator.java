package ru.javaprojects.javaprojects.reference.architectures.web;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.javaprojects.javaprojects.common.validation.UniqueNameValidator;
import ru.javaprojects.javaprojects.reference.architectures.Architecture;
import ru.javaprojects.javaprojects.reference.architectures.ArchitectureRepository;

@Component
public class UniqueArchitectureNameValidator extends UniqueNameValidator<Architecture, ArchitectureRepository> {
    public UniqueArchitectureNameValidator(ArchitectureRepository repository, MessageSource messageSource) {
        super(repository, messageSource, "error.duplicate.architecture-name");
    }
}
