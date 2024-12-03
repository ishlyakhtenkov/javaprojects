package ru.javaprojects.javaprojects.projects.to;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.javaprojects.common.to.BaseTo;
import ru.javaprojects.javaprojects.common.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class CommentTo extends BaseTo {
    @NotNull
    private Long projectId;

    @Nullable
    private Long parentId;

    @NotBlank(message = "{validation.comment.text.NotBlank}")
    @NoHtml(message = "{validation.comment.text.NoHtml}")
    @Size(max = 4096, message = "{validation.comment.text.Size}")
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
