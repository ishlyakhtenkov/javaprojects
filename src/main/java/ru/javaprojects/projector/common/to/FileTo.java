package ru.javaprojects.projector.common.to;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.util.validation.NoHtml;

import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileTo {
    @Nullable
    @NoHtml
    @Size(max = 128)
    private String fileName;

    @Nullable
    @NoHtml
    @Size(max = 512)
    private String fileLink;

    @Nullable
    private MultipartFile inputtedFile;

    @Nullable
    private byte[] inputtedFileBytes;

    public String getImageSrc() {
        if (fileLink != null) {
            return "/" + fileLink;
        }
        if (inputtedFileBytes == null || fileName == null) {
            return null;
        }
        String srcType = (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) ? "data:application/octet-stream;base64," :
                fileName.endsWith(".svg") ? "data:image/svg+xml;base64," : "data:image/*;base64,";
        return srcType + Base64.getEncoder().encodeToString(inputtedFileBytes);
    }
}
