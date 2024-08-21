package ru.javaprojects.projector.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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

    public static void deleteDir(String path) {
        Path dirPath = Paths.get(path);
        if (Files.isDirectory(dirPath)) {
            try {
                Files.walkFileTree(dirPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
            } catch (IOException ex) {
                throw new IllegalRequestDataException("Failed to delete dir: " + path, "file.failed-to-delete-dir", null);
            }
        }
    }

    public static void moveFile(String file, String newDir) {
        try {
            Path filePath = Paths.get(file);
            Path newDirPath = Paths.get(newDir);
            if (Files.notExists(newDirPath)) {
                Files.createDirectories(newDirPath);
            }
            Files.move(filePath, newDirPath.resolve(filePath.getFileName()), REPLACE_EXISTING);

            File oldDir = new File(file).getParentFile();
            if (oldDir.isDirectory() && Objects.requireNonNull(oldDir.list()).length == 0) {
                delete(oldDir.getPath());
            }
        } catch (IOException ex) {
            throw new IllegalRequestDataException("Failed to move " + file + " to " + newDir, "file.failed-to-move", null);
        }
    }

    public static void delete(String path) {
        try {
            boolean isFile = new File(path).isFile();
            Files.delete(Paths.get(path));
            if (isFile) {
                File dir = new File(path).getParentFile();
                if (Objects.requireNonNull(dir.list()).length == 0) {
                    Files.delete(dir.toPath());
                }
            }
        } catch (IOException ex) {
            throw new IllegalRequestDataException("Failed to delete file: " + path, "file.failed-to-delete", null);
        }
    }
}
