package ru.javaprojects.projector.reference;

import ru.javaprojects.projector.common.model.File;

public interface Reference {
    String getName();

    File getLogo();

    void setName(String name);

    void setLogo(File logo);
}
