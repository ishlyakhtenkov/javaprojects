package ru.javaprojects.projector.references.technologies;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import java.util.Objects;

@Component
@AllArgsConstructor
public class UniqueTechnologyNameValidator implements org.springframework.validation.Validator {
    public static final String DUPLICATE_ERROR_CODE = "Duplicate";

    private final TechnologyRepository repository;
    private final MessageSource messageSource;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return TechnologyTo.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        TechnologyTo technologyTo = ((TechnologyTo) target);
        if (StringUtils.hasText(technologyTo.getName())) {
            repository.findByNameIgnoreCase(technologyTo.getName())
                    .ifPresent(dbTechnology -> {
                        if (technologyTo.isNew() || !Objects.equals(technologyTo.getId(), dbTechnology.getId())) {
                            errors.rejectValue("name", DUPLICATE_ERROR_CODE,
                                    messageSource.getMessage("duplicate.technology-name", null, LocaleContextHolder.getLocale()));
                        }
                    });
        }
    }
}
