package dev.tsvinc.music.sort;

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
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.reference.GenreTypes;
import org.pmw.tinylog.Logger;

public class ProcessDirectory {

  public static final String MP_3_FORMAT = "mp3";
  public static final String FLAC_FORMAT = "flac";

  private ProcessDirectory() {}

  public static final String MP3 = "*.mp3";
  public static final String FLAC = "*.flac";

  public static GenreWithFormat getGenreTag(String path) {
    ListingWithFormat fileListForEachDir = createFileListForEachDir(path);
    List<String> genreList = new ArrayList<>();
    for (String string : fileListForEachDir.getFileList()) {
      File musicFile = new File(string);
      try {
        AudioFile audioFile = AudioFileIO.read(musicFile);
        Tag tag = audioFile.getTag();
        String genre = tag.getFirst(FieldKey.GENRE);
        /*check if genre is in numeric format e.g. (043)*/
        if (genre.matches(".*[0-9].*")) {
          /*if so -- extract numbers and get genre value for it*/
          String genreNumericalConvert = extractNumber(genre);
          String finalGenre =
              GenreTypes.getInstanceOf().getValueForId(Integer.parseInt(genreNumericalConvert));
          genreList.add(finalGenre);
        } else {
          genreList.add(genre);
        }
      } catch (IOException
          | CannotReadException
          | ReadOnlyFileException
          | TagException
          | InvalidAudioFrameException e) {
        Logger.error("error reading file: {}\n{}", musicFile.getName(), e.getMessage(), e);
      }
    }
    String mostRepeatedGenre = findMostRepeatedGenre(genreList);
    return GenreWithFormat.builder()
        .genre(mostRepeatedGenre)
        .format(fileListForEachDir.getFormat())
        .build();
  }

  public static String genreToOneStyle(String genre) {
    String[] words = genre.split("\\s");
    List<String> o = new ArrayList<>();
    for (String word : words) {
      char[] chars = word.toCharArray();
      char[] charsOut = new char[genre.length()];
      for (int i = 0, charsLength = chars.length; i < charsLength; i++) {
        if (i == 0) {
          charsOut[i] = Character.toUpperCase(chars[i]);
        } else {
          charsOut[i] = Character.toLowerCase(chars[i]);
        }
      }
      o.add(new String(charsOut));
    }
    String output;
    if (o.size() > 1) {
      output = String.join(" ", o);
    } else {
      output = o.get(0);
    }
    return output;
  }

  public static String sanitizeGenre(String genre) {
    genre = genre.replaceAll("[^A-Za-z0-9\\-\\s]+", "");
    genre = genreToOneStyle(genre);
    Pattern hipHop = Pattern.compile("hip.*hop");
    Pattern altRock = Pattern.compile("(alt.*rock)");
    Pattern psychedelic = Pattern.compile("(?!psy.*rock)(psy.*delic)");
    Pattern gangsta = Pattern.compile("(gangs|gangz)(ta)");
    Pattern electronic = Pattern.compile("(electro)");
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
    }
    if (genre.contains("\u0000")) {
      genre = genre.replace("\u0000", "");
    }
    return genre;
  }
  /*need*/
  private static ListingWithFormat createFileListForEachDir(String folderName) {
    /*creating filter for musical files*/
    List<String> resultMp3 = new ArrayList<>();
    List<String> resultFlac = new ArrayList<>();
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

  private static void listFiles(String folderName, List<String> result, String glob) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderName), glob)) {
      stream.forEach(o -> result.add(folderName + File.separator + o.getFileName()));
    } catch (IOException e) {
      Logger.error(
          "Error listing directory: {} with filter: {}", folderName, FLAC, e.getMessage(), e);
    }
  }

  public static String findMostRepeatedGenre(List<String> list) {
    Map<String, Integer> stringsCount = new HashMap<>();
    for (String string : list) {
      Integer counter = stringsCount.get(string);
      if (counter == null) counter = 0;
      counter++;
      stringsCount.put(string, counter);
    }
    Map.Entry<String, Integer> mostRepeated = null;
    for (Map.Entry<String, Integer> e : stringsCount.entrySet()) {
      if (mostRepeated == null || mostRepeated.getValue() < e.getValue()) {
        mostRepeated = e;
      }
    }
    if (mostRepeated != null) return mostRepeated.getKey();
    else return null;
  }

  public static String extractNumber(final String str) {
    if (str == null || str.isEmpty()) return "";
    StringBuilder sb = new StringBuilder();
    boolean found = false;
    char[] charArray = str.toCharArray();
    for (char c : charArray) {
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
