package dev.tsvinc.music.sort.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.tsvinc.music.sort.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileServiceTest {

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = FileService.getInstance();
    }

    @Test
    @DisplayName("Should return a list of mp3 files when the folder exists")
    void listMp3FilesWhenFolderExistsThenReturnListOfFiles() {
        final var folderName = "src/test/resources/samples";
        final var result = fileService.listFiles(folderName, Constants.MP3_FORMAT);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return a list of flac files when the folder exists")
    void listFlacFilesWhenFolderExistsThenReturnListOfFiles() {
        final var folderName = "src/test/resources/samples";
        final var result = fileService.listFiles(folderName, Constants.FLAC_FORMAT);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return an empty list when the folder does not exist")
    void listFilesWhenFolderDoesNotExistThenReturnEmptyList() {
        final var result = fileService.listFiles("/tmp/does-not-exist", Constants.MP3_FORMAT);
        assertTrue(result.isEmpty());
    }
}