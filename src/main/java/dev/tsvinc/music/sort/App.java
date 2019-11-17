package dev.tsvinc.music.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class App {
  private static final Logger log = LoggerFactory.getLogger(App.class);

  private static final String HOME_DIR = System.getProperty("user.home");
  private static final String CONFIG_PATH;
  private static final String APP_CONFIG_DIR_PATH;
  private static final String ERROR_CREATING_DIRECTORY = "Error creating directory: {}, {}";
  private static final String APP_PROPERTIES_LOCATION;
  private static final String SOURCE_FOLDER = "source";
  private static final String TARGET_FOLDER = "target";
  private static String sourceFolderValue = null;
  private static String targetFolderValue = null;

  static {
    java.util.logging.Logger.getLogger("org.jaudiotagger.tag.id3").setLevel(Level.SEVERE);
    java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.SEVERE);
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    CONFIG_PATH = HOME_DIR + File.separator + ".config";
    APP_CONFIG_DIR_PATH = CONFIG_PATH + File.separator + "music-sorter";
    APP_PROPERTIES_LOCATION = getAppPropertiesLocation();
  }

  public static void main(final String[] args) {
    if (!initProperties()) {
      log.error("Error loading properties");
      System.exit(0);
    }

    final Set<String> folderList = albumsListing(sourceFolderValue);

    if (folderList.isEmpty()) {
      log.info("No data to work with. Exiting.");
      System.exit(1);
    }

    log.info("folders list created. size: {}", folderList.size());
    folderList.forEach(
        release -> {
          final File releasePath = new File(release);
          log.info("Working on: {} :: {}", releasePath.getName(), release);
          moveRelease(release);
        });
    cleanUpParentDirectory(sourceFolderValue);
  }

  private static void moveRelease(final String sourceDirectory) {
    final File source = new File(sourceDirectory);
    final GenreWithFormat genreTag = ProcessDirectory.getGenreTag(sourceDirectory);
    final String genre = ProcessDirectory.sanitizeGenre(genreTag.getGenre());
    final String outWithFormat = targetFolderValue + File.separator + genreTag.getFormat();
    final File outWithFormatDir = new File(outWithFormat);
    if (!outWithFormatDir.exists()) {
      createDirectory(outWithFormatDir);
    }

    final String outWithGenreAndFormat = outWithFormat + File.separator + genre;
    final File genreDir = new File(outWithGenreAndFormat);
    final String finalDestination = outWithGenreAndFormat + File.separator;
    final File destination = new File(finalDestination + source.getName());
    if (!genreDir.exists()) {
      createDirectory(genreDir);
    }
    if (!destination.exists()) {
      createDirectory(destination);
    }
    try (final DirectoryStream<Path> directoryStream =
        Files.newDirectoryStream(Paths.get(sourceDirectory))) {
      directoryStream.forEach(
          src -> {
            final Path dest = destination.toPath().resolve(src.getFileName());
            try {
              Files.move(src, dest, REPLACE_EXISTING);
            } catch (final IOException e) {
              log.error("Failed to move file: {}", e.getMessage(), e);
            }
          });
      Files.deleteIfExists(source.toPath());
    } catch (final IOException e) {
      log.error("Failed to move directory: {}, {}", sourceDirectory, e.getMessage(), e);
    }
  }

  private static void cleanUpParentDirectory(final String sourceDirectory) {
    while (!directoryIsEmpty(Paths.get(sourceDirectory))) {
      try (final Stream<Path> stream = Files.walk(Paths.get(sourceDirectory), Integer.MAX_VALUE)) {
        stream
            .filter(path -> path.toFile().isDirectory())
            .filter(App::directoryIsEmpty)
            .collect(Collectors.toList())
            .forEach(
                dir -> {
                  try {
                    Files.delete(dir);
                  } catch (final IOException ioe) {
                    log.error("Error deleting folder: {} {}\n{}", dir, ioe.getMessage(), ioe);
                  }
                });

      } catch (final IOException e) {
        log.error("Error while walking directory: {}, {}", sourceDirectory, e.getMessage(), e);
      }
    }
  }

  private static boolean directoryIsEmpty(final Path directory) {
    boolean result = false;
    try (final DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
      result = !dirStream.iterator().hasNext();
    } catch (final IOException e) {
      log.error("Error checking if directory is empty: {}, {}, {}", directory, e.getMessage(), e);
    }
    return result;
  }

  private static void createDirectory(final File destination) {
    try {
      Files.createDirectories(destination.toPath());
    } catch (final IOException e) {
      log.error("Failed to create directory: {}", e.getMessage(), e);
    }
  }

  private static Set<String> albumsListing(final String sourceDirectory) {
    final Set<String> dirs = new HashSet<>();
    try (final Stream<Path> stream = Files.walk(Paths.get(sourceDirectory), Integer.MAX_VALUE)) {
      stream
          .filter(
              path ->
                  path.getFileName().toString().contains(".mp3")
                      || path.getFileName().toString().contains(".flac"))
          .collect(Collectors.toSet())
          .forEach(o -> dirs.add(o.getParent().toString()));
    } catch (final IOException e) {
      log.error("Error while walking directory: {}, {}", sourceDirectory, e.getMessage(), e);
    }
    return dirs;
  }

  private static boolean initProperties() {
    boolean done = false;
    final boolean configPathExists = new File(CONFIG_PATH).exists();
    final boolean appConfigPathExists = new File(APP_CONFIG_DIR_PATH).exists();
    if (!configPathExists) {
      try {
        Files.createDirectory(Paths.get(CONFIG_PATH));
      } catch (final IOException e) {
        log.error(ERROR_CREATING_DIRECTORY, CONFIG_PATH, e.getMessage(), e);
      }
    }
    if (!appConfigPathExists)
      try {
        Files.createDirectory(Paths.get(APP_CONFIG_DIR_PATH));
      } catch (final IOException e) {
        log.error(ERROR_CREATING_DIRECTORY, APP_CONFIG_DIR_PATH, e.getMessage(), e);
      }

    if (!Paths.get(APP_PROPERTIES_LOCATION).toFile().exists()) {
      final Charset utf8 = StandardCharsets.UTF_8;
      final List<String> list = Arrays.asList("source=", "target=");
      try {
        Files.createFile(Paths.get(APP_PROPERTIES_LOCATION));
        Files.write(Paths.get(APP_PROPERTIES_LOCATION), list, utf8, StandardOpenOption.APPEND);
      } catch (final IOException e) {
        log.error(
            "Error writing to config file: {}, {}", APP_PROPERTIES_LOCATION, e.getMessage(), e);
      }
    }
    try (final InputStream input = new FileInputStream(APP_PROPERTIES_LOCATION)) {
      final Properties prop = new Properties();
      prop.load(input);
      sourceFolderValue = prop.getProperty(SOURCE_FOLDER);
      targetFolderValue = prop.getProperty(TARGET_FOLDER);
      if (sourceFolderValue != null
          && !sourceFolderValue.isEmpty()
          && targetFolderValue != null
          && !targetFolderValue.isEmpty()) {
        done = true;
      }
    } catch (final IOException ex) {
      log.error("Error loading properties: {}", ex.getMessage(), ex);
    }
    return done;
  }

  private static String getAppPropertiesLocation() {
    return APP_CONFIG_DIR_PATH + File.separator + "music-sorter.properties";
  }
}
