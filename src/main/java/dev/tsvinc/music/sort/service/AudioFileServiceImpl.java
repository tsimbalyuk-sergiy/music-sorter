package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.UNKNOWN;
import static org.pmw.tinylog.Logger.error;

import dev.tsvinc.music.sort.domain.Metadata;
import ealvatag.audio.AudioFileIO;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.TagException;
import ealvatag.tag.reference.GenreTypes;
import io.vavr.control.Try;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.inject.Inject;

public class AudioFileServiceImpl implements AudioFileService {

    public static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
    private static final Pattern ZERO_TO_NINE_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern LETTERS_NUMBERS_SPACES_PATTERN = Pattern.compile("[^A-Za-z0-9\\-\\s&]+");
    private static final Pattern VA_PATTERN = Pattern.compile("((VA)|(va))(-|_-).*");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("[^\\d.]");

    @Inject
    FileService fileService;

    /**
     * It reads the genre of a music file and adds it to a list of genres
     *
     * @param genreList a list of genres that will be populated with the genres of the files in the directory
     * @param musicFile the file to be checked
     * @param artistList a list of artists
     * @param years a list of years that will be used to determine the oldest and newest songs in the library
     * @param checkArtist if true, the artist name will be added to the artistList
     */
    static void checkGenre(
            final List<String> genreList,
            final File musicFile,
            final List<String> artistList,
            final List<String> years,
            final boolean checkArtist)
            throws CannotReadException, IOException, TagException, InvalidAudioFrameException {
        final var audioFile = AudioFileIO.read(musicFile);
        final var tag = audioFile.getTag().orNull();
        if (null != tag && !tag.getFirst(FieldKey.GENRE).isEmpty()) {
            final var genre = tag.getFirst(FieldKey.GENRE);
            /*check if genre is in a numeric format e.g. (043)*/
            if (AudioFileServiceImpl.ZERO_TO_NINE_PATTERN.matcher(genre).matches()) {
                /*if so -- extract numbers and get genre value for it*/
                final var genreNumericalConvert = AudioFileServiceImpl.extractNumber(genre);
                final var finalGenre = GenreTypes.getInstanceOf().getValue(Integer.parseInt(genreNumericalConvert));
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

    /**
     * It takes a string, splits it into words, capitalizes the first letter of each word, and then joins the words back
     * together
     *
     * @param genre The genre to be converted.
     * @return A string with the first letter of each word capitalized.
     */
    public static String genreToOneStyle(final String genre) {
        if (genre.isEmpty() || genre.isBlank()) {
            return genre;
        }
        final var words = AudioFileServiceImpl.SPACE_PATTERN.split(genre);
        final List<String> out = new ArrayList<>(words.length);
        for (final var word : words) {
            final var chars = word.toCharArray();
            final var charsOut = new char[genre.length()];
            for (int i = 0, charsLength = chars.length; i < charsLength; i++) {
                if (0 == i) {
                    charsOut[i] = Character.toUpperCase(chars[i]);
                } else {
                    charsOut[i] = Character.toLowerCase(chars[i]);
                }
            }
            out.add(new String(charsOut));
        }
        final String output;
        if (1 < out.size()) {
            output = String.join(" ", out);
        } else {
            output = out.get(0);
        }
        return output;
    }

    /**
     * It takes a string, removes all non-alphanumeric characters, and returns the result
     *
     * @param sourceString The string to be sanitized
     * @param isGenre boolean
     * @return A string that has been sanitized.
     */
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

    /**
     * If the genre contains "hip", "alt", "psy", "gangs", "gangz", "electro", or "Lo-Fi", then replace the genre with the
     * corresponding genre
     *
     * @param genre The genre of the song.
     * @return A string that is the genre of the song.
     */
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

    /**
     * It takes a list of strings and returns the most repeated string in the list
     *
     * @param list The list of strings to search for the most repeated string.
     * @return The most repeated string in the list.
     */
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

    /**
     * If the input string is null or empty, return an empty string. Otherwise, loop through the string and append the
     * first sequence of digits to a StringBuilder
     *
     * @param str The string to extract the number from
     * @return The first sequence of digits in the string.
     */
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
                // If we already found a digit before and this char is not a digit, stop looping
                break;
            }
        }
        return sb.toString();
    }

    /**
     * > If the artist is "va" (various artists), then check the genre of the file against the list of genres, and if it
     * matches, add the file to the list of files to be copied
     *
     * @param genreList a list of genres to check for
     * @param artist the artist name
     * @param artistList a list of artists to check for. If the artist is "va" then this list is ignored.
     * @param years a list of years to check for
     * @param string the path to the file
     */
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
                .onFailure(e -> error("error reading file: {}\n{}", musicFile.getName(), e.getMessage(), e));
    }

    /**
     * It takes a path, creates a list of files in that path, then it iterates over the list of files, and for each file it
     * extracts the metadata (genre, artist, year) and then it finds the most repeated metadata for each of those fields
     *
     * @param path the path to the directory
     * @return A Metadata object
     */
    public Metadata getMetadata(final String path) {
        final var listing = this.fileService.createFileListForEachDir(path);
        var artist = "";
        if (AudioFileServiceImpl.VA_PATTERN.matcher(new File(path).getName()).matches()) {
            artist = "va";
        }
        final List<String> artistList = new ArrayList<>(listing.getFileList().size());
        final List<String> years = new ArrayList<>(listing.getFileList().size());
        if (!listing.getFileList().isEmpty()) {
            final List<String> genreList = new ArrayList<>(listing.getFileList().size());
            for (final var string : listing.getFileList()) {
                AudioFileServiceImpl.getMetadataForFile(genreList, artist, artistList, years, string);
            }
            final var mostRepeatedGenre =
                    AudioFileServiceImpl.sanitizeString(AudioFileServiceImpl.findMostRepeatedString(genreList), true);
            var mostRepeatedArtist = artist;
            if (!"va".equals(artist)) {
                mostRepeatedArtist = AudioFileServiceImpl.sanitizeString(
                        AudioFileServiceImpl.findMostRepeatedString(artistList), false);
            }

            var mostRepeatedYear = AudioFileServiceImpl.findMostRepeatedString(years);

            var year = 1990;
            if (null != mostRepeatedYear && !mostRepeatedYear.isEmpty()) {
                mostRepeatedYear = AudioFileServiceImpl.DECIMAL_PATTERN
                        .matcher(mostRepeatedYear)
                        .replaceAll("");
                if (4 > mostRepeatedYear.length()) { // in case we have something like: 199x
                    mostRepeatedYear = String.format("%1$-" + 4 + "s", mostRepeatedYear)
                            .replace(' ', '0'); // for left pad use "%1$" + ...
                } else if (4 < mostRepeatedYear.length()) { // in case we have something like: 20201106231346
                    mostRepeatedYear = mostRepeatedYear.substring(0, 4);
                }
                year = Integer.parseInt(mostRepeatedYear);
            }
            return Metadata.builder()
                    .genre(mostRepeatedGenre)
                    .format(listing.getFormat())
                    .artist(mostRepeatedArtist)
                    .year(year)
                    .audioFilesCount(listing.getFileList().size())
                    .build();
        } else {
            return Metadata.builder().invalid(true).build();
        }
    }
}
