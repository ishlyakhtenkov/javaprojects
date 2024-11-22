package ru.javaprojects.projector.reference.architectures;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.translate.TranslateException;
import ru.javaprojects.projector.common.translate.Translator;
import ru.javaprojects.projector.reference.ReferenceService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
public class ArchitectureService extends ReferenceService<Architecture, ArchitectureTo> {
    private static final String NATIVE = "native";
    private final Translator translator;
    private final List<String> supportedLocales;
    private Map<Long, Map<String, Architecture>> architectures;

    public ArchitectureService(ArchitectureRepository repository, Translator translator,
                               @Value("${content-path.architectures}") String architectureFilesPath,
                               @Value("${locale.supported}") List<String> supportedLocales) {
        super(repository, architectureFilesPath, Architecture.class.getSimpleName());
        this.translator = translator;
        this.supportedLocales = supportedLocales;
    }

    @PostConstruct
    private void loadArchitectures() {
        architectures = new ConcurrentHashMap<>();
        repository.findAll().forEach(a -> architectures.put(a.getId(), localizeArchitecture(a)));
    }

    private Map<String, Architecture> localizeArchitecture(Architecture a) {
        Map<String, Architecture> localizedArchitectures = new HashMap<>();
        localizedArchitectures.put(NATIVE, a);
        supportedLocales.forEach(locale -> {
            try {
                String localizedName = translator.translate(a.getName(), locale);
                String localizedDescription = translator.translate(a.getDescription(), locale);
                Architecture localizedArchitecture = new Architecture(a.getId(), localizedName, localizedDescription,
                        a.getLogo());
                localizedArchitectures.put(locale, localizedArchitecture);
            } catch (TranslateException e) {
                log.error(e.getMessage());
            }
        });
        return localizedArchitectures;
    }

    public Architecture getLocalized(long id, String locale) {
        Map<String, Architecture> localizedArchitectures = architectures.get(id);
        if (localizedArchitectures != null) {
            return localizedArchitectures.get(locale.toLowerCase());
        } else {
            throw new NotFoundException("Architecture with id=" + id + " not found",
                    "error.notfound.entity", new Object[]{id});
        }
    }

    public List<Architecture> getAll() {
        List<Architecture> architectureList = new ArrayList<>();
        architectures.forEach((id, localizedArchitectures) -> {
            Architecture localizedArchitecture = localizedArchitectures.get(LocaleContextHolder.getLocale()
                    .getLanguage().toLowerCase());
            architectureList.add(localizedArchitecture != null ? localizedArchitecture : localizedArchitectures.get(NATIVE));
        });
        architectureList.sort(Comparator.comparing(Architecture::getName));
        return architectureList;
    }

    @Override
    public Architecture create(ArchitectureTo architectureTo) {
        Architecture architecture = super.create(architectureTo);
        architectures.put(architecture.getId(), localizeArchitecture(architecture));
        return architecture;
    }

    @Override
    protected Architecture createNewFromTo(ArchitectureTo architectureTo) {
        return ArchitectureUtil.createNewFromTo(architectureTo, filesPath);
    }

    @Override
    public Architecture update(ArchitectureTo referenceTo) {
        Architecture architecture = super.update(referenceTo);
        architectures.put(architecture.getId(), localizeArchitecture(architecture));
        return architecture;
    }

    @Override
    protected void updateSpecificFields(Architecture architecture, ArchitectureTo architectureTo) {
        architecture.setDescription(architectureTo.getDescription());
    }

    @Override
    public void delete(long id) {
        super.delete(id);
        architectures.remove(id);
    }
}
