package ru.javaprojects.projector.reference.architectures;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.common.util.AppUtil;
import ru.javaprojects.projector.reference.architectures.model.Architecture;
import ru.javaprojects.projector.reference.architectures.model.LocalizedFields;

import java.util.Set;

import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;

@UtilityClass
public class ArchitectureUtil {
    public static Architecture createNewFromTo(ArchitectureTo architectureTo, Set<LocalizedFields> localizedFields,
                                               String architectureFilesPath) {
        return new Architecture(null, architectureTo.getName(), architectureTo.getDescription(),
                AppUtil.createFile(architectureTo::getLogo, architectureFilesPath, architectureTo.getName() + "/"),
                localizedFields);
    }

    public static ArchitectureTo asTo(Architecture architecture) {
        return new ArchitectureTo(architecture.getId(), architecture.getName(), architecture.getDescription(),
                architecture.getLogo().getFileName(), architecture.getLogo().getFileLink());
    }

    public static Architecture updateFromTo(Architecture architecture, ArchitectureTo architectureTo,
                                            Set<LocalizedFields> localizedFields, String architectureFilesPath) {
        String architectureOldName = architecture.getName();
        architecture.setName(architectureTo.getName());
        architecture.setDescription(architectureTo.getDescription());
        if (architectureTo.getLogo() != null && !architectureTo.getLogo().isEmpty()) {
            architecture.setLogo(AppUtil.createFile(architectureTo::getLogo, architectureFilesPath,
                    architectureTo.getName() + "/"));
        } else if (!architecture.getName().equalsIgnoreCase(architectureOldName)) {
            architecture.getLogo().setFileLink(architectureFilesPath + normalizePath(architecture.getName() + "/" +
                    architecture.getLogo().getFileName()));
        }
        architecture.setLocalizedFields(localizedFields);
        return architecture;
    }
}
