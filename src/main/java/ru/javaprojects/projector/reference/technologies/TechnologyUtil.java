package ru.javaprojects.projector.reference.technologies;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.AppUtil;
import ru.javaprojects.projector.reference.technologies.model.Technology;

@UtilityClass
public class TechnologyUtil {
    public static Technology createNewFromTo(TechnologyTo technologyTo, String technologyFilesPath) {
        return new Technology(null, technologyTo.getName(), technologyTo.getUrl(), technologyTo.getUsage(),
                technologyTo.getPriority(),
                AppUtil.createFile(technologyTo::getLogo, technologyFilesPath, technologyTo.getName()));
    }

    public static TechnologyTo asTo(Technology technology) {
        FileTo logo = technology.getLogo() == null ? null : AppUtil.asFileTo(technology.getLogo());
        return new TechnologyTo(technology.getId(), technology.getName(), technology.getUrl(), technology.getUsage(),
                technology.getPriority(), logo);
    }
}
