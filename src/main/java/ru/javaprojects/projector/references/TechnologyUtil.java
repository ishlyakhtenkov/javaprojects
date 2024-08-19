package ru.javaprojects.projector.references;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.references.model.ImageFile;
import ru.javaprojects.projector.references.model.Technology;

@UtilityClass
public class TechnologyUtil {
    public static Technology createNewFromTo(TechnologyTo technologyTo, String contentPath) {
        String filename = technologyTo.getImageFile().getOriginalFilename();
        return new Technology(null, technologyTo.getName(), technologyTo.getUrl(), technologyTo.getUsage(),
                technologyTo.getPriority(),
                new ImageFile(filename, contentPath + technologyTo.getName().toLowerCase() + "/" + filename));
    }
}
