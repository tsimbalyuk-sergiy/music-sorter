package dev.tsvinc.music.sort.service;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AudioFileServiceImplTest {

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