package ru.javaprojects.projector.reference.architectures;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.translate.Translator;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.reference.architectures.model.Architecture;
import ru.javaprojects.projector.reference.architectures.model.LocalizedFields;

import java.util.List;
import java.util.Set;
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

    @Cacheable("architectures")
    public List<Architecture> getAll() {
        return repository.findAllByOrderByName();
    }

    public Architecture get(long id) {
        return repository.getExisted(id);
    }

    public Architecture getWithLocalizedFields(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Not found architecture with id =" + id,
                "error.notfound.entity", new Object[]{id}));
    }

    public Architecture getByName(String name) {
        Assert.notNull(name, "name must not be null");
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Not found architecture with name =" + name,
                        "error.notfound.architecture",  new Object[]{name}));
    }

    @CacheEvict(value = "architectures", allEntries = true)
    public Architecture create(ArchitectureTo architectureTo) {
        Assert.notNull(architectureTo, "architectureTo must not be null");
        if (architectureTo.getLogo() == null || architectureTo.getLogo().isEmpty()) {
            throw new IllegalRequestDataException("Architecture logo file is not present",
                    "architecture.logo-not-present", null);
        }
        Set<LocalizedFields> localizedFields = supportedLocales.stream()
                .map(locale -> new LocalizedFields(locale, translator.translate(architectureTo.getName(), locale),
                        translator.translate(architectureTo.getDescription(), locale)))
                .collect(Collectors.toSet());
        Architecture architecture =
                repository.saveAndFlush(createNewFromTo(architectureTo, localizedFields, architectureFilesPath));
        FileUtil.upload(architectureTo.getLogo(),
                architectureFilesPath + FileUtil.normalizePath(architecture.getName() + "/"),
                FileUtil.normalizePath(architectureTo.getLogo().getRealFileName()));
        return architecture;
    }

    @CacheEvict(value = "architectures", allEntries = true)
    @Transactional
    public void update(ArchitectureTo architectureTo) {
        Assert.notNull(architectureTo, "architectureTo must not be null");
        Architecture architecture = get(architectureTo.getId());
        String oldName = architecture.getName();
        String oldLogoFileLink = architecture.getLogo().getFileLink();
        Set<LocalizedFields> localizedFields = supportedLocales.stream()
                .map(locale -> new LocalizedFields(locale, translator.translate(architectureTo.getName(), locale),
                        translator.translate(architectureTo.getDescription(), locale)))
                .collect(Collectors.toSet());
        repository.saveAndFlush(updateFromTo(architecture, architectureTo, localizedFields, architectureFilesPath));
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
    }

    @CacheEvict(value = "architectures", allEntries = true)
    @Transactional
    public void delete(long id) {
        Architecture architecture = get(id);
        repository.delete(architecture);
        repository.flush();
        FileUtil.deleteFile(architecture.getLogo().getFileLink());
    }
}
