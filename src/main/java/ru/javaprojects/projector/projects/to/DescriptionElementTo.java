package ru.javaprojects.projector.projects.to;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.HasId;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.projects.model.ElementType;

@Getter
@Setter
@NoArgsConstructor
public class DescriptionElementTo extends BaseTo implements Comparable<DescriptionElementTo> {

    @NotNull
    private ElementType type;

    @NotNull
    @PositiveOrZero
    private Byte index;

    @Nullable //TODO validate not empty when not image
    @NoHtml
    @Size(max = 1024)
    private String text;

    @Nullable
    private FileTo image;

    public DescriptionElementTo(Long id, ElementType type, Byte index, String text, String imageFileName,
                                String imageFileLink) {
        super(id);
        this.type = type;
        this.index = index;
        this.text = text;
        this.image = new FileTo(imageFileName, imageFileLink, null, null);
    }

    public DescriptionElementTo(Long id, ElementType type, Byte index, MultipartFile imageMultipartFile) {
        super(id);
        this.type = type;
        this.index = index;
        this.image = new FileTo(null, null, imageMultipartFile, null);
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
