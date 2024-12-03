package ru.javaprojects.javaprojects.projects.web;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.javaprojects.javaprojects.app.AuthUser;
import ru.javaprojects.javaprojects.projects.repository.ProjectRepository;
import ru.javaprojects.javaprojects.projects.to.ProjectTo;

import java.util.Objects;

@Component
@AllArgsConstructor
public class UniqueProjectNameValidator implements org.springframework.validation.Validator {
    public static final String DUPLICATE_ERROR_CODE = "Duplicate";

    private final ProjectRepository repository;
    private final MessageSource messageSource;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ProjectTo.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        ProjectTo project = ((ProjectTo) target);
        if (StringUtils.hasText(project.getName())) {
            repository.findByAuthor_IdAndName(AuthUser.authId(), project.getName())
                    .ifPresent(dbProject -> {
                        if (project.isNew() || !Objects.equals(project.getId(), dbProject.getId())) {
                            errors.rejectValue("name", DUPLICATE_ERROR_CODE,
                                    messageSource.getMessage("error.duplicate.project-name", null,
                                            LocaleContextHolder.getLocale()));
                        }
                    });
        }
    }
}
