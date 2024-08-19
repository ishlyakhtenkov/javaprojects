package ru.javaprojects.projector.references;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.references.model.ImageFile;
import ru.javaprojects.projector.references.model.Technology;

@UtilityClass
public class TechnologyUtil {
    public static Technology createNewFromTo(TechnologyTo technologyTo) {
        String filename = technologyTo.getImageFile().getOriginalFilename();
        return new Technology(null, technologyTo.getName(), technologyTo.getUrl(), technologyTo.getUsage(),
                new ImageFile(filename, "content/technologies/" + technologyTo.getName() + "/" + filename));
    }
}
