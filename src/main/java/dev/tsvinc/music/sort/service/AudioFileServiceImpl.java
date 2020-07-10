package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.UNKNOWN;
import static org.pmw.tinylog.Logger.error;

import dev.tsvinc.music.sort.domain.Metadata;
import ealvatag.audio.AudioFileIO;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import ealvatag.tag.reference.GenreTypes;
import io.vavr.control.Try;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;

public class AudioFileServiceImpl implements AudioFileService {

  @Inject FileService fileService;

  static void checkGenre(
      List<String> genreList,
      File musicFile,
      List<String> artistList,
      List<String> years,
      boolean checkArtist)
      throws CannotReadException, IOException, TagException, InvalidAudioFrameException {
    final var audioFile = AudioFileIO.read(musicFile);
    final Tag tag = audioFile.getTag().orNull();
    if (null != tag && !tag.getFirst(FieldKey.GENRE).isEmpty()) {
      final var genre = tag.getFirst(FieldKey.GENRE);
      /*check if genre is in a numeric format e.g. (043)*/
      if (genre.matches(".*[0-9].*")) {
        /*if so -- extract numbers and get genre value for it*/
        final var genreNumericalConvert = extractNumber(genre);
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
    final var words = genre.split("\\s");
    final List<String> o = new ArrayList<>();
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
      o.add(new String(charsOut));
    }
    final String output;
    if (1 < o.size()) {
      output = String.join(" ", o);
    } else {
      output = o.get(0);
    }
    return output;
  }

  public static String sanitizeString(String sourceString, boolean isGenre) {
    sourceString =
        sourceString == null || sourceString.isEmpty()
            ? UNKNOWN
            : sourceString.replaceAll("[^A-Za-z0-9\\-\\s&]+", "");
    if (isGenre) {
      sourceString = genreToOneStyle(sourceString);
      sourceString = checkGenreForPredictedMatches(sourceString);
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
    final Map<String, Integer> stringsCount = new HashMap<>();
    for (final var string : list) {
      var counter = stringsCount.get(string);
      if (null == counter) {
        counter = 0;
      }
      counter++;
      stringsCount.put(string, counter);
    }
    Map.Entry<String, Integer> mostRepeated = null;
    for (final var e : stringsCount.entrySet()) {
      if (null == mostRepeated || mostRepeated.getValue() < e.getValue()) {
        mostRepeated = e;
      }
    }
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
    final var sb = new StringBuilder();
    var found = false;
    final var charArray = str.toCharArray();
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

  public Metadata getMetadata(final String path) {
    final var listing = fileService.createFileListForEachDir(path);
    final List<String> genreList = new ArrayList<>();
    String artist = "";
    if (new File(path).getName().matches("((VA)|(va))(-|_-).*")) {
      artist = "va";
    }
    final List<String> artistList = new ArrayList<>(listing.getFileList().size());
    final List<String> years = new ArrayList<>(listing.getFileList().size());
    if (!listing.getFileList().isEmpty()) {
      for (final var string : listing.getFileList()) {
        getMetadataForFile(genreList, artist, artistList, years, string);
      }
      String mostRepeatedGenre = sanitizeString(findMostRepeatedString(genreList), true);
      String mostRepeatedArtist = artist;
      if (!artist.equals("va")) {
        mostRepeatedArtist = sanitizeString(findMostRepeatedString(artistList), false);
      }

      String mostRepeatedYear = findMostRepeatedString(years);

      int year = 1990;
      if (null != mostRepeatedYear && !mostRepeatedYear.isEmpty()) {
        mostRepeatedYear = mostRepeatedYear.replaceAll("[^\\d.]", "");
        if (mostRepeatedYear.length() < 4) { // in case we have something like: 199x
          mostRepeatedYear =
              String.format("%1$-" + 4 + "s", mostRepeatedYear)
                  .replace(' ', '0'); // for left pad use "%1$" + ...
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

  private void getMetadataForFile(
      List<String> genreList,
      String artist,
      List<String> artistList,
      List<String> years,
      String string) {
    final var musicFile = new File(string);
    Try.of(
            () -> {
              if (artist.equals("va")) {
                checkGenre(genreList, musicFile, null, years, false);
              } else {
                checkGenre(genreList, musicFile, artistList, years, true);
              }
              return null;
            })
        .onFailure(
            e -> error("error reading file: {}\n{}", musicFile.getName(), e.getMessage(), e));
  }
}
