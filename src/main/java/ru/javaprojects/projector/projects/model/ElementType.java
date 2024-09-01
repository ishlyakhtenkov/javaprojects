package ru.javaprojects.projector.projects.model;

import org.springframework.util.StringUtils;

public enum ElementType {
    TITLE, PARAGRAPH, IMAGE;

    @Override
    public String toString() {
        return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
    }
}
