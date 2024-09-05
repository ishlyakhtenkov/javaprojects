package ru.javaprojects.projector.projects;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.BaseTo;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.util.validation.ImageFile;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.projects.model.ElementType;

@Getter
@Setter
@NoArgsConstructor
public class DescriptionElementTo extends BaseTo implements HasId, Comparable<DescriptionElementTo> {

    @NotNull
    private ElementType type;

    @NotNull
    @PositiveOrZero
    private Byte index;

    @Nullable
    @NoHtml
    @Size(max = 1024)
    private String text;

    @Null
    @NoHtml
    @Size(max = 128)
    private String fileName;

    @Null
    @NoHtml
    @Size(max = 512)
    private String fileLink;

    @Nullable
    @ImageFile
    private MultipartFile imageFile;

    public DescriptionElementTo(Long id, ElementType type, Byte index, String text, String fileName, String fileLink) {
        super(id);
        this.type = type;
        this.index = index;
        this.text = text;
        this.fileName = fileName;
        this.fileLink = fileLink;
    }

    @Override
    public int compareTo(DescriptionElementTo o) {
        return Integer.compare(this.index, o.index);
    }

    @Override
    public String toString() {
        return String.format("DescriptionElementTo[id=%d, type=%s]", id, type);
    }
}
