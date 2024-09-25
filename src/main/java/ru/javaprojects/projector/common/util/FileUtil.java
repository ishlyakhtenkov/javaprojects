package ru.javaprojects.projector.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.projector.common.error.FileException;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;
import ru.javaprojects.projector.common.to.FileTo;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@UtilityClass
public class FileUtil {
    public static void upload(MultipartFile multipartFile, String dirPath, String fileName) {
        try {
            upload(multipartFile.getBytes(), dirPath, fileName);
        } catch (IOException e) {
            throw new FileException("Failed to upload file: " + fileName +
                    ": " + e.getMessage(), "file.failed-to-upload", new Object[]{fileName});
        }
    }

    public static void upload(byte[] fileBytes, String dirPath, String fileName) {
        if (fileBytes.length == 0) {
            throw new IllegalRequestDataException("File must not be empty: " + fileName, "file.must-not-be-empty",
                    new Object[]{fileName});
        }
        try (OutputStream outStream = Files.newOutputStream(Files.createDirectories(Paths.get(dirPath)).resolve(fileName))) {
            outStream.write(fileBytes);
        } catch (IOException e) {
            throw new FileException("Failed to upload file: " + fileName +
                    ": " + e.getMessage(), "file.failed-to-upload", new Object[]{fileName});
        }
    }

    public static void upload(FileTo fileTo, String dirPath, String fileName) {
        Assert.notNull(fileTo, "fileTo must not be null");
        if ((fileTo.getInputtedFile() != null && !fileTo.getInputtedFile().isEmpty())) {
            upload(fileTo.getInputtedFile(), dirPath, fileName);
        } else if (fileTo.getInputtedFileBytes() != null && fileTo.getInputtedFileBytes().length != 0) {
            upload(fileTo.getInputtedFileBytes(), dirPath, fileName);
        } else {
            throw new IllegalRequestDataException("File must not be empty: " + fileName, "file.must-not-be-empty",
                    new Object[]{fileName});
        }
    }

    public static void deleteDirectory(String dirPath) {
        Path dir = Paths.get(dirPath);
        if (Files.notExists(dir)) {
            throw new IllegalArgumentException("Directory does not exist: " + dirPath);
        }
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Not a directory: " + dirPath);
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw e;
                    }
                }
            });
        } catch (IOException e) {
            throw new FileException("Failed to delete dir: " + dirPath + ": " + e.getMessage(), "file.failed-to-delete-dir",
                    new Object[]{dir.getFileName()});
        }
    }

    public static void moveFile(String filePath, String dirPath) {
        try {
            Path file = Paths.get(filePath);
            checkNotExistOrDirectory(file);
            Path dir = Paths.get(dirPath);
            if (!file.equals(dir.resolve(file.getFileName()))) {
                Files.createDirectories(dir);
                Files.move(file, dir.resolve(file.getFileName()), REPLACE_EXISTING);
                deleteEmptyParentDirs(file);
            }
        } catch (IOException ex) {
            throw new FileException("Failed to move " + filePath + " to " + dirPath, "file.failed-to-move", null);
        }
    }

    public static void deleteFile(String filePath) {
        try {
            Path file = Paths.get(filePath);
            checkNotExistOrDirectory(file);
            Files.delete(file);
            deleteEmptyParentDirs(file);
        } catch (IOException ex) {
            throw new FileException("Failed to delete file: " + filePath, "file.failed-to-delete", new Object[]{filePath});
        }
    }

    private static void deleteEmptyParentDirs(Path file) throws IOException {
        Path parentDir = file.getParent();
        while (parentDir != null) {
            try (Stream<Path> otherFiles = Files.list(parentDir)) {
                if (otherFiles.findAny().isEmpty()) {
                    deleteDirectory(parentDir.toString());
                    parentDir = parentDir.getParent();
                } else {
                    parentDir = null;
                }
            }
        }
    }

    private static void checkNotExistOrDirectory(Path file) {
        if (Files.notExists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (Files.isDirectory(file)) {
            throw new IllegalArgumentException("Not a file: " + file);
        }
    }

    public static String normalizePath(String path) {
        Assert.notNull(path, "path must not be null");
        return path.toLowerCase().replace(' ', '_');
    }
}
