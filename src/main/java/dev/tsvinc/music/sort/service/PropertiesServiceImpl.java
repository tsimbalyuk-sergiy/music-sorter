package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.DB_DEFAULT_FILE_NAME;
import static dev.tsvinc.music.sort.util.Constants.DB_DEFAULT_PASSWORD;
import static dev.tsvinc.music.sort.util.Constants.DB_DEFAULT_USERNAME;
import static dev.tsvinc.music.sort.util.Constants.DB_LOCATION;
import static dev.tsvinc.music.sort.util.Constants.DB_PASSWORD;
import static dev.tsvinc.music.sort.util.Constants.DB_USERNAME;
import static dev.tsvinc.music.sort.util.Constants.ERROR_CREATING_DIRECTORY;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_PATTERNS;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_PATTERNS_DEFAULT;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_SKIP;
import static dev.tsvinc.music.sort.util.Constants.SORT_BY_ARTIST;
import static dev.tsvinc.music.sort.util.Constants.SOURCE_FOLDER;
import static dev.tsvinc.music.sort.util.Constants.TARGET_FOLDER;
import static org.pmw.tinylog.Logger.error;
import static org.pmw.tinylog.Logger.info;

import dev.tsvinc.music.sort.domain.AppProperties;
import dev.tsvinc.music.sort.domain.DbProperties;
import io.vavr.control.Try;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertiesServiceImpl implements PropertiesService {

  private static final String CONFIG_PATH =
      System.getProperty("user.home") + File.separator + ".config";
  private static final String APP_CONFIG_DIR_PATH = CONFIG_PATH + File.separator + "music-sorter";
  private final String appPropertiesLocation = appPropertiesLocation();
  private boolean skipLiveReleases = false;
  private boolean sortByArtist = false;
  private String dbLocation;
  private List<String> liveReleasesPatterns;

  private String sourceFolderValue;
  private String targetFolderValue;
  private String dbUsername;
  private String dbPassword;

  @Override
  public boolean initProperties() {
    final var configPathExists = new File(CONFIG_PATH).exists();
    final var appConfigPathExists = new File(APP_CONFIG_DIR_PATH).exists();
    if (!configPathExists) {
      Try.of(() -> Files.createDirectory(Paths.get(CONFIG_PATH)))
          .onFailure(e -> error(ERROR_CREATING_DIRECTORY, CONFIG_PATH, e.getMessage(), e));
    }
    if (!appConfigPathExists) {
      Try.of(() -> Files.createDirectory(Paths.get(APP_CONFIG_DIR_PATH)))
          .onFailure(e -> error(ERROR_CREATING_DIRECTORY, APP_CONFIG_DIR_PATH, e.getMessage(), e));
    }

    if (!Paths.get(appPropertiesLocation).toFile().exists()) {
      createPropertiesExample();
    }
    return fillProperties();
  }

  @Override
  public AppProperties getProperties() {
    if (!initProperties()) {
      error("Error loading properties");
      System.exit(-1);
    } else {
      return AppProperties.builder()
          .sourceFolder(sourceFolderValue)
          .targetFolder(targetFolderValue)
          .liveReleasesPatterns(liveReleasesPatterns)
          .skipLiveReleases(skipLiveReleases)
          .sortByArtist(sortByArtist)
          .dbProperties(
              DbProperties.builder()
                  .dbLocation(dbLocation)
                  .dbUsername(dbUsername)
                  .dbPassword(dbPassword)
                  .build())
          .build();
    }
    return null;
  }

  private void createPropertiesExample() {
    Try.of(
            () -> {
              Files.createFile(Paths.get(appPropertiesLocation));
              Files.write(
                  Paths.get(appPropertiesLocation),
                  Arrays.asList("source=", "target="),
                  StandardCharsets.UTF_8,
                  StandardOpenOption.APPEND);
              return null;
            })
        .onFailure(
            e ->
                error(
                    "Error writing to config file: {}, {}",
                    appPropertiesLocation,
                    e.getMessage(),
                    e));
  }

  private String appPropertiesLocation() {
    return APP_CONFIG_DIR_PATH + File.separator + "music-sorter.properties";
  }

  private boolean fillProperties() {
    var done = false;
    try (final InputStream input = new FileInputStream(appPropertiesLocation)) {
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
      /*GENERAL SORTING RULES*/
      skipLiveReleases =
          prop.containsKey(LIVE_RELEASES_SKIP)
              && Boolean.parseBoolean(prop.getProperty(LIVE_RELEASES_SKIP));
      sortByArtist =
          prop.containsKey(SORT_BY_ARTIST)
              && Boolean.parseBoolean(prop.getProperty(SORT_BY_ARTIST));
      if (skipLiveReleases && prop.containsKey(LIVE_RELEASES_PATTERNS)) {
        liveReleasesPatterns =
            Arrays.asList(prop.get(LIVE_RELEASES_PATTERNS).toString().split(","));
      } else {
        liveReleasesPatterns = LIVE_RELEASES_PATTERNS_DEFAULT;
        info("\"live_releases_patterns\" option is empty. using defaults: ");
        info("{}", LIVE_RELEASES_PATTERNS_DEFAULT);
      }

      /*DB*/
      dbLocation =
          prop.containsKey(DB_LOCATION)
              ? String.valueOf(prop.getProperty(DB_LOCATION))
              : APP_CONFIG_DIR_PATH + File.separator + DB_DEFAULT_FILE_NAME;
      dbUsername =
          prop.containsKey(DB_USERNAME) ? prop.getProperty(DB_USERNAME) : DB_DEFAULT_USERNAME;
      dbPassword =
          prop.containsKey(DB_PASSWORD) ? prop.getProperty(DB_PASSWORD) : DB_DEFAULT_PASSWORD;
    } catch (final IOException ex) {
      error("Error loading properties: {}", ex.getMessage(), ex);
    }
    return done;
  }
}
