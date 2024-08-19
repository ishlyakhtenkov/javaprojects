package ru.javaprojects.projector.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@UtilityClass
public class FileUtil {

    public static void upload(MultipartFile multipartFile, String directoryPath, String fileName) {
        if (multipartFile.isEmpty()) {
            throw new IllegalRequestDataException("File must not be empty: " + fileName, "file.must-not-be-empty", null);
        }
        File dir = new File(directoryPath);
        if (dir.exists() || dir.mkdirs()) {
            File file = new File(directoryPath + fileName);
            try (OutputStream outStream = new FileOutputStream(file)) {
                outStream.write(multipartFile.getBytes());
            } catch (IOException ex) {
                throw new IllegalRequestDataException("Failed to upload file: " + multipartFile.getOriginalFilename() +
                        ": " + ex.getMessage(), "file.failed-to-upload", new Object[]{fileName});
            }
        }
    }
}
