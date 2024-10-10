package ru.javaprojects.projector.reference.architectures;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.common.util.Util;

import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;

@UtilityClass
public class ArchitectureUtil {
    public static Architecture createNewFromTo(ArchitectureTo architectureTo, String architectureFilesPath) {
        return new Architecture(null, architectureTo.getName(), architectureTo.getDescription(),
                Util.createFile(architectureTo::getLogo, architectureFilesPath, architectureTo.getName() + "/"));
    }

    public static ArchitectureTo asTo(Architecture architecture) {
        return new ArchitectureTo(architecture.getId(), architecture.getName(), architecture.getDescription(),
                architecture.getLogo().getFileName(), architecture.getLogo().getFileLink());
    }

    public static Architecture updateFromTo(Architecture architecture, ArchitectureTo architectureTo,
                                            String architectureFilesPath) {
        String architectureOldName = architecture.getName();
        architecture.setName(architectureTo.getName());
        architecture.setDescription(architectureTo.getDescription());
        if (architectureTo.getLogo() != null && !architectureTo.getLogo().isEmpty()) {
            architecture.setLogo(Util.createFile(architectureTo::getLogo, architectureFilesPath, architectureTo.getName() + "/"));
        } else if (!architecture.getName().equalsIgnoreCase(architectureOldName)) {
            architecture.getLogo().setFileLink(architectureFilesPath + normalizePath(architecture.getName() + "/" +
                    architecture.getLogo().getFileName()));
        }
        return architecture;
    }
}
