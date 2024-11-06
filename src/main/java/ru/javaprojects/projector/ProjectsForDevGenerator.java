package ru.javaprojects.projector;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.projects.model.Project;
import ru.javaprojects.projector.projects.repository.ProjectRepository;

import java.util.HashSet;

@Component
@Profile({"dev", "!test"})
@AllArgsConstructor
public class ProjectsForDevGenerator {
    private final ProjectRepository projectRepository;

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
    }
}
