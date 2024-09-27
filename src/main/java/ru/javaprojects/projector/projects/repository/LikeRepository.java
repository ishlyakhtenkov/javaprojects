package ru.javaprojects.projector.projects.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.BaseRepository;
import ru.javaprojects.projector.projects.model.Like;

import java.util.Optional;

@Transactional(readOnly = true)
public interface LikeRepository extends BaseRepository<Like> {

    Optional<Like> findByProjectIdAndUserId(long projectId, long userId);
}
