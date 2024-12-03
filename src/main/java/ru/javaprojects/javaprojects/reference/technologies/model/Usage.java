package ru.javaprojects.javaprojects.reference.technologies.model;

import org.springframework.util.StringUtils;

public enum Usage {
    BACKEND, FRONTEND;

    @Override
    public String toString() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
