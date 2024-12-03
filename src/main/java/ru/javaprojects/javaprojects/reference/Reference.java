package ru.javaprojects.javaprojects.reference;

import ru.javaprojects.javaprojects.common.model.File;

public interface Reference {
    String getName();

    File getLogo();

    void setName(String name);

    void setLogo(File logo);
}
