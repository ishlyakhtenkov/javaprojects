package ru.javaprojects.projector.references.technologies;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.model.LogoFile;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.references.technologies.model.Technology;

import static ru.javaprojects.projector.common.util.FileUtil.isFileToEmpty;
import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;

@UtilityClass
public class TechnologyUtil {
    public static Technology createNewFromTo(TechnologyTo technologyTo, String contentPath) {
        return new Technology(null, technologyTo.getName(), technologyTo.getUrl(), technologyTo.getUsage(),
                technologyTo.getPriority(), createLogoFile(technologyTo, contentPath));
    }

    public static TechnologyTo asTo(Technology technology) {
        return new TechnologyTo(technology.getId(), technology.getName(), technology.getUrl(), technology.getUsage(),
                technology.getPriority(), technology.getLogoFile().getFileName(), technology.getLogoFile().getFileLink());
    }

    public static Technology updateFromTo(Technology technology, TechnologyTo technologyTo, String contentPath) {
        String technologyOldName = technology.getName();
        technology.setName(technologyTo.getName());
        technology.setUrl(technologyTo.getUrl());
        technology.setUsage(technologyTo.getUsage());
        technology.setPriority(technologyTo.getPriority());
        if (!isFileToEmpty(technologyTo.getLogo())) {
            technology.setLogoFile(createLogoFile(technologyTo, contentPath));
        } else if (!technology.getName().equalsIgnoreCase(technologyOldName)) {
            technology.getLogoFile().setFileLink(contentPath + normalizePath(technology.getName() + "/" +
                    technology.getLogoFile().getFileName()));
        }
        return technology;
    }

    private LogoFile createLogoFile(TechnologyTo technologyTo, String contentPath) {
        FileTo logo = technologyTo.getLogo();
        Assert.notNull(logo, "logo must not be null");
        String filename = normalizePath(logo.getInputtedFile() != null ?
                logo.getInputtedFile().getOriginalFilename() : logo.getFileName());
        return new LogoFile(filename, contentPath + normalizePath(technologyTo.getName() + "/" + filename));
    }
}
