package ru.javaprojects.projector.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.to.FileTo;

import java.util.function.Supplier;

class UtilTest {
    private static final String FILES_PATH = "/content/inputted-files/";
    private static final String DIR = "files/";
    private static final String ORIGINAL_FILE_NAME = "New project logo.png";
    private static final MockMultipartFile INPUTTED_FILE = new MockMultipartFile("inputtedFile", ORIGINAL_FILE_NAME,
            MediaType.IMAGE_PNG_VALUE, "new logo file content bytes".getBytes());

    @Test
    void createFileWhenInputtedFileIsNotEmpty() {
        File expected = new File(FileUtil.normalizePath(ORIGINAL_FILE_NAME), FILES_PATH +
                FileUtil.normalizePath(DIR + ORIGINAL_FILE_NAME));
        Supplier<FileTo> fileToExtractor = () -> new FileTo(null, null, INPUTTED_FILE, null);
        File created = Util.createFile(fileToExtractor, FILES_PATH, DIR);
        Assertions.assertThat(created).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void createFileWhenInputtedFileIsEmpty() {
        File expected = new File(FileUtil.normalizePath(ORIGINAL_FILE_NAME), FILES_PATH +
                FileUtil.normalizePath(DIR + ORIGINAL_FILE_NAME));
        Supplier<FileTo> fileToExtractor = () -> new FileTo(ORIGINAL_FILE_NAME, null, null, new byte[] {1, 2, 3, 4});
        File created = Util.createFile(fileToExtractor, FILES_PATH, DIR);
        Assertions.assertThat(created).usingRecursiveComparison().isEqualTo(expected);
    }
}