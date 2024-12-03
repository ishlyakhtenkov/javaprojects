package ru.javaprojects.javaprojects.projects.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.javaprojects.common.repository.BaseRepository;
import ru.javaprojects.javaprojects.projects.model.Like;

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

    @Query("""
              SELECT SUM(total) FROM 
              (SELECT COUNT(DISTINCT l.id) as total FROM Like l JOIN Project p ON l.objectId = p.id 
              WHERE l.objectType = ru.javaprojects.javaprojects.projects.model.ObjectType.PROJECT AND p.author.id =:userId 
              UNION ALL 
              SELECT COUNT(DISTINCT l.id) as total FROM Like l JOIN Comment c ON l.objectId = c.id 
              WHERE l.objectType = ru.javaprojects.javaprojects.projects.model.ObjectType.COMMENT AND c.author.id =:userId) q""")
    int countLikesForAuthor(long userId);
}
