package dev.tsvinc.music.sort;

import static dev.tsvinc.music.sort.Constants.FLAC;
import static dev.tsvinc.music.sort.Constants.FLAC_FORMAT;
import static dev.tsvinc.music.sort.Constants.MP3;
import static dev.tsvinc.music.sort.Constants.MP_3_FORMAT;
import static dev.tsvinc.music.sort.Constants.UNKNOWN;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.reference.GenreTypes;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ProcessDirectory {

  private ProcessDirectory() {}

  public static GenreWithFormat getGenreTag(final String path) {
    final var fileListForEachDir = createFileListForEachDir(path);
    final List<String> genreList = new ArrayList<>();
    for (final var string : fileListForEachDir.getFileList()) {
      final var musicFile = new File(string);
      try {
        final var audioFile = AudioFileIO.read(musicFile);
        final var tag = audioFile.getTag();
        if (null != tag && !tag.isEmpty()) {
          final var genre = tag.getFirst(FieldKey.GENRE);
          /*check if genre is in a numeric format e.g. (043)*/
          if (genre.matches(".*[0-9].*")) {
            /*if so -- extract numbers and get genre value for it*/
            final var genreNumericalConvert = extractNumber(genre);
            final var finalGenre =
                GenreTypes.getInstanceOf().getValueForId(Integer.parseInt(genreNumericalConvert));
            genreList.add(finalGenre);
          } else {
            genreList.add(genre);
          }
        } else {
          genreList.add(UNKNOWN);
        }
      } catch (final IOException
          | CannotReadException
          | ReadOnlyFileException
          | TagException
          | InvalidAudioFrameException e) {
        Logger.error("error reading file: {}\n{}", musicFile.getName(), e.getMessage(), e);
      }
    }
    final var mostRepeatedGenre = findMostRepeatedGenre(genreList);
    return GenreWithFormat.builder()
        .genre(mostRepeatedGenre)
        .format(fileListForEachDir.getFormat())
        .build();
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

  public static String sanitizeGenre(String genre) {
    genre =
        genre == null || genre.isEmpty() ? UNKNOWN : genre.replaceAll("[^A-Za-z0-9\\-\\s&]+", "");
    genre = genreToOneStyle(genre);
    final var hipHop = Pattern.compile("hip.*hop");
    final var altRock = Pattern.compile("(alt.*rock)");
    final var psychedelic = Pattern.compile("(?!psy.*rock)(psy.*delic)");
    final var loFi = Pattern.compile("(Lo-Fi)");
    final var gangsta = Pattern.compile("(gangs|gangz)(ta)");
    final var electronic = Pattern.compile("(electro)");
    genre = getString(genre, hipHop, altRock, psychedelic, loFi, gangsta, electronic);
    if (genre.contains("\u0000")) {
      genre = genre.replace("\u0000", "");
    }
    return genre;
  }

  private static String getString(
      String genre,
      final Pattern hipHop,
      final Pattern altRock,
      final Pattern psychedelic,
      final Pattern loFi,
      final Pattern gangsta,
      final Pattern electronic) {
    if (hipHop.matcher(genre.toLowerCase()).find()) {
      genre = "Hip-Hop";
    } else if (altRock.matcher(genre.toLowerCase()).find()) {
      genre = "Alternative Rock";
    } else if (psychedelic.matcher(genre.toLowerCase()).find()) {
      genre = "Psychedelic";
    } else if (gangsta.matcher(genre.toLowerCase()).find()) {
      genre = "Gangsta Rap";
    } else if (electronic.matcher(genre.toLowerCase()).find()) {
      genre = "Electronic";
    } else if (loFi.matcher(genre.toLowerCase()).find()) {
      genre = "Lo-Fi";
    }
    return genre;
  }

  /*need*/
  private static ListingWithFormat createFileListForEachDir(final String folderName) {
    /*creating filter for musical files*/
    final List<String> resultMp3 = new ArrayList<>();
    final List<String> resultFlac = new ArrayList<>();
    var result = new ListingWithFormat();
    listFiles(folderName, resultMp3, MP3);
    listFiles(folderName, resultFlac, FLAC);
    if (!resultMp3.isEmpty()) {
      result = ListingWithFormat.builder().format(MP_3_FORMAT).fileList(resultMp3).build();
    } else if (!resultFlac.isEmpty()) {
      result = ListingWithFormat.builder().format(FLAC_FORMAT).fileList(resultFlac).build();
    }
    return result;
  }

  private static void listFiles(
      final String folderName, final List<? super String> result, final String glob) {
    try (final var stream = Files.newDirectoryStream(Paths.get(folderName), glob)) {
      stream.forEach(o -> result.add(folderName + File.separator + o.getFileName()));
    } catch (final IOException e) {
      Logger.error(
          "Error listing directory: {} with a filter: {}, {}", folderName, FLAC, e.getMessage(), e);
    }
  }

  public static String findMostRepeatedGenre(final List<String> list) {
    final Map<String, Integer> stringsCount = new HashMap<>();
    for (final var string : list) {
      var counter = stringsCount.get(string);
      if (null == counter) counter = 0;
      counter++;
      stringsCount.put(string, counter);
    }
    Map.Entry<String, Integer> mostRepeated = null;
    for (final var e : stringsCount.entrySet()) {
      if (null == mostRepeated || mostRepeated.getValue() < e.getValue()) {
        mostRepeated = e;
      }
    }
    if (null != mostRepeated) return mostRepeated.getKey();
    else return null;
  }

  public static String extractNumber(final String str) {
    if (null == str || str.isEmpty()) return "";
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
}
