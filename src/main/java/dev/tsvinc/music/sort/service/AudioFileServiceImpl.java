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
  private static final Pattern LETTERS_NUMBERS_SPACES_PATTERN =
      Pattern.compile("[^A-Za-z0-9\\-\\s&]+");
  private static final Pattern VA_PATTERN = Pattern.compile("((VA)|(va))(-|_-).*");
  private static final Pattern DECIMAL_PATTERN = Pattern.compile("[^\\d.]");
  @Inject FileService fileService;

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
        final var finalGenre =
            GenreTypes.getInstanceOf().getValue(Integer.parseInt(genreNumericalConvert));
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

  public static String sanitizeString(String sourceString, final boolean isGenre) {
    sourceString =
        null == sourceString || sourceString.isEmpty()
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
    } else if (Pattern.compile("(?!psy.*rock)(psy.*delic)").matcher(genre.toLowerCase()).find()) {
      genre = "Psychedelic";
    } else if (Pattern.compile("(gangs|gangz)(ta)").matcher(genre.toLowerCase()).find()) {
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
        // If we already found a digit before and this char is not a digit, stop looping
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
    Try.of(
            () -> {
              if ("va".equals(artist)) {
                AudioFileServiceImpl.checkGenre(genreList, musicFile, null, years, false);
              } else {
                AudioFileServiceImpl.checkGenre(genreList, musicFile, artistList, years, true);
              }
              return null;
            })
        .onFailure(
            e -> error("error reading file: {}\n{}", musicFile.getName(), e.getMessage(), e));
  }

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
          AudioFileServiceImpl.sanitizeString(
              AudioFileServiceImpl.findMostRepeatedString(genreList), true);
      var mostRepeatedArtist = artist;
      if (!"va".equals(artist)) {
        mostRepeatedArtist =
            AudioFileServiceImpl.sanitizeString(
                AudioFileServiceImpl.findMostRepeatedString(artistList), false);
      }

      var mostRepeatedYear = AudioFileServiceImpl.findMostRepeatedString(years);

      var year = 1990;
      if (null != mostRepeatedYear && !mostRepeatedYear.isEmpty()) {
        mostRepeatedYear =
            AudioFileServiceImpl.DECIMAL_PATTERN.matcher(mostRepeatedYear).replaceAll("");
        if (4 > mostRepeatedYear.length()) { // in case we have something like: 199x
          mostRepeatedYear =
              String.format("%1$-" + 4 + "s", mostRepeatedYear)
                  .replace(' ', '0'); // for left pad use "%1$" + ...
        } else if (4
            < mostRepeatedYear.length()) { // in case we have something like: 20201106231346
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
