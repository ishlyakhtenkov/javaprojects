package ru.javaprojects.projector.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import ru.javaprojects.projector.TestContentFilesManager;
import ru.javaprojects.projector.common.error.IllegalRequestDataException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
class FileUtilTest implements TestContentFilesManager {
    private static final String NEW_DIR_NAME = "newDir";
    private static final String EXISTING_DIR_NAME = "angular";
    private static final String EXISTING_FILE_NAME = "angular.svg";
    private static final String EXISTING_FILE_PATH = EXISTING_DIR_NAME + "/" + EXISTING_FILE_NAME;
    private static final String NOT_EXISTING_DIR_NAME = "notExistDir";
    private static final String NOT_EXISTING_FILE_NAME = "notExistFile.svg";
    private static final String NOT_EXISTING_FILE_PATH = NOT_EXISTING_DIR_NAME + "/" + NOT_EXISTING_FILE_NAME;

    private static final String TEST_DATA_FILES_PATH = "src/test/test-data-files/technologies";

    @Value("${content-path.technologies}")
    private String contentPath;

    @Override
    public Path getContentPath() {
        return Paths.get(contentPath);
    }

    @Override
    public Path getTestDataFilesPath() {
        return Paths.get(TEST_DATA_FILES_PATH);
    }

    @Test
    void upload() throws IOException {
        MockMultipartFile file = new MockMultipartFile("fileName", new byte[] {1, 2, 3, 4, 5});
        FileUtil.upload(file, contentPath + NEW_DIR_NAME, file.getName());
        assertEquals(file.getSize(), Files.size(Paths.get(contentPath, NEW_DIR_NAME, file.getName())));
        assertTrue(Files.exists(Paths.get(contentPath, NEW_DIR_NAME, file.getName())));
    }

    @Test
    void uploadWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("fileName", new byte[] {});
        assertThrows(IllegalRequestDataException.class, () -> FileUtil.upload(file, contentPath, file.getName()));
        assertTrue(Files.notExists(Paths.get(contentPath, file.getName())));
    }

    @Test
    void uploadWhenFileExists() throws IOException {
        assertTrue(Files.exists(Paths.get(contentPath, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
        MockMultipartFile file = new MockMultipartFile(EXISTING_FILE_NAME, new byte[] {1, 2, 3, 4, 5});
        FileUtil.upload(file, contentPath + EXISTING_DIR_NAME, file.getName());
        assertTrue(Files.exists(Paths.get(contentPath, EXISTING_DIR_NAME, file.getName())));
        assertEquals(file.getSize(), Files.size(Paths.get(contentPath, EXISTING_DIR_NAME, file.getName())));
    }

    @Test
    void deleteDirectory() {
        FileUtil.deleteDirectory(contentPath + EXISTING_DIR_NAME);
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_DIR_NAME)));
    }

    @Test
    void deleteDirectoryWhenNotExists() {
        assertTrue(Files.notExists(Paths.get(contentPath + NOT_EXISTING_DIR_NAME)));
        assertThrows(IllegalArgumentException.class, () -> FileUtil.deleteDirectory(contentPath + NOT_EXISTING_DIR_NAME));
    }

    @Test
    void deleteDirectoryWhenNotDirectory() {
        assertTrue(Files.exists(Paths.get(contentPath + EXISTING_FILE_PATH)));
        assertThrows(IllegalArgumentException.class, () -> FileUtil.deleteDirectory(contentPath + EXISTING_FILE_PATH));
    }

    @Test
    void moveFile() {
        FileUtil.moveFile(contentPath + EXISTING_FILE_PATH, contentPath + NEW_DIR_NAME);
        assertTrue(Files.exists(Paths.get(contentPath, NEW_DIR_NAME, EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_FILE_PATH)));
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_DIR_NAME)));
    }

    @Test
    void moveFileToSameDir() {
        FileUtil.moveFile(contentPath + EXISTING_FILE_PATH, contentPath + EXISTING_DIR_NAME);
        assertTrue(Files.exists(Paths.get(contentPath, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
    }

    @Test
    void moveFileWhenFileNotExists() {
        assertThrows(IllegalArgumentException.class, () ->
                FileUtil.moveFile(contentPath + NOT_EXISTING_FILE_PATH, contentPath + NEW_DIR_NAME));
        assertTrue(Files.notExists(Paths.get(contentPath, NEW_DIR_NAME, NOT_EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(contentPath, NEW_DIR_NAME)));
    }

    @Test
    void moveFileWhenFileNotFile() {
        assertThrows(IllegalArgumentException.class, () ->
                FileUtil.moveFile(contentPath + EXISTING_DIR_NAME, contentPath + NEW_DIR_NAME));
        assertTrue(Files.notExists(Paths.get(contentPath, NEW_DIR_NAME)));
        assertTrue(Files.exists(Paths.get(contentPath, EXISTING_DIR_NAME)));
    }

    @Test
    void moveFileWhenFileExistsInNewDir() throws IOException {
        Files.createDirectories(Paths.get(contentPath, NEW_DIR_NAME));
        Files.createFile(Paths.get(contentPath, NEW_DIR_NAME, EXISTING_FILE_NAME));

        FileUtil.moveFile(contentPath + EXISTING_FILE_PATH, contentPath + NEW_DIR_NAME);
        assertTrue(Files.exists(Paths.get(contentPath, NEW_DIR_NAME, EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_FILE_PATH)));
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_DIR_NAME)));
    }

    @Test
    void moveFileWhenFileNotAloneInOldDir() throws IOException {
        String anotherFileName = "anotherFile.svg";
        Files.createFile(Paths.get(contentPath, EXISTING_DIR_NAME, anotherFileName));

        FileUtil.moveFile(contentPath + EXISTING_FILE_PATH, contentPath + NEW_DIR_NAME);
        assertTrue(Files.exists(Paths.get(contentPath, NEW_DIR_NAME, EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_FILE_PATH)));
        assertTrue(Files.exists(Paths.get(contentPath, EXISTING_DIR_NAME, anotherFileName)));
    }

    @Test
    void deleteFile() {
        FileUtil.deleteFile(contentPath + EXISTING_FILE_PATH);
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_FILE_PATH)));
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_DIR_NAME)));
    }

    @Test
    void deleteFileWhenNotExists() {
        assertThrows(IllegalArgumentException.class, () -> FileUtil.deleteFile(contentPath + NOT_EXISTING_FILE_PATH));
    }

    @Test
    void deleteFileWhenNotFile() {
        assertThrows(IllegalArgumentException.class, () -> FileUtil.deleteFile(contentPath + EXISTING_DIR_NAME));
    }

    @Test
    void deleteFileWhenNotAloneInDir() throws IOException {
        String anotherFileName = "anotherFile.svg";
        Files.createFile(Paths.get(contentPath, EXISTING_DIR_NAME, anotherFileName));

        FileUtil.deleteFile(contentPath + EXISTING_FILE_PATH);
        assertTrue(Files.notExists(Paths.get(contentPath, EXISTING_FILE_PATH)));
        assertTrue(Files.exists(Paths.get(contentPath, EXISTING_DIR_NAME, anotherFileName)));
    }

    @Test
    void normalizeFileName() {
        assertEquals("apache_tomcat.svg", FileUtil.normalizePath("Apache Tomcat.svg"));
        assertEquals("spring.svg", FileUtil.normalizePath("Spring.svg"));
        assertEquals("apache__tomcat_file.svg", FileUtil.normalizePath("Apache  Tomcat File.svg"));
        assertEquals("apache_tomcat_file.svg", FileUtil.normalizePath("Apache_Tomcat_File.svg"));
    }
}
