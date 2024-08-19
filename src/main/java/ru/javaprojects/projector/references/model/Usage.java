package ru.javaprojects.projector.references.model;

import org.springframework.util.StringUtils;

public enum Usage {
    BACKEND, FRONTEND;

    @Override
    public String toString() {
        return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
    }
}
