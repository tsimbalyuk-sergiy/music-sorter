package dev.tsvinc.music.sort;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.reference.GenreTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ProcessDirectory {

  private static final Logger log = LoggerFactory.getLogger(ProcessDirectory.class);
  public static final String MP_3_FORMAT = "mp3";
  public static final String FLAC_FORMAT = "flac";
  public static final String UNKNOWN = "UNKNOWN";

  private ProcessDirectory() {}

  public static final String MP3 = "*.mp3";
  public static final String FLAC = "*.flac";

  public static GenreWithFormat getGenreTag(final String path) {
    final ListingWithFormat fileListForEachDir = createFileListForEachDir(path);
    final List<String> genreList = new ArrayList<>();
    for (final String string : fileListForEachDir.getFileList()) {
      final File musicFile = new File(string);
      try {
        final AudioFile audioFile = AudioFileIO.read(musicFile);
        final Tag tag = audioFile.getTag();
        if (tag != null && !tag.isEmpty()) {
          final String genre = tag.getFirst(FieldKey.GENRE);
          /*check if genre is in numeric format e.g. (043)*/
          if (genre.matches(".*[0-9].*")) {
            /*if so -- extract numbers and get genre value for it*/
            final String genreNumericalConvert = extractNumber(genre);
            final String finalGenre =
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
        log.error("error reading file: {}\n{}", musicFile.getName(), e.getMessage(), e);
      }
    }
    final String mostRepeatedGenre = findMostRepeatedGenre(genreList);
    return GenreWithFormat.builder()
        .genre(mostRepeatedGenre)
        .format(fileListForEachDir.getFormat())
        .build();
  }

  public static String genreToOneStyle(final String genre) {
    final String[] words = genre.split("\\s");
    final List<String> o = new ArrayList<>();
    for (final String word : words) {
      final char[] chars = word.toCharArray();
      final char[] charsOut = new char[genre.length()];
      for (int i = 0, charsLength = chars.length; i < charsLength; i++) {
        if (i == 0) {
          charsOut[i] = Character.toUpperCase(chars[i]);
        } else {
          charsOut[i] = Character.toLowerCase(chars[i]);
        }
      }
      o.add(new String(charsOut));
    }
    final String output;
    if (o.size() > 1) {
      output = String.join(" ", o);
    } else {
      output = o.get(0);
    }
    return output;
  }

  public static String sanitizeGenre(String genre) {
    genre = genre.replaceAll("[^A-Za-z0-9\\-\\s&]+", "");
    genre = genreToOneStyle(genre);
    final Pattern hipHop = Pattern.compile("hip.*hop");
    final Pattern altRock = Pattern.compile("(alt.*rock)");
    final Pattern psychedelic = Pattern.compile("(?!psy.*rock)(psy.*delic)");
    final Pattern loFi = Pattern.compile("(lo-fi)");
    final Pattern gangsta = Pattern.compile("(gangs|gangz)(ta)");
    final Pattern electronic = Pattern.compile("(electro)");
    final Pattern disco = Pattern.compile("(disco)");
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
    } else if (disco.matcher(genre.toLowerCase()).find()) {
      genre = "Disco";
    }
    if (genre.contains("\u0000")) {
      genre = genre.replace("\u0000", "");
    }
    return genre;
  }
  /*need*/
  private static ListingWithFormat createFileListForEachDir(final String folderName) {
    /*creating filter for musical files*/
    final List<String> resultMp3 = new ArrayList<>();
    final List<String> resultFlac = new ArrayList<>();
    ListingWithFormat result = new ListingWithFormat();
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
      final String folderName, final List<String> result, final String glob) {
    try (final DirectoryStream<Path> stream =
        Files.newDirectoryStream(Paths.get(folderName), glob)) {
      stream.forEach(o -> result.add(folderName + File.separator + o.getFileName()));
    } catch (final IOException e) {
      log.error(
          "Error listing directory: {} with filter: {}, {}", folderName, FLAC, e.getMessage(), e);
    }
  }

  public static String findMostRepeatedGenre(final List<String> list) {
    final Map<String, Integer> stringsCount = new HashMap<>();
    for (final String string : list) {
      Integer counter = stringsCount.get(string);
      if (counter == null) counter = 0;
      counter++;
      stringsCount.put(string, counter);
    }
    Map.Entry<String, Integer> mostRepeated = null;
    for (final Map.Entry<String, Integer> e : stringsCount.entrySet()) {
      if (mostRepeated == null || mostRepeated.getValue() < e.getValue()) {
        mostRepeated = e;
      }
    }
    if (mostRepeated != null) return mostRepeated.getKey();
    else return "";
  }

  public static String extractNumber(final String str) {
    if (str == null || str.isEmpty()) return "";
    final StringBuilder sb = new StringBuilder();
    boolean found = false;
    final char[] charArray = str.toCharArray();
    for (final char c : charArray) {
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
