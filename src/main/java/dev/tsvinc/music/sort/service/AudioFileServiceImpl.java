package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.UNKNOWN;
import static org.tinylog.Logger.error;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.reference.GenreTypes;

import dev.tsvinc.music.sort.domain.Metadata;

import io.vavr.control.Try;

public class AudioFileServiceImpl implements AudioFileService {

    public static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
    private static final Pattern ZERO_TO_NINE_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern LETTERS_NUMBERS_SPACES_PATTERN = Pattern.compile("[^A-Za-z0-9\\-\\s&]+");
    private static final Pattern VA_PATTERN = Pattern.compile("((VA)|(va))(-|_-).*");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("[^\\d.]");

    private final FileService fileService;

    public AudioFileServiceImpl(FileService fileService) {
        this.fileService = fileService;
    }

    static void checkGenre(
            final List<String> genreList,
            final File musicFile,
            final List<String> artistList,
            final List<String> years,
            final boolean checkArtist)
            throws CannotReadException, IOException, TagException, InvalidAudioFrameException, ReadOnlyFileException {
        final var audioFile = AudioFileIO.read(musicFile);
        final var tag = audioFile.getTag();
        if (null != tag && !tag.getFirst(FieldKey.GENRE).isEmpty()) {
            final var genre = tag.getFirst(FieldKey.GENRE);
            if (AudioFileServiceImpl.ZERO_TO_NINE_PATTERN.matcher(genre).matches()) {
                final var genreNumericalConvert = AudioFileServiceImpl.extractNumber(genre);
                final var finalGenre =
                        GenreTypes.getInstanceOf().getValueForId(Integer.parseInt(genreNumericalConvert));
                genreList.add(finalGenre);
            } else {
                genreList.add(genre);
            }
            if (checkArtist) {
                artistList.add(tag.getFirst(FieldKey.ARTIST));
            }
            years.add(tag.getFirst(FieldKey.YEAR));
        } else {
            genreList.add(UNKNOWN);
        }
    }

    public static String genreToOneStyle(final String genre) {
        if (genre.isBlank()) {
            return genre;
        }

        return Arrays.stream(SPACE_PATTERN.split(genre))
                .map(word -> capitalizeWord(word))
                .collect(Collectors.joining(" "));
    }

