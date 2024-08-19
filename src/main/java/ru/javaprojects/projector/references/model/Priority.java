package ru.javaprojects.projector.references.model;

import org.springframework.util.StringUtils;

public enum Priority {
    VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH, ULTRA;

    @Override
    public String toString() {
        return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
    }

}
