package ru.javaprojects.projector.reference.architectures;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.error.NotFoundException;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.reference.technologies.model.Technology;

import java.util.List;

import static ru.javaprojects.projector.common.util.FileUtil.isFileToEmpty;
import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;
import static ru.javaprojects.projector.reference.architectures.ArchitectureUtil.createNewFromTo;
import static ru.javaprojects.projector.reference.architectures.ArchitectureUtil.updateFromTo;


@Service
@RequiredArgsConstructor
public class ArchitectureService {
    private final ArchitectureRepository repository;

    @Value("${content-path.architectures}")
    private String contentPath;

    public List<Architecture> getAll() {
        return repository.findAllByOrderByName();
    }

    public Architecture get(long id) {
        return repository.getExisted(id);
    }

    public Architecture getByName(String name) {
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Not found architecture with name =" + name, "notfound.architecture",
                        new Object[]{name}));
    }

    public Architecture create(ArchitectureTo architectureTo) {
        Assert.notNull(architectureTo, "architectureTo must not be null");
        if (architectureTo.getLogo() == null || isFileToEmpty(architectureTo.getLogo())) {
            throw new IllegalRequestDataException("Architecture logo file is not present",
                    "architecture.logo-not-present", null);
        }
        Architecture architecture = repository.saveAndFlush(createNewFromTo(architectureTo, contentPath));
        uploadImage(architectureTo, architecture.getName());
        return architecture;
    }

    @Transactional
    public void update(ArchitectureTo architectureTo) {
        Assert.notNull(architectureTo, "architectureTo must not be null");
        Architecture architecture = get(architectureTo.getId());
        String oldName = architecture.getName();
        String oldLogoFileLink = architecture.getLogo().getFileLink();
        repository.saveAndFlush(updateFromTo(architecture, architectureTo, contentPath));
        if (!isFileToEmpty(architectureTo.getLogo())) {
            uploadImage(architectureTo, architecture.getName());
            if (!oldLogoFileLink.equalsIgnoreCase(architecture.getLogo().getFileLink())) {
                FileUtil.deleteFile(oldLogoFileLink);
            }
        } else if (!architecture.getName().equalsIgnoreCase(oldName)) {
            FileUtil.moveFile(oldLogoFileLink, contentPath + FileUtil.normalizePath(architecture.getName()));
        }
    }

    private void uploadImage(ArchitectureTo architectureTo, String architectureName) {
        FileTo logo = architectureTo.getLogo();
        String fileName = normalizePath(logo.getInputtedFile() != null ? logo.getInputtedFile().getOriginalFilename() :
                logo.getFileName());
        FileUtil.upload(logo, contentPath + FileUtil.normalizePath(architectureName) + "/", fileName);
    }

    @Transactional
    public void delete(long id) {
        Architecture architecture = get(id);
        repository.delete(architecture);
        repository.flush();
        FileUtil.deleteFile(architecture.getLogo().getFileLink());
    }
}
