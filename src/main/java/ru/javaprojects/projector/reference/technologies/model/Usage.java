package ru.javaprojects.projector.reference.technologies.model;

import org.springframework.util.StringUtils;

public enum Usage {
    BACKEND, FRONTEND;

    @Override
    public String toString() {
        return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
    }
}
