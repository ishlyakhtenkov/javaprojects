package ru.javaprojects.projector.references.technologies;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.common.model.LogoFile;
import ru.javaprojects.projector.references.technologies.model.Technology;

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
        technology.setUrl(technologyTo.getUrl());
        technology.setUsage(technologyTo.getUsage());
        technology.setPriority(technologyTo.getPriority());
        if (technologyTo.getLogoFile() != null && !technologyTo.getLogoFile().isEmpty()) {
            technology.setLogoFile(createLogoFile(technologyTo, contentPath));
        } else if (!technologyTo.getName().equalsIgnoreCase(technology.getName())) {
            technology.getLogoFile().setFileLink(contentPath + normalizePath(technologyTo.getName()) + "/" +
                    technology.getLogoFile().getFileName());
        }
        technology.setName(technologyTo.getName());
        return technology;
    }

    private LogoFile createLogoFile(TechnologyTo technologyTo, String contentPath) {
        String filename = normalizePath(technologyTo.getLogoFile().getOriginalFilename());
        return new LogoFile(filename, contentPath + normalizePath(technologyTo.getName()) + "/" + filename);
    }
}
