package dev.tsvinc.music.sort;

import static dev.tsvinc.music.sort.Constants.ERROR_CREATING_DIRECTORY;
import static dev.tsvinc.music.sort.Constants.LIVE_RELEASES_PATTERNS;
import static dev.tsvinc.music.sort.Constants.LIVE_RELEASES_SKIP;
import static dev.tsvinc.music.sort.Constants.SOURCE_FOLDER;
import static dev.tsvinc.music.sort.Constants.TARGET_FOLDER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;

public class App {
  private static final String HOME_DIR = System.getProperty("user.home");
  private static final String CONFIG_PATH;
  private static final String APP_CONFIG_DIR_PATH;
  private static final String APP_PROPERTIES_LOCATION;
  private static String sourceFolderValue;
  private static String targetFolderValue;
  private static boolean skipLiveReleases = false;
  private static List<String> liveReleasesPatterns;
  private static final Predicate<Path> IS_MUSIC_FILE =
      path ->
          path.getFileName().toString().contains(".mp3")
              || path.getFileName().toString().contains(".flac");

  static {
    java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.SEVERE);

    CONFIG_PATH = HOME_DIR + File.separator + ".config";
    APP_CONFIG_DIR_PATH = CONFIG_PATH + File.separator + "music-sorter";
    APP_PROPERTIES_LOCATION = appPropertiesLocation();

