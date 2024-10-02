package ru.javaprojects.projector.projects.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.repository.BaseRepository;
import ru.javaprojects.projector.projects.model.Like;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface LikeRepository extends BaseRepository<Like> {

    Optional<Like> findByObjectIdAndUserId(long objectId, long userId);

    List<Like> findAllByObjectId(long objectId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Like l WHERE l.objectId=:objectId")
    void deleteAllByObjectId(long objectId);
}
