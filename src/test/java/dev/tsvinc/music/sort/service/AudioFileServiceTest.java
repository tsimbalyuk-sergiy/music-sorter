package dev.tsvinc.music.sort.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AudioFileServiceTest {

    @Test
    @DisplayName("Should return the number when the input is a number with letters")
    void extractNumberWhenInputIsANumberWithLettersThenReturnTheNumber() {
        final var input = "123abc";
        final var expected = "123";
        final var actual = AudioFileService.extractNumber(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should return an empty string when the input is null")
    void extractNumberWhenInputIsNullThenReturnEmptyString() {
        assertEquals("", AudioFileService.extractNumber(null));
    }

    @Test
    @DisplayName("Should return an empty string when the input is empty")
    void extractNumberWhenInputIsEmptyThenReturnEmptyString() {
        assertEquals("", AudioFileService.extractNumber(""));
    }

    @Test
    @DisplayName("Should return the number when the input is a number")
    void extractNumberWhenInputIsANumberThenReturnTheNumber() {
        final var input = "123";
        final var expected = "123";
        final var actual = AudioFileService.extractNumber(input);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should return an invalid metadata object when the path is invalid")
    void getMetadataWhenPathIsInvalidThenReturnInvalidMetadataObject() {
        final var audioFileService = AudioFileService.getInstance();

        final var metadata = audioFileService.getMetadata("src/test/resources/samples/sample#.info");

        assertTrue(metadata.invalid());
    }

    @Test
    @DisplayName("Should return a valid metadata object when the path is valid")
    void getMetadataWhenPathIsValidThenReturnValidMetadataObject() {
        final var audioFileService = AudioFileService.getInstance();

        final var metadata = audioFileService.getMetadata("src/test/resources/samples/sample1.mp3");

        assertNotNull(metadata);
    }

    @Test
    @DisplayName("Should return null when the list is empty")
    void findMostRepeatedStringShouldReturnNullWhenTheListIsEmpty() {
        final var list = new ArrayList<String>();
        final var result = AudioFileService.findMostRepeatedString(list);
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
        final var result = AudioFileService.findMostRepeatedString(list);
        assertEquals("a", result);
    }

    @Test
    @DisplayName("Should return electronic when the genre is electro")
    void checkGenreForPredictedMatchesShouldReturnElectronicWhenTheGenreIsElectro() {
        final var genre = "electro";
        final var result = AudioFileService.checkGenreForPredictedMatches(genre);
        assertEquals("Electronic", result);
    }

    @Test
    @DisplayName("Should return alternative rock when the genre is alt rock")
    void checkGenreForPredictedMatchesShouldReturnAlternativeRockWhenTheGenreIsAltRock() {
        final var genre = "alt rock";
        final var result = AudioFileService.checkGenreForPredictedMatches(genre);
        assertEquals("Alternative Rock", result);
    }

    @Test
    @DisplayName("Should return gangsta rap when the genre is gangsta rap")
    void checkGenreForPredictedMatchesShouldReturnGangstaRapWhenTheGenreIsGangstaRap() {
        final var genre = "gangsta rap";
        final var result = AudioFileService.checkGenreForPredictedMatches(genre);
        assertEquals("Gangsta Rap", result);
    }

    @Test
    @DisplayName("Should return psychedelic when the genre is psy delic")
    void checkGenreForPredictedMatchesShouldReturnPsychedelicWhenTheGenreIsPsyDeli() {
        final var genre = "psy delic";
        final var result = AudioFileService.checkGenreForPredictedMatches(genre);
        assertEquals("Psychedelic", result);
    }

    @Test
    @DisplayName("Should return psychedelic when the genre is psychedelic and so on")
    void testCheckGenreForPredictedMatches() {
        assertEquals("Genre", AudioFileService.checkGenreForPredictedMatches("Genre"));
        assertEquals("Hip-Hop", AudioFileService.checkGenreForPredictedMatches("hipUhop"));
        assertEquals("Alternative Rock", AudioFileService.checkGenreForPredictedMatches("altUrock"));
        assertEquals("Psychedelic", AudioFileService.checkGenreForPredictedMatches("psyUdelic"));
        assertEquals("Gangsta Rap", AudioFileService.checkGenreForPredictedMatches("gangsta"));
        assertEquals("Electronic", AudioFileService.checkGenreForPredictedMatches("electro"));
    }

    @Test
    @DisplayName("Should return hip-hop when the genre is hip hop")
    void checkGenreForPredictedMatchesShouldReturnHipHopWhenTheGenreIsHipHop() {
        final var genre = "hip hop";
        final var result = AudioFileService.checkGenreForPredictedMatches(genre);
        assertEquals("Hip-Hop", result);
    }

    @Test
    @DisplayName("Should return the genre in one style when the genre is not empty")
    void genreToOneStyleWhenGenreIsNotEmptyThenReturnTheGenreInOneStyle() {
        String genre = "hip-hop";
        String result = AudioFileService.genreToOneStyle(genre);
        assertEquals("Hip-hop", result);
    }

    @Test
    @DisplayName("Should return the genre in one style when the genre is not blank")
    void genreToOneStyleWhenGenreIsNotBlankThenReturnTheGenreInOneStyle() {
        final var genre = "hip-hop";
        final var result = AudioFileService.genreToOneStyle(genre);
        assertEquals("Hip-hop", result);
    }

    @Test
    @DisplayName("Should add the genre to the list when the tag is not null and the genre is not empty")
    void checkGenreWhenTagIsNotNullAndGenreIsNotEmptyThenAddGenreToList()
            throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {
        final var genreList = new ArrayList<String>();
        final var musicFile = new File("src/test/resources/samples/sample1.mp3");
        AudioFileService.checkGenre(genreList, musicFile, new ArrayList<>(), new ArrayList<>(), false);
        assertEquals("SomeSpecificGenre", genreList.get(0));
    }

    @Test
    @DisplayName("Should add unknown to the list when the tag is null or empty")
    void checkGenreWhenTagIsNullOrEmptyThenAddUnknownToList()
            throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {
        final var genreList = new ArrayList<String>();
        final var musicFile = new File("src/test/resources/samples/sample3.mp3");

        AudioFileService.checkGenre(genreList, musicFile, new ArrayList<>(), new ArrayList<>(), false);

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

        AudioFileService.checkGenre(genreList, musicFile, new ArrayList<>(), new ArrayList<>(), false);

        assertEquals("SomeSpecificGenre", genreList.get(0));
    }
}