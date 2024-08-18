package ru.javaprojects.projector.references.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.javaprojects.projector.references.model.Technology;
import ru.javaprojects.projector.references.repository.TechnologyRepository;

@Service
@AllArgsConstructor
public class TechnologyService {
    private final TechnologyRepository repository;

    public Page<Technology> getAll(Pageable pageable) {
        return repository.findAllByOrderByName(pageable);
    }

    public Page<Technology> getAll(Pageable pageable, String keyword) {
        return repository.findAllByKeyword(keyword, pageable);
    }
}
