package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.APP_CONFIG_DIR_PATH;
import static dev.tsvinc.music.sort.util.Constants.CONFIG_PATH;
import static dev.tsvinc.music.sort.util.Constants.ERROR_CREATING_DIRECTORY;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_PATTERNS;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_PATTERNS_DEFAULT;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_SKIP;
import static dev.tsvinc.music.sort.util.Constants.SORT_BY_ARTIST;
import static dev.tsvinc.music.sort.util.Constants.SOURCE_FOLDER;
import static dev.tsvinc.music.sort.util.Constants.TARGET_FOLDER;
import static org.tinylog.Logger.error;
import static org.tinylog.Logger.info;

import dev.tsvinc.music.sort.domain.AppProperties;
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

public class PropertiesService {

    private static PropertiesService propertiesServiceInstance;
    private final String appPropertiesLocation = PropertiesService.appPropertiesLocation();
    private boolean skipLiveReleases;
    private boolean sortByArtist;

    private List<String> liveReleasesPatterns;

    private String sourceFolderValue;
    private String targetFolderValue;

    private PropertiesService() {}

    public static PropertiesService getInstance() {
        if (propertiesServiceInstance == null) {
            propertiesServiceInstance = new PropertiesService();
        }
        return propertiesServiceInstance;
    }

    private static String appPropertiesLocation() {
        return APP_CONFIG_DIR_PATH + File.separator + "music-sorter.properties";
    }

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

        if (!Paths.get(this.appPropertiesLocation).toFile().exists()) {
            this.createPropertiesExample();
        }
        return this.fillProperties();
    }

    public AppProperties getProperties() {
        if (!this.initProperties()) {
            error("Error loading properties");
            throw new UnsupportedOperationException("Error loading properties");
        } else {
            return new AppProperties(
                    this.sourceFolderValue,
                    this.targetFolderValue,
                    this.sortByArtist,
                    this.skipLiveReleases,
                    this.liveReleasesPatterns);
        }
    }

    private void createPropertiesExample() {
        Try.of(() -> {
                    Files.createFile(Paths.get(this.appPropertiesLocation));
                    Files.write(
                            Paths.get(this.appPropertiesLocation),
                            Arrays.asList("source=", "target="),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.APPEND);
                    return null;
                })
                .onFailure(e ->
                        error("Error writing to config file: {}, {}", this.appPropertiesLocation, e.getMessage(), e));
    }

    private boolean fillProperties() {
        var done = false;
        try (final InputStream input = new FileInputStream(this.appPropertiesLocation)) {
            final var prop = new Properties();
            prop.load(input);
            this.sourceFolderValue = prop.getProperty(SOURCE_FOLDER);
            this.targetFolderValue = prop.getProperty(TARGET_FOLDER);
            if (null != this.sourceFolderValue
                    && !this.sourceFolderValue.isEmpty()
                    && null != this.targetFolderValue
                    && !this.targetFolderValue.isEmpty()) {
                done = true;
            }
            /*GENERAL SORTING RULES*/
            this.skipLiveReleases =
                    prop.containsKey(LIVE_RELEASES_SKIP) && Boolean.parseBoolean(prop.getProperty(LIVE_RELEASES_SKIP));
            this.sortByArtist =
                    prop.containsKey(SORT_BY_ARTIST) && Boolean.parseBoolean(prop.getProperty(SORT_BY_ARTIST));
            if (this.skipLiveReleases && prop.containsKey(LIVE_RELEASES_PATTERNS)) {
                this.liveReleasesPatterns = Arrays.asList(
                        prop.get(LIVE_RELEASES_PATTERNS).toString().split(","));
            } else {
                this.liveReleasesPatterns = LIVE_RELEASES_PATTERNS_DEFAULT;
                info("\"live_releases_patterns\" option is empty. using defaults: ");
                info("{}", LIVE_RELEASES_PATTERNS_DEFAULT);
            }
        } catch (final IOException ex) {
            error("Error loading properties: {}", ex.getMessage(), ex);
        }
        return done;
    }
}
