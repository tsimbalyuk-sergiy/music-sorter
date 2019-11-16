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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class App {
  private static final Logger log = LoggerFactory.getLogger(App.class);

  private static final String HOME_DIR = System.getProperty("user.home");
  private static String configPath;
  private static String appConfigDirPath;
  public static final String ERROR_CREATING_DIRECTORY = "Error creating directory: {}, {}";
  private static String appPropertiesLocation;
  private static final String SOURCE_FOLDER = "source";
  private static final String TARGET_FOLDER = "target";
  private static String sourceFolderValue = null;
  private static String targetFolderValue = null;

  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    configPath = HOME_DIR + File.separator + ".config";
    appConfigDirPath = configPath + File.separator + "music-sorter";
    appPropertiesLocation = getAppPropertiesLocation();
  }

  public static void main(String[] args) {
    if (!initProperties()) {
      log.error("Error loading properties");
      System.exit(0);
    }

    Set<String> folderList = albumsListing(sourceFolderValue);

    if (folderList.isEmpty()) {
      log.info("No data to work with. Exiting.");
      System.exit(1);
    }

    log.info("folders list created. size: {}", folderList.size());
    folderList.forEach(
        release -> {
          File releasePath = new File(release);
          log.info("Working on: {} :: {}", releasePath.getName(), release);
          moveRelease(release);
        });
    cleanUpParentDirectory(sourceFolderValue);
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
              Files.move(src, dest, REPLACE_EXISTING);
            } catch (IOException e) {
              log.error("Failed to move file: {}", e.getMessage(), e);
            }
          });
      Files.deleteIfExists(source.toPath());
    } catch (IOException e) {
      log.error("Failed to move directory: {}, {}", sourceDirectory, e.getMessage(), e);
    }
  }

  private static void cleanUpParentDirectory(String sourceDirectory) {
    while (!directoryIsEmpty(Paths.get(sourceDirectory))) {
      try (Stream<Path> stream = Files.walk(Paths.get(sourceDirectory), Integer.MAX_VALUE)) {
        stream
            .filter(path -> path.toFile().isDirectory())
            .filter(App::directoryIsEmpty)
            .collect(Collectors.toList())
            .forEach(
                dir -> {
                  try {
                    Files.delete(dir);
                  } catch (IOException ioe) {
                    log.error("Error deleting folder: {} {}\n{}", dir, ioe.getMessage(), ioe);
                  }
                });

      } catch (IOException e) {
        log.error("Error while walking directory: {}, {}", sourceDirectory, e.getMessage(), e);
      }
    }
  }

  private static boolean directoryIsEmpty(final Path directory) {
    boolean result = false;
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
      result = !dirStream.iterator().hasNext();
    } catch (IOException e) {
      log.error("Error checking if directory is empty: {}, {}, {}", directory, e.getMessage(), e);
    }
    return result;
  }

  private static void createDirectory(File destination) {
    try {
      Files.createDirectories(destination.toPath());
    } catch (IOException e) {
      log.error("Failed to create directory: {}", e.getMessage(), e);
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
      log.error("Error while walking directory: {}, {}", sourceDirectory, e.getMessage(), e);
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
        log.error(ERROR_CREATING_DIRECTORY, configPath, e.getMessage(), e);
      }
    }
    if (!appConfigPathExists)
      try {
        Files.createDirectory(Paths.get(appConfigDirPath));
      } catch (IOException e) {
        log.error(ERROR_CREATING_DIRECTORY, appConfigDirPath, e.getMessage(), e);
      }

    if (!Paths.get(appPropertiesLocation).toFile().exists()) {
      Charset utf8 = StandardCharsets.UTF_8;
      List<String> list = Arrays.asList("source=", "target=");
      try {
        Files.createFile(Paths.get(appPropertiesLocation));
        Files.write(Paths.get(appPropertiesLocation), list, utf8, StandardOpenOption.APPEND);
      } catch (IOException e) {
        log.error("Error writing to config file: {}, {}", appPropertiesLocation, e.getMessage(), e);
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
      log.error("Error loading properties: {}", ex.getMessage(), ex);
    }
    return done;
  }

  private static String getAppPropertiesLocation() {
    return appConfigDirPath + File.separator + "music-sorter.properties";
  }
}
