package ru.javaprojects.projector.projects;

import ru.javaprojects.projector.reference.architectures.Architecture;

public interface HasArchitecture {
    Architecture getArchitecture();

    void setArchitecture(Architecture architecture);
}