    private static String capitalizeWord(String word) {
        if (word.isEmpty()) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    public static String sanitizeString(String sourceString, final boolean isGenre) {
        sourceString = null == sourceString || sourceString.isEmpty()
                ? UNKNOWN
                : AudioFileServiceImpl.LETTERS_NUMBERS_SPACES_PATTERN
                        .matcher(sourceString)
                        .replaceAll("");
        if (isGenre) {
            sourceString = AudioFileServiceImpl.genreToOneStyle(sourceString);
            sourceString = AudioFileServiceImpl.checkGenreForPredictedMatches(sourceString);
        }
        if (sourceString.contains("\u0000")) {
            sourceString = sourceString.replace("\u0000", "");
        }
        return sourceString;
    }

    private static String checkGenreForPredictedMatches(String genre) {
        if (Pattern.compile("hip.*hop").matcher(genre.toLowerCase()).find()) {
            genre = "Hip-Hop";
        } else if (Pattern.compile("(alt.*rock)").matcher(genre.toLowerCase()).find()) {
            genre = "Alternative Rock";
        } else if (Pattern.compile("(?!psy.*rock)(psy.*delic)")
                .matcher(genre.toLowerCase())
                .find()) {
            genre = "Psychedelic";
        } else if (Pattern.compile("(gangs|gangz)(ta)")
                .matcher(genre.toLowerCase())
                .find()) {
            genre = "Gangsta Rap";
        } else if (Pattern.compile("(electro)").matcher(genre.toLowerCase()).find()) {
            genre = "Electronic";
        } else if (Pattern.compile("(Lo-Fi)").matcher(genre.toLowerCase()).find()) {
            genre = "Lo-Fi";
        }
        return genre;
    }

    public static String findMostRepeatedString(final List<String> list) {
        final Map<String, Integer> stringsCount = new HashMap<>(list.size());
        for (final var string : list) {
            var counter = stringsCount.get(string);
            if (null == counter) {
                counter = 0;
            }
            counter++;
            stringsCount.put(string, counter);
        }
        final var mostRepeated =
                stringsCount.entrySet().stream().max(Entry.comparingByValue()).orElse(null);
        if (null != mostRepeated) {
            return mostRepeated.getKey();
        } else {
            return null;
        }
    }

    public static String extractNumber(final String str) {
        if (null == str || str.isEmpty()) {
            return "";
        }
        var found = false;
        final var charArray = str.toCharArray();
        final var sb = new StringBuilder(charArray.length);
        for (final var c : charArray) {
            if (Character.isDigit(c)) {
                sb.append(c);
                found = true;
            } else if (found) {
                break;
            }
        }
        return sb.toString();
    }

    private static void getMetadataForFile(
            final List<String> genreList,
            final String artist,
            final List<String> artistList,
            final List<String> years,
            final String string) {
        final var musicFile = new File(string);
        Try.of(() -> {
                    if ("va".equals(artist)) {
                        AudioFileServiceImpl.checkGenre(genreList, musicFile, null, years, false);
                    } else {
                        AudioFileServiceImpl.checkGenre(genreList, musicFile, artistList, years, true);
                    }
                    return null;
                })
                .onFailure(e -> error(
                        "[ERROR] Failed to read audio metadata from '{}': {}", musicFile.getName(), e.getMessage(), e));
    }

    public Metadata getMetadata(final String path) {
        final var listing = fileService.createFileListForEachDir(path);

        if (listing.fileList().isEmpty()) {
            return Metadata.createInvalid();
        }

        final String artist = determineArtistType(path);
        final var metadataLists = extractMetadataFromFiles(listing.fileList(), artist);

        final var mostRepeatedGenre = sanitizeString(findMostRepeatedString(metadataLists.genres()), true);
        final var mostRepeatedArtist = determineFinalArtist(artist, metadataLists.artists());
        final var year = parseYear(findMostRepeatedString(metadataLists.years()));

        return new Metadata(
                mostRepeatedGenre,
                listing.format(),
                mostRepeatedArtist,
                year,
                listing.fileList().size(),
                false);
    }

    private String determineArtistType(String path) {
        return VA_PATTERN.matcher(new File(path).getName()).matches() ? "va" : "";
    }

    private MetadataLists extractMetadataFromFiles(List<String> fileList, String artist) {
        final List<String> genreList = new CopyOnWriteArrayList<>();
        final List<String> artistList = new CopyOnWriteArrayList<>();
        final List<String> years = new CopyOnWriteArrayList<>();

        fileList.parallelStream()
                .forEach(filePath -> getMetadataForFile(genreList, artist, artistList, years, filePath));

        return new MetadataLists(genreList, artistList, years);
    }

    private String determineFinalArtist(String artist, List<String> artistList) {
        if ("va".equals(artist)) {
            return artist;
        }
        return sanitizeString(findMostRepeatedString(artistList), false);
    }

    private int parseYear(String yearString) {
        if (yearString == null || yearString.isEmpty()) {
            return 1990;
        }

        String cleanedYear = DECIMAL_PATTERN.matcher(yearString).replaceAll("");
        cleanedYear = normalizeYearLength(cleanedYear);

        return Integer.parseInt(cleanedYear);
    }

    private String normalizeYearLength(String year) {
        if (year.length() < 4) {
            return String.format("%1$-4s", year).replace(' ', '0');
        } else if (year.length() > 4) {
            return year.substring(0, 4);
        }
        return year;
    }

    private record MetadataLists(List<String> genres, List<String> artists, List<String> years) {}
}
