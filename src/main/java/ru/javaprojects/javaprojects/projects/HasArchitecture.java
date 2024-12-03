package ru.javaprojects.javaprojects.projects;

import ru.javaprojects.javaprojects.reference.architectures.Architecture;

public interface HasArchitecture {
    Architecture getArchitecture();

    void setArchitecture(Architecture architecture);
}
