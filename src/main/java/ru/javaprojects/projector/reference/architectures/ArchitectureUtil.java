package ru.javaprojects.projector.reference.architectures;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.AppUtil;

import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;

@UtilityClass
public class ArchitectureUtil {
    public static Architecture createNewFromTo(ArchitectureTo architectureTo, String architectureFilesPath) {
        return new Architecture(null, architectureTo.getName(), architectureTo.getDescription(),
                AppUtil.createFile(architectureTo::getLogo, architectureFilesPath, architectureTo.getName() + "/"));
    }

    public static ArchitectureTo asTo(Architecture architecture) {
        String logoFileName = architecture.getLogo() != null ? architecture.getLogo().getFileName() : null;
        String logoFileLink = architecture.getLogo() != null ? architecture.getLogo().getFileLink() : null;
        FileTo logo = (logoFileName == null || logoFileLink == null) ? null :
                new FileTo(logoFileName, logoFileLink, null, null);
        return new ArchitectureTo(architecture.getId(), architecture.getName(), architecture.getDescription(), logo);
    }

    public static Architecture updateFromTo(Architecture architecture, ArchitectureTo architectureTo,
                                            String architectureFilesPath) {
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
        return architecture;
    }
}
