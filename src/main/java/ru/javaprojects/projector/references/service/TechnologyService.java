package ru.javaprojects.projector.references.service;

import lombok.AllArgsConstructor;
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
import ru.javaprojects.projector.references.model.Technology;
import ru.javaprojects.projector.references.repository.TechnologyRepository;

import static ru.javaprojects.projector.references.TechnologyUtil.createNewFromTo;

@Service
@RequiredArgsConstructor
public class TechnologyService {
    private final TechnologyRepository repository;

    @Value("${content-path.technologies}")
    private String contentPath;

    public Page<Technology> getAll(Pageable pageable) {
        return repository.findAllByOrderByName(pageable);
    }

    public Page<Technology> getAll(Pageable pageable, String keyword) {
        return repository.findAllByKeyword(keyword, pageable);
    }

    @Transactional
    public Technology create(TechnologyTo technologyTo) {
        Assert.notNull(technologyTo, "technologyTo must not be null");
        if (technologyTo.getImageFile() == null) {
            throw new IllegalRequestDataException("Technology image file is not present",
                    "technology.image-not-present", null);
        }
        Technology technology = repository.saveAndFlush(createNewFromTo(technologyTo, contentPath));
        String fileName = technologyTo.getImageFile().getOriginalFilename();
        FileUtil.upload(technologyTo.getImageFile(), contentPath + technology.getName().toLowerCase() + "/", fileName);
        return technology;
    }
}
