package ru.javaprojects.projector;

import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public interface TestContentFilesManager {

    Path getContentPath();

    Path getTestDataFilesPath();

    @BeforeEach
    default void prepareContentFiles() throws IOException {
        cleanContentDir(getContentPath());
        createContentFiles(getContentPath());
    }

    private void cleanContentDir(Path contentPath) throws IOException {
        if (Files.exists(contentPath)) {
            Files.walkFileTree(contentPath, new SimpleFileVisitor<>() {

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
        }
    }

    private void createContentFiles(Path contentPath) throws IOException {
        Files.createDirectories(contentPath);
        Files.walkFileTree(getTestDataFilesPath(), new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(contentPath.resolve(getTestDataFilesPath().relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, contentPath.resolve(getTestDataFilesPath().relativize(file)), REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
