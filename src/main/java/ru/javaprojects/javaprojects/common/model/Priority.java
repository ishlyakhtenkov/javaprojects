package ru.javaprojects.javaprojects.common.model;

import org.springframework.util.StringUtils;

public enum Priority {
    ULTRA, VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW;

    @Override
    public String toString() {
        return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
    }
}
