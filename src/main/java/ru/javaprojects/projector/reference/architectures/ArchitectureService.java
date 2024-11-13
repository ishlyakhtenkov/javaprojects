package ru.javaprojects.projector.reference.architectures;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.translate.Translator;
import ru.javaprojects.projector.common.util.FileUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.javaprojects.projector.reference.architectures.ArchitectureUtil.createNewFromTo;
import static ru.javaprojects.projector.reference.architectures.ArchitectureUtil.updateFromTo;


@Service
@RequiredArgsConstructor
public class ArchitectureService {
    private final ArchitectureRepository repository;
    private final Translator translator;

    @Value("${content-path.architectures}")
    private String architectureFilesPath;

    @Value("${locale.supported}")
    private List<String> supportedLocales;

    private Map<Long, Map<String, Architecture>> architectures;

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

    public List<Architecture> getAll() {
        return architectures.values().stream()
                .flatMap(map -> map.entrySet()
                        .stream())
                .filter(e -> e.getKey().equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()))
                .map(Map.Entry::getValue)
                .toList();
    }

    public Architecture get(long id) {
        return repository.getExisted(id);
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

    public Architecture getByName(String name) {
        Assert.notNull(name, "name must not be null");
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Not found architecture with name =" + name,
                        "error.notfound.architecture",  new Object[]{name}));
    }

    public Architecture create(ArchitectureTo architectureTo) {
        Assert.notNull(architectureTo, "architectureTo must not be null");
        if (architectureTo.getLogo() == null || architectureTo.getLogo().isEmpty()) {
            throw new IllegalRequestDataException("Architecture logo file is not present",
                    "architecture.logo-not-present", null);
        }
        Architecture architecture = repository.saveAndFlush(createNewFromTo(architectureTo, architectureFilesPath));
        FileUtil.upload(architectureTo.getLogo(),
                architectureFilesPath + FileUtil.normalizePath(architecture.getName() + "/"),
                FileUtil.normalizePath(architectureTo.getLogo().getRealFileName()));
        architectures.put(architecture.getId(), localizeArchitecture(architecture));
        return architecture;
    }

    @Transactional
    public void update(ArchitectureTo architectureTo) {
        Assert.notNull(architectureTo, "architectureTo must not be null");
        Architecture architecture = get(architectureTo.getId());
        String oldName = architecture.getName();
        String oldLogoFileLink = architecture.getLogo().getFileLink();

        repository.saveAndFlush(updateFromTo(architecture, architectureTo, architectureFilesPath));
        if (architectureTo.getLogo() != null && !architectureTo.getLogo().isEmpty()) {
            FileUtil.upload(architectureTo.getLogo(),
                    architectureFilesPath + FileUtil.normalizePath(architecture.getName() + "/"),
                    FileUtil.normalizePath(architectureTo.getLogo().getRealFileName()));
            if (!oldLogoFileLink.equalsIgnoreCase(architecture.getLogo().getFileLink())) {
                FileUtil.deleteFile(oldLogoFileLink);
            }
        } else if (!architecture.getName().equalsIgnoreCase(oldName)) {
            FileUtil.moveFile(oldLogoFileLink, architectureFilesPath +
                    FileUtil.normalizePath(architecture.getName()));
        }
        architectures.put(architecture.getId(), localizeArchitecture(architecture));
    }

    @Transactional
    public void delete(long id) {
        Architecture architecture = get(id);
        repository.delete(architecture);
        repository.flush();
        FileUtil.deleteFile(architecture.getLogo().getFileLink());
        architectures.remove(architecture.getId());
    }
}
