package ru.javaprojects.projector.reference.architectures;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.projector.common.error.NotFoundException;

import java.util.List;

@Service
@AllArgsConstructor
public class ArchitectureService {
    private final ArchitectureRepository repository;

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

    public Architecture create(Architecture architecture) {
        Assert.notNull(architecture, "architecture must not be null");
        return repository.save(architecture);
    }

    @Transactional // just to make one select by id instead of two by Hibernate
    public void update(Architecture architecture) {
        Assert.notNull(architecture, "architecture must not be null");
        repository.getExisted(architecture.id());
        repository.save(architecture);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
