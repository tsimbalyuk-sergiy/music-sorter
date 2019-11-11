package dev.tsvinc.music.sort;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class App {

  private static final String HOME_DIR = System.getProperty("user.home");
  private static String configPath;
  private static String appConfigDirPath;
  public static final String ERROR_CREATING_DIRECTORY = "Error creating directory: {}";
  private static String appPropertiesLocation;
  private static final String SOURCE_FOLDER = "source";
  private static final String TARGET_FOLDER = "target";
  private static String sourceFolderValue = null;
  private static String targetFolderValue = null;
  private static final String DATE_STAMP_SIMPLE = "yyyyMMdd";

  static {
    configPath = HOME_DIR + File.separator + ".config";
    appConfigDirPath = configPath + File.separator + "music-sorter";

    appPropertiesLocation = getAppPropertiesLocation();
    final LocalDateTime now = LocalDateTime.now();
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_STAMP_SIMPLE);
    final String format = now.format(formatter);
    String logFile = "music-sort_" + format + ".log";
    Configurator.defaultConfig()
        .writer(new FileWriter(logFile, true, true))
        .level(Level.INFO)
        .locale(Locale.US)
        .formatPattern(
            "{level}: {class}.{method}:{line} - {message}") /*https://tinylog.org/configuration*/
        .activate();
  }

  public static void main(String[] args) {
    if (!initProperties()) {
      Logger.error("Error loading properties");
      System.exit(0);
    }

    Set<String> folderList = albumsListing(sourceFolderValue);

    if (folderList.isEmpty()) {
      Logger.info("No data to work with. Exiting.");
      System.exit(1);
    }

    Logger.info("folders list created. size: {}", folderList.size());
    folderList.forEach(
        release -> {
          File releasePath = new File(release);
          Logger.info("Working on: {} :: {}", release, releasePath);
          moveRelease(release);
        });
  }

  private static void moveRelease(String sourceDirectory) {
    File source = new File(sourceDirectory);
    GenreWithFormat genreTag = ProcessDirectory.getGenreTag(sourceDirectory);
    String genre = ProcessDirectory.sanitizeGenre(genreTag.getGenre());
    String outWithFormat = targetFolderValue + File.separator + genreTag.getFormat();
    File outWithFormatDir = new File(outWithFormat);
    if (!outWithFormatDir.exists()) {
      createDirectory(outWithFormatDir);
    }

    String outWithGenreAndFormat = outWithFormat + File.separator + genre;
    File genreDir = new File(outWithGenreAndFormat);
    String finalDestination = outWithGenreAndFormat + File.separator;
    File destination = new File(finalDestination + source.getName());
    if (!genreDir.exists()) {
      createDirectory(genreDir);
    }
    if (!destination.exists()) {
      createDirectory(destination);
    }
    try (DirectoryStream<Path> directoryStream =
        Files.newDirectoryStream(Paths.get(sourceDirectory))) {
      directoryStream.forEach(
          src -> {
            Path dest = destination.toPath().resolve(src.getFileName());
            try {
              Files.move(src, dest, REPLACE_EXISTING, ATOMIC_MOVE);
            } catch (IOException e) {
              Logger.error("Failed to move file: {}", e.getMessage(), e);
            }
          });
      Files.deleteIfExists(source.toPath());
    } catch (IOException e) {
      Logger.error("Failed to move directory: {}", sourceDirectory, e.getMessage(), e);
    }
  }

  private static void createDirectory(File destination) {
    try {
      Files.createDirectories(destination.toPath());
    } catch (IOException e) {
      Logger.error("Failed to create directory: {}", e.getMessage(), e);
    }
  }

  private static Set<String> albumsListing(String sourceDirectory) {
    Set<String> dirs = new HashSet<>();
    try (Stream<Path> stream = Files.walk(Paths.get(sourceDirectory), Integer.MAX_VALUE)) {
      stream
          .filter(
              path ->
                  path.getFileName().toString().contains(".mp3")
                      || path.getFileName().toString().contains(".flac"))
          .collect(Collectors.toSet())
          .forEach(o -> dirs.add(o.getParent().toString()));
    } catch (IOException e) {
      Logger.error("Error while walking directory: {}", sourceDirectory, e.getMessage(), e);
    }
    return dirs;
  }

  private static boolean initProperties() {
    boolean done = false;
    boolean configPathExists = new File(configPath).exists();
    boolean appConfigPathExists = new File(appConfigDirPath).exists();
    if (!configPathExists) {
      try {
        Files.createDirectory(Paths.get(configPath));
      } catch (IOException e) {
        Logger.error(ERROR_CREATING_DIRECTORY, configPath, e.getMessage(), e);
      }
    }
    if (!appConfigPathExists)
      try {
        Files.createDirectory(Paths.get(appConfigDirPath));
      } catch (IOException e) {
        Logger.error(ERROR_CREATING_DIRECTORY, appConfigDirPath, e.getMessage(), e);
      }

    if (!Paths.get(appPropertiesLocation).toFile().exists()) {
      Charset utf8 = StandardCharsets.UTF_8;
      List<String> list = Arrays.asList("source=", "target=");
      try {
        Files.createFile(Paths.get(appPropertiesLocation));
        Files.write(Paths.get(appPropertiesLocation), list, utf8, StandardOpenOption.APPEND);
      } catch (IOException e) {
        Logger.error("Error writing to config file: {}", appPropertiesLocation, e.getMessage(), e);
      }
    }
    try (InputStream input = new FileInputStream(appPropertiesLocation)) {
      Properties prop = new Properties();
      prop.load(input);
      sourceFolderValue = prop.getProperty(SOURCE_FOLDER);
      targetFolderValue = prop.getProperty(TARGET_FOLDER);
      if (sourceFolderValue != null
          && !sourceFolderValue.isEmpty()
          && targetFolderValue != null
          && !targetFolderValue.isEmpty()) {
        done = true;
      }
    } catch (IOException ex) {
      Logger.error("Error loading properties: {}", ex.getMessage(), ex);
    }
    return done;
  }

  private static String getAppPropertiesLocation() {
    return appConfigDirPath + File.separator + "music-sorter.properties";
  }
}