    org.pmw.tinylog.Configurator.defaultConfig()
        .writer(new ConsoleWriter())
        .level(org.pmw.tinylog.Level.INFO)
        .locale(Locale.US)
        .formatPattern(
            "{level}: {class}.{method}:{line} - {message}") /*https://tinylog.org/configuration*/
        .activate();
  }

  public static void main(final String[] args) {
    if (!initProperties()) {
      Logger.error("Error loading properties");
      System.exit(0);
    }

    final var folderList = albumsListing(sourceFolderValue);

    if (folderList.isEmpty()) {
      Logger.info("No data to work with. Exiting.");
      System.exit(1);
    }

    Logger.info("folders list created. size: {}", folderList.size());
    folderList.forEach(
        release -> {
          final var releasePath = new File(release);
          Logger.info("Working on: {} :: {}", releasePath.getName(), release);
          moveRelease(release);
        });
    Logger.info("Finished. Cleaning empty directories ...");
    if (folderList.isEmpty()) {
      cleanUpParentDirectory(sourceFolderValue);
      System.exit(0);
    }
  }

  private static void moveRelease(final String sourceDirectory) {
    final var source = new File(sourceDirectory);
    final var genreTag = ProcessDirectory.getGenreTag(sourceDirectory);
    final var genre = ProcessDirectory.sanitizeGenre(genreTag.getGenre());
    final var outWithFormat = targetFolderValue + File.separator + genreTag.getFormat();
    final var outWithFormatDir = new File(outWithFormat);
    if (!outWithFormatDir.exists()) {
      createDirectory(outWithFormatDir);
    }

    final var outWithGenreAndFormat = outWithFormat + File.separator + genre;
    final var genreDir = new File(outWithGenreAndFormat);
    final var finalDestination = outWithGenreAndFormat + File.separator;
    final var destination = new File(finalDestination + source.getName());
    if (!genreDir.exists()) {
      createDirectory(genreDir);
    }
    if (!destination.exists()) {
      createDirectory(destination);
    }
    try (final var directoryStream = Files.newDirectoryStream(Paths.get(sourceDirectory))) {
      directoryStream.forEach(
          src -> {
            final var dest = destination.toPath().resolve(src.getFileName());
            try {
              Files.move(src, dest, REPLACE_EXISTING);
            } catch (final IOException e) {
              Logger.error("Failed to move file: {}", e.getMessage(), e);
            }
          });
      Files.deleteIfExists(source.toPath());
    } catch (final IOException e) {
      Logger.error("Failed to move directory: {}, {}", sourceDirectory, e.getMessage(), e);
    }
  }

  private static void cleanUpParentDirectory(final String sourceDirectory) {
    while (!directoryIsEmpty(Paths.get(sourceDirectory))) {
      try (final var stream = Files.walk(Paths.get(sourceDirectory), Integer.MAX_VALUE)) {
        stream
            .filter(path -> path.toFile().isDirectory())
            .filter(App::directoryIsEmpty)
            .collect(Collectors.toList())
            .forEach(
                dir -> {
                  try {
                    Files.delete(dir);
                  } catch (final IOException ioe) {
                    Logger.error("Error deleting folder: {} {}\n{}", dir, ioe.getMessage(), ioe);
                  }
                });

      } catch (final IOException e) {
        Logger.error("Error while walking directory: {}, {}", sourceDirectory, e.getMessage(), e);
      }
    }
  }

  private static boolean directoryIsEmpty(final Path directory) {
    var result = false;
    try (final var dirStream = Files.newDirectoryStream(directory)) {
      result = !dirStream.iterator().hasNext();
    } catch (final IOException e) {
      Logger.error(
          "Error checking if directory is empty: {}, {}, {}", directory, e.getMessage(), e);
    }
    return result;
  }

  private static void createDirectory(final File destination) {
    try {
      Files.createDirectories(destination.toPath());
    } catch (final IOException e) {
      Logger.error("Failed to create a directory: {}", e.getMessage(), e);
    }
  }

  private static Set<String> albumsListing(final String sourceDirectory) {
    final Set<String> dirs = new HashSet<>();
    Set<String> cleanedDirectories = new HashSet<>();
    try (final var stream = Files.walk(Paths.get(sourceDirectory), Integer.MAX_VALUE)) {
      stream
          .filter(IS_MUSIC_FILE)
          .collect(Collectors.toSet())
          .forEach(o -> dirs.add(o.getParent().toString()));
      if (skipLiveReleases) {
        cleanedDirectories =
            dirs.stream().filter(App::isNotLiveRelease).collect(Collectors.toSet());
      } else {
        cleanedDirectories = dirs;
      }
    } catch (final IOException e) {
      Logger.error("Error while walking directory: {}, {}", sourceDirectory, e.getMessage(), e);
    }
    return cleanedDirectories;
  }

  private static boolean isNotLiveRelease(String folderName) {
    return liveReleasesPatterns.parallelStream().noneMatch(folderName::contains);
  }

  private static boolean initProperties() {
    final var configPathExists = new File(CONFIG_PATH).exists();
    final var appConfigPathExists = new File(APP_CONFIG_DIR_PATH).exists();
    if (!configPathExists) {
      try {
        Files.createDirectory(Paths.get(CONFIG_PATH));
      } catch (final IOException e) {
        Logger.error(ERROR_CREATING_DIRECTORY, CONFIG_PATH, e.getMessage(), e);
      }
    }
    if (!appConfigPathExists)
      try {
        Files.createDirectory(Paths.get(APP_CONFIG_DIR_PATH));
      } catch (final IOException e) {
        Logger.error(ERROR_CREATING_DIRECTORY, APP_CONFIG_DIR_PATH, e.getMessage(), e);
      }

    if (!Paths.get(APP_PROPERTIES_LOCATION).toFile().exists()) {
      final var utf8 = StandardCharsets.UTF_8;
      final var list = Arrays.asList("source=", "target=");
      try {
        Files.createFile(Paths.get(APP_PROPERTIES_LOCATION));
        Files.write(Paths.get(APP_PROPERTIES_LOCATION), list, utf8, StandardOpenOption.APPEND);
      } catch (final IOException e) {
        Logger.error(
            "Error writing to config file: {}, {}", APP_PROPERTIES_LOCATION, e.getMessage(), e);
      }
    }
    return isDone();
  }

  private static boolean isDone() {
    var done = false;
    try (final InputStream input = new FileInputStream(APP_PROPERTIES_LOCATION)) {
      final var prop = new Properties();
      prop.load(input);
      sourceFolderValue = prop.getProperty(SOURCE_FOLDER);
      targetFolderValue = prop.getProperty(TARGET_FOLDER);
      if (null != sourceFolderValue
          && !sourceFolderValue.isEmpty()
          && null != targetFolderValue
          && !targetFolderValue.isEmpty()) {
        done = true;
      }
      skipLiveReleases =
          prop.containsKey(LIVE_RELEASES_SKIP)
              && Boolean.parseBoolean(prop.getProperty(LIVE_RELEASES_SKIP));
      if (skipLiveReleases && prop.containsKey(LIVE_RELEASES_PATTERNS)) {
        liveReleasesPatterns =
            Arrays.asList(prop.get(LIVE_RELEASES_PATTERNS).toString().split(","));
      } else {
        Logger.error("\"live_releases_patterns\" option is empty.");
      }
    } catch (final IOException ex) {
      Logger.error("Error loading properties: {}", ex.getMessage(), ex);
    }
    return done;
  }

  private static String appPropertiesLocation() {
    return APP_CONFIG_DIR_PATH + File.separator + "music-sorter.properties";
  }
}
