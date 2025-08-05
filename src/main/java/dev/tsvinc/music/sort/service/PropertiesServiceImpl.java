package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.CHECKSUM_VALIDATION_ENABLED;
import static dev.tsvinc.music.sort.util.Constants.ERROR_CREATING_DIRECTORY;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_PATTERNS;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_PATTERNS_DEFAULT;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_SKIP;
import static dev.tsvinc.music.sort.util.Constants.SORT_BY_ARTIST;
import static dev.tsvinc.music.sort.util.Constants.SOURCE_FOLDER;
import static dev.tsvinc.music.sort.util.Constants.TARGET_FOLDER;
import static org.tinylog.Logger.error;
import static org.tinylog.Logger.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Properties;

import dev.tsvinc.music.sort.domain.AppProperties;

import io.vavr.collection.List;
import io.vavr.control.Try;

public class PropertiesServiceImpl implements PropertiesService {

    private static final String CONFIG_PATH = System.getProperty("user.home") + File.separator + ".config";
    private static final String APP_CONFIG_DIR_PATH =
            PropertiesServiceImpl.CONFIG_PATH + File.separator + "music-sorter";
    private final String appPropertiesLocation = PropertiesServiceImpl.appPropertiesLocation();
    private boolean skipLiveReleases;
    private boolean sortByArtist;
    private boolean checksumValidationEnabled;

    private List<String> liveReleasesPatterns;

    private String sourceFolderValue;
    private String targetFolderValue;

    @Override
    public boolean initProperties() {
        final var configPathExists = new File(PropertiesServiceImpl.CONFIG_PATH).exists();
        final var appConfigPathExists = new File(PropertiesServiceImpl.APP_CONFIG_DIR_PATH).exists();
        if (!configPathExists) {
            Try.of(() -> Files.createDirectory(Paths.get(PropertiesServiceImpl.CONFIG_PATH)))
                    .onFailure(
                            e -> error(ERROR_CREATING_DIRECTORY, PropertiesServiceImpl.CONFIG_PATH, e.getMessage(), e));
        }
        if (!appConfigPathExists) {
            Try.of(() -> Files.createDirectory(Paths.get(PropertiesServiceImpl.APP_CONFIG_DIR_PATH)))
                    .onFailure(e -> error(
                            ERROR_CREATING_DIRECTORY, PropertiesServiceImpl.APP_CONFIG_DIR_PATH, e.getMessage(), e));
        }

        if (!Paths.get(this.appPropertiesLocation).toFile().exists()) {
            this.createPropertiesExample();
        }
        return this.fillProperties();
    }

    @Override
    public AppProperties getProperties() {
        if (!this.initProperties()) {
            error("[ERROR] Failed to load configuration. Please check your settings file.");
            System.exit(-1);
        } else {
            return new AppProperties(
                    this.sourceFolderValue,
                    this.targetFolderValue,
                    this.sortByArtist,
                    this.skipLiveReleases,
                    this.liveReleasesPatterns,
                    this.checksumValidationEnabled);
        }
        return null;
    }

    private void createPropertiesExample() {
        Try.of(() -> {
                    Files.createFile(Paths.get(this.appPropertiesLocation));
                    Files.write(
                            Paths.get(this.appPropertiesLocation),
                            Arrays.asList(
                                    "source=",
                                    "target=",
                                    "# Optional: Enable checksum validation (disabled by default)",
                                    "checksum_validation_enabled=false"),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.APPEND);
                    return null;
                })
                .onFailure(e ->
                        error("Error writing to config file: {}, {}", this.appPropertiesLocation, e.getMessage(), e));
    }

    private static String appPropertiesLocation() {
        return PropertiesServiceImpl.APP_CONFIG_DIR_PATH + File.separator + "music-sorter.properties";
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
            this.checksumValidationEnabled = prop.containsKey(CHECKSUM_VALIDATION_ENABLED)
                    && Boolean.parseBoolean(prop.getProperty(CHECKSUM_VALIDATION_ENABLED));
            if (this.skipLiveReleases && prop.containsKey(LIVE_RELEASES_PATTERNS)) {
                this.liveReleasesPatterns =
                        List.of(prop.get(LIVE_RELEASES_PATTERNS).toString().split(","));
            } else {
                this.liveReleasesPatterns = List.ofAll(LIVE_RELEASES_PATTERNS_DEFAULT);
                info("[CONFIG] 'live_releases_patterns' not configured. Using default patterns:");
                info("{}", LIVE_RELEASES_PATTERNS_DEFAULT);
            }
        } catch (final IOException ex) {
            error("[ERROR] Configuration error: {}", ex.getMessage(), ex);
        }
        return done;
    }
}
