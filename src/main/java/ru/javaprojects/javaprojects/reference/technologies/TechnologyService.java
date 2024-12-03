package ru.javaprojects.javaprojects.reference.technologies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.javaprojects.javaprojects.reference.ReferenceService;
import ru.javaprojects.javaprojects.reference.technologies.model.Technology;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TechnologyService extends ReferenceService<Technology, TechnologyTo> {
    public TechnologyService(TechnologyRepository technologyRepository,
                             @Value("${content-path.technologies}") String technologyFilesPath) {
        super(technologyRepository, technologyFilesPath, Technology.class.getSimpleName());
    }

    @Cacheable("technologies")
    public List<Technology> getAll() {
        return repository.findAll(Sort.by("name"));
    }

    public Page<Technology> getAll(Pageable pageable) {
        Assert.notNull(pageable, "pageable must not be null");
        return repository.findAll(pageable);
    }

    public Page<Technology> getAllByKeyword(String keyword, Pageable pageable) {
        Assert.notNull(keyword, "keyword must not be null");
        Assert.notNull(pageable, "pageable must not be null");
        return ((TechnologyRepository) repository).findAllByKeyword(keyword, pageable);
    }

    public Set<Technology> getAllByIds(Set<Long> ids) {
        Assert.notNull(ids, "ids must not be null");
        return new HashSet<>(repository.findAllById(ids));
    }

    @Override
    @CacheEvict(value = "technologies", allEntries = true)
    public Technology create(TechnologyTo technologyTo) {
        return super.create(technologyTo);
    }

    @Override
    protected Technology createNewFromTo(TechnologyTo technologyTo) {
        return TechnologyUtil.createNewFromTo(technologyTo, filesPath);
    }

    @Override
    @CacheEvict(value = "technologies", allEntries = true)
    public Technology update(TechnologyTo technologyTo) {
        return super.update(technologyTo);
    }

    @Override
    protected void updateSpecificFields(Technology technology, TechnologyTo technologyTo) {
        technology.setUrl(technologyTo.getUrl());
        technology.setUsage(technologyTo.getUsage());
        technology.setPriority(technologyTo.getPriority());
    }

    @Override
    @CacheEvict(value = "technologies", allEntries = true)
    public void delete(long id) {
        super.delete(id);
    }
}
