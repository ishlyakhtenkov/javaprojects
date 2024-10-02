package ru.javaprojects.projector.projects.to;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.util.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class CommentTo extends BaseTo {
    @NotNull
    private Long projectId;

    @Nullable
    private Long parentId;

    @NotBlank
    @NoHtml
    @Size(max = 4096)
    private String text;

    public CommentTo(Long id, Long projectId, Long parentId, String text) {
        super(id);
        this.projectId = projectId;
        this.parentId = parentId;
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("CommentTo[id=%d, projectId=%d, parentId=%d]", id, projectId, parentId);
    }
}
