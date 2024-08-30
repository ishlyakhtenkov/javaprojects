package ru.javaprojects.projector.common.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

public class YamlFileValidator implements ConstraintValidator<YamlFile, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext ctx) {
        return file == null || file.isEmpty() || Objects.requireNonNull(file.getOriginalFilename()).endsWith(".yml") ||
                Objects.requireNonNull(file.getOriginalFilename()).endsWith(".yaml");
    }
}
