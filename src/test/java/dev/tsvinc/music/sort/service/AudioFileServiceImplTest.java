package dev.tsvinc.music.sort.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.tsvinc.music.sort.domain.ListingWithFormat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AudioFileServiceImplTest {

    @Test
    @DisplayName("Should return the number when the input is a number with letters")
    void extractNumberWhenInputIsANumberWithLettersThenReturnTheNumber() {
        final var input = "123abc";
        final var expected = "123";
        final var actual = AudioFileServiceImpl.extractNumber(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should return an empty string when the input is null")
    void extractNumberWhenInputIsNullThenReturnEmptyString() {
        assertEquals("", AudioFileServiceImpl.extractNumber(null));
    }

    @Test
    @DisplayName("Should return an empty string when the input is empty")
    void extractNumberWhenInputIsEmptyThenReturnEmptyString() {
        assertEquals("", AudioFileServiceImpl.extractNumber(""));
    }

    @Test
    @DisplayName("Should return the number when the input is a number")
    void extractNumberWhenInputIsANumberThenReturnTheNumber() {
        final var input = "123";
        final var expected = "123";
        final var actual = AudioFileServiceImpl.extractNumber(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should return an invalid metadata object when the path is invalid")
    void getMetadataWhenPathIsInvalidThenReturnInvalidMetadataObject() {
        final var audioFileService = new AudioFileServiceImpl();
        final var fileService = mock(FileService.class);
        audioFileService.fileService = fileService;
        when(fileService.createFileListForEachDir(anyString()))
                .thenReturn(new ListingWithFormat("", new ArrayList<>()));

        final var metadata = audioFileService.getMetadata("");

        assertTrue(metadata.invalid());
    }

    @Test
    @DisplayName("Should return a valid metadata object when the path is valid")
    void getMetadataWhenPathIsValidThenReturnValidMetadataObject() {
        final var audioFileService = new AudioFileServiceImpl();
        final var fileService = mock(FileService.class);
        audioFileService.fileService = fileService;
        final var fileList = new ArrayList<String>();
        fileList.add("/path/to/file/file1.mp3");
        fileList.add("/path/to/file/file2.mp3");
        when(fileService.createFileListForEachDir("/path/to/file")).thenReturn(new ListingWithFormat("mp3", fileList));

        final var audioFile = mock(AudioFile.class);
        final var tag = mock(Tag.class);
        when(audioFile.getTag()).thenReturn(tag);
        when(tag.getFirst(FieldKey.GENRE)).thenReturn("genre");
        when(tag.getFirst(FieldKey.ARTIST)).thenReturn("artist");
        when(tag.getFirst(FieldKey.YEAR)).thenReturn("2020");

        final var metadata = audioFileService.getMetadata("/path/to/file");

        assertNotNull(metadata);
    }

    @Test
    @DisplayName("Should return null when the list is empty")
    void findMostRepeatedStringShouldReturnNullWhenTheListIsEmpty() {
        final var list = new ArrayList<String>();
        final var result = AudioFileServiceImpl.findMostRepeatedString(list);
        assertNull(result);
    }

    @Test
    @DisplayName("Should return the most repeated string in the list")
    void findMostRepeatedStringShouldReturnTheMostRepeatedStringInTheList() {
        final var list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("a");
        list.add("a");
        list.add("b");
        final var result = AudioFileServiceImpl.findMostRepeatedString(list);
        assertEquals("a", result);
    }

    @Test
    @DisplayName("Should return electronic when the genre is electro")
    void checkGenreForPredictedMatchesShouldReturnElectronicWhenTheGenreIsElectro() {
        final var genre = "electro";
        final var result = AudioFileServiceImpl.checkGenreForPredictedMatches(genre);
        assertEquals("Electronic", result);
    }

    @Test
    @DisplayName("Should return alternative rock when the genre is alt rock")
    void checkGenreForPredictedMatchesShouldReturnAlternativeRockWhenTheGenreIsAltRock() {
        final var genre = "alt rock";
        final var result = AudioFileServiceImpl.checkGenreForPredictedMatches(genre);
        assertEquals("Alternative Rock", result);
    }

    @Test
    @DisplayName("Should return gangsta rap when the genre is gangsta rap")
    void checkGenreForPredictedMatchesShouldReturnGangstaRapWhenTheGenreIsGangstaRap() {
        final var genre = "gangsta rap";
        final var result = AudioFileServiceImpl.checkGenreForPredictedMatches(genre);
        assertEquals("Gangsta Rap", result);
    }

    @Test
    @DisplayName("Should return psychedelic when the genre is psy delic")
    void checkGenreForPredictedMatchesShouldReturnPsychedelicWhenTheGenreIsPsyDeli() {
        final var genre = "psy delic";
        final var result = AudioFileServiceImpl.checkGenreForPredictedMatches(genre);
        assertEquals("Psychedelic", result);
    }

    @Test
    @DisplayName("Should return psychedelic when the genre is psychedelic and so on")
    void testCheckGenreForPredictedMatches() {
        assertEquals("Genre", AudioFileServiceImpl.checkGenreForPredictedMatches("Genre"));
        assertEquals("Hip-Hop", AudioFileServiceImpl.checkGenreForPredictedMatches("hipUhop"));
        assertEquals("Alternative Rock", AudioFileServiceImpl.checkGenreForPredictedMatches("altUrock"));
        assertEquals("Psychedelic", AudioFileServiceImpl.checkGenreForPredictedMatches("psyUdelic"));
        assertEquals("Gangsta Rap", AudioFileServiceImpl.checkGenreForPredictedMatches("gangsta"));
        assertEquals("Electronic", AudioFileServiceImpl.checkGenreForPredictedMatches("electro"));
    }

    @Test
    @DisplayName("Should return hip-hop when the genre is hip hop")
    void checkGenreForPredictedMatchesShouldReturnHipHopWhenTheGenreIsHipHop() {
        final var genre = "hip hop";
        final var result = AudioFileServiceImpl.checkGenreForPredictedMatches(genre);
        assertEquals("Hip-Hop", result);
    }

    @Test
    @DisplayName("Should return the genre in one style when the genre is not empty")
    void genreToOneStyleWhenGenreIsNotEmptyThenReturnTheGenreInOneStyle() {
        String genre = "hip-hop";
        String result = AudioFileServiceImpl.genreToOneStyle(genre);
        assertEquals("Hip-hop", result);
    }

    @Test
    @DisplayName("Should return the genre in one style when the genre is not blank")
    void genreToOneStyleWhenGenreIsNotBlankThenReturnTheGenreInOneStyle() {
        final var genre = "hip-hop";
        final var result = AudioFileServiceImpl.genreToOneStyle(genre);
        assertEquals("Hip-hop", result);
    }

    @Test
    @DisplayName("Should add the genre to the list when the tag is not null and the genre is not empty")
    void checkGenreWhenTagIsNotNullAndGenreIsNotEmptyThenAddGenreToList()
            throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {
        final var genreList = new ArrayList<String>();
        final var musicFile = new File("src/test/resources/samples/sample1.mp3");
        final var audioFile = mock(AudioFile.class);
        final var tag = mock(Tag.class);
        when(audioFile.getTag()).thenReturn(tag);
        when(tag.getFirst(FieldKey.GENRE)).thenReturn("genre");
        AudioFileServiceImpl.checkGenre(genreList, musicFile, new ArrayList<>(), new ArrayList<>(), false);
        assertEquals("SomeSpecificGenre", genreList.get(0));
    }

    @Test
    @DisplayName("Should add unknown to the list when the tag is null or empty")
    void checkGenreWhenTagIsNullOrEmptyThenAddUnknownToList()
            throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {
        final var genreList = new ArrayList<String>();
        final var musicFile = new File("src/test/resources/samples/sample3.mp3");

        AudioFileServiceImpl.checkGenre(genreList, musicFile, new ArrayList<>(), new ArrayList<>(), false);

        assertEquals(1, genreList.size());
        assertEquals("UNKNOWN", genreList.get(0));
    }

    @Test
    @DisplayName(
            "Should add the genre to the list when the tag is not null and the genre is not empty and it's a number")
    void checkGenreWhenTagIsNotNullAndGenreIsNotEmptyAndItIsANumberThenAddConvertedNumberToList()
            throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {
        final var genreList = new ArrayList<String>();
        final var musicFile = new File("src/test/resources/samples/sample1.mp3");

        AudioFileServiceImpl.checkGenre(genreList, musicFile, new ArrayList<>(), new ArrayList<>(), false);

        assertEquals("SomeSpecificGenre", genreList.get(0));
    }
}