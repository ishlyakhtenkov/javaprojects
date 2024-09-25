package ru.javaprojects.projector.reference.architectures;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.to.FileTo;

import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;

@UtilityClass
public class ArchitectureUtil {
    public static Architecture createNewFromTo(ArchitectureTo architectureTo, String contentPath) {
        return new Architecture(null, architectureTo.getName(), architectureTo.getDescription(),
                createLogoFile(architectureTo, contentPath));
    }

    public static ArchitectureTo asTo(Architecture architecture) {
        return new ArchitectureTo(architecture.getId(), architecture.getName(), architecture.getDescription(),
                architecture.getLogo().getFileName(), architecture.getLogo().getFileLink());
    }

    public static Architecture updateFromTo(Architecture architecture, ArchitectureTo architectureTo, String contentPath) {
        String architectureOldName = architecture.getName();
        architecture.setName(architectureTo.getName());
        architecture.setDescription(architectureTo.getDescription());
        if (!architectureTo.getLogo().isEmpty()) {
            architecture.setLogo(createLogoFile(architectureTo, contentPath));
        } else if (!architecture.getName().equalsIgnoreCase(architectureOldName)) {
            architecture.getLogo().setFileLink(contentPath + normalizePath(architecture.getName() + "/" +
                    architecture.getLogo().getFileName()));
        }
        return architecture;
    }

    private File createLogoFile(ArchitectureTo architectureTo, String contentPath) {
        FileTo logo = architectureTo.getLogo();
        Assert.notNull(logo, "logo must not be null");
        String filename = normalizePath(logo.getInputtedFile() != null ?
                logo.getInputtedFile().getOriginalFilename() : logo.getFileName());
        return new File(filename, contentPath + normalizePath(architectureTo.getName() + "/" + filename));
    }
}
