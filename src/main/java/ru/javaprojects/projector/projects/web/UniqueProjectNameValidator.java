package ru.javaprojects.projector.projects.web;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.javaprojects.projector.common.validation.UniqueNameValidator;
import ru.javaprojects.projector.projects.repository.ProjectRepository;
import ru.javaprojects.projector.projects.model.Project;

@Component
public class UniqueProjectNameValidator extends UniqueNameValidator<Project, ProjectRepository> {
    public UniqueProjectNameValidator(ProjectRepository repository, MessageSource messageSource) {
        super(repository, messageSource, "duplicate.project-name");
    }
}
