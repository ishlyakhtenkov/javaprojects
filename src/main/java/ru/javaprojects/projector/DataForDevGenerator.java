package ru.javaprojects.projector;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.model.Tag;
import ru.javaprojects.projector.projects.repository.ProjectRepository;
import ru.javaprojects.projector.projects.repository.TagRepository;
import ru.javaprojects.projector.users.model.Role;
import ru.javaprojects.projector.users.model.User;
import ru.javaprojects.projector.users.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Component
@Profile({"dev", "!test"})
@AllArgsConstructor
public class DataForDevGenerator {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @EventListener
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Project dbProject = projectRepository.findWithAllInformationAndDescriptionById(100017).orElseThrow();
        for (int i = 0; i < 100; i++) {
            Project project = new Project(null, dbProject.getName() + "-" + i, dbProject.getAnnotation(),
                    dbProject.isVisible(), dbProject.getPriority(), dbProject.getStarted(), dbProject.getFinished(),
                    dbProject.getArchitecture(), new File(dbProject.getLogo().getFileName(), dbProject.getLogo().getFileLink()),
                    null, new File(dbProject.getPreview().getFileName(), dbProject.getPreview().getFileLink()), null,
                    null, null, null, 0, dbProject.getAuthor(), new HashSet<>(dbProject.getTechnologies()),
                    new HashSet<>(dbProject.getTags()));
            projectRepository.save(project);
        }

        for (int i = 0; i < 50; i++) {
            User user = new User(null, "testUser" + i + "@gmail.com", "Jack Robert " + i, "password", true, Set.of(Role.USER));
            userRepository.save(user);
        }

        for (int i = 0; i < 50; i++) {
            tagRepository.save(new Tag(null, "spring-" + i));
        }
    }
}
