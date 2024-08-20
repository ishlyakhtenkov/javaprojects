package ru.javaprojects.projector.references.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.util.FileUtil;
import ru.javaprojects.projector.references.TechnologyTo;
import ru.javaprojects.projector.references.model.LogoFile;
import ru.javaprojects.projector.references.model.Technology;
import ru.javaprojects.projector.references.repository.TechnologyRepository;

import java.nio.file.Files;

import static ru.javaprojects.projector.references.TechnologyUtil.createNewFromTo;
import static ru.javaprojects.projector.references.TechnologyUtil.updateFromTo;

@Service
@RequiredArgsConstructor
public class TechnologyService {
    private final TechnologyRepository repository;

    @Value("${content-path.technologies}")
    private String contentPath;

    public Technology get(long id) {
        return repository.getExisted(id);
    }

    public Page<Technology> getAll(Pageable pageable) {
        return repository.findAllByOrderByName(pageable);
    }

    public Page<Technology> getAll(Pageable pageable, String keyword) {
        return repository.findAllByKeyword(keyword, pageable);
    }

    @Transactional
    public Technology create(TechnologyTo technologyTo) {
        Assert.notNull(technologyTo, "technologyTo must not be null");
        if (technologyTo.getLogoFile() == null) {
            throw new IllegalRequestDataException("Technology logo file is not present",
                    "technology.logo-not-present", null);
        }
        Technology technology = repository.saveAndFlush(createNewFromTo(technologyTo, contentPath));
        String fileName = technologyTo.getLogoFile().getOriginalFilename();
        FileUtil.upload(technologyTo.getLogoFile(), contentPath + technology.getName().toLowerCase() + "/", fileName);
        return technology;
    }

    @Transactional
    public void update(TechnologyTo technologyTo) {
        Assert.notNull(technologyTo, "technologyTo must not be null");
        Technology technology = get(technologyTo.getId());
        String oldName = technology.getName();
        String oldLogoFileLink = technology.getLogoFile().getFileLink();
        repository.saveAndFlush(updateFromTo(technology, technologyTo, contentPath));
        if (technologyTo.getLogoFile() != null) {
            FileUtil.deleteDir(contentPath + oldName);
            String newLogoFileName = technologyTo.getLogoFile().getOriginalFilename();
            FileUtil.upload(technologyTo.getLogoFile(), contentPath + technologyTo.getName().toLowerCase() +
                    "/", newLogoFileName);
        } else if (!technologyTo.getName().equalsIgnoreCase(oldName)) {
            FileUtil.moveFile(oldLogoFileLink, contentPath + technologyTo.getName().toLowerCase());
        }
    }
}
