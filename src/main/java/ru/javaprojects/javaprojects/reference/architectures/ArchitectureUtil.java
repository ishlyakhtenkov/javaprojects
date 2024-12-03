package ru.javaprojects.javaprojects.reference.architectures;

import lombok.experimental.UtilityClass;
import ru.javaprojects.javaprojects.common.to.FileTo;
import ru.javaprojects.javaprojects.common.util.AppUtil;

@UtilityClass
public class ArchitectureUtil {
    public static Architecture createNewFromTo(ArchitectureTo architectureTo, String architectureFilesPath) {
        return new Architecture(null, architectureTo.getName(), architectureTo.getDescription(),
                AppUtil.createFile(architectureTo::getLogo, architectureFilesPath, architectureTo.getName()));
    }

    public static ArchitectureTo asTo(Architecture architecture) {
        FileTo logo = architecture.getLogo() == null ? null : AppUtil.asFileTo(architecture.getLogo());
        return new ArchitectureTo(architecture.getId(), architecture.getName(), architecture.getDescription(), logo);
    }
}
