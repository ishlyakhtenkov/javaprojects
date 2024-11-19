package ru.javaprojects.projector.reference.architectures;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.translate.Translator;
import ru.javaprojects.projector.reference.ReferenceService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class ArchitectureService extends ReferenceService<Architecture, ArchitectureTo> {
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
        return supportedLocales.stream()
                .collect(Collectors.toMap(Function.identity(), locale ->
                        new Architecture(a.getId(), translator.translate(a.getName(), locale),
                                translator.translate(a.getDescription(), locale), a.getLogo())));
    }

    public Architecture getLocalized(long id, String locale) {
        Map<String, Architecture> localizedArchitectures = architectures.get(id);
        if (localizedArchitectures != null) {
            return localizedArchitectures.get(locale.toLowerCase());
        } else {
            throw new NotFoundException("Localized architecture with id=" + id + " not found",
                    "error.notfound.architecture.localized", new Object[]{id});
        }
    }

    public List<Architecture> getAll() {
        return architectures.values().stream()
                .flatMap(map -> map.entrySet()
                        .stream())
                .filter(e -> e.getKey().equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()))
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparing(Architecture::getName))
                .toList();
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
