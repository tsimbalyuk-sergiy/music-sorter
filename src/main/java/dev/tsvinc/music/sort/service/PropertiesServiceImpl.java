package dev.tsvinc.music.sort.service;

import dev.tsvinc.music.sort.domain.AppProperties;
import dev.tsvinc.music.sort.util.LiveReleasesPatterns;
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

import static dev.tsvinc.music.sort.util.Constants.ERROR_CREATING_DIRECTORY;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_PATTERNS;
import static dev.tsvinc.music.sort.util.Constants.LIVE_RELEASES_SKIP;
import static dev.tsvinc.music.sort.util.Constants.SORT_BY_ARTIST;
import static dev.tsvinc.music.sort.util.Constants.SOURCE_FOLDER;
import static dev.tsvinc.music.sort.util.Constants.TARGET_FOLDER;
import static org.tinylog.Logger.error;
import static org.tinylog.Logger.info;

public class PropertiesServiceImpl implements PropertiesService {

    private static final String CONFIG_PATH = System.getProperty("user.home") + File.separator + ".config";
    private static final String APP_CONFIG_DIR_PATH =
            PropertiesServiceImpl.CONFIG_PATH + File.separator + "music-sorter";
    private final String appPropertiesLocation = PropertiesServiceImpl.appPropertiesLocation();
    private boolean skipLiveReleases;
    private boolean sortByArtist;

    private List<String> liveReleasesPatterns;

    private String sourceFolderValue;
    private String targetFolderValue;

    @Override
    public boolean initProperties() {
        final var configPathExists = new File(PropertiesServiceImpl.CONFIG_PATH).exists();
        if (!configPathExists) {
            Try.of(() -> Files.createDirectory(Paths.get(PropertiesServiceImpl.CONFIG_PATH)))
                    .onFailure(
                            e -> error(ERROR_CREATING_DIRECTORY, PropertiesServiceImpl.CONFIG_PATH, e.getMessage(), e));
        }
        final var appConfigPathExists = new File(PropertiesServiceImpl.APP_CONFIG_DIR_PATH).exists();
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
            error("Error loading properties");
            System.exit(-1);
        }
        return AppProperties.builder()
                .sourceFolder(this.sourceFolderValue)
                .targetFolder(this.targetFolderValue)
                .liveReleasesPatterns(this.liveReleasesPatterns)
                .skipLiveReleases(this.skipLiveReleases)
                .sortByArtist(this.sortByArtist)
                .build();
    }

    private void createPropertiesExample() {
        Try.run(() -> {
                    Files.createFile(Paths.get(this.appPropertiesLocation));
                    Files.write(
                            Paths.get(this.appPropertiesLocation),
                            Arrays.asList("source=", "target="),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.APPEND);
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
            if (this.skipLiveReleases && prop.containsKey(LIVE_RELEASES_PATTERNS)) {
                this.liveReleasesPatterns =
                        Arrays.asList(prop.getProperty(LIVE_RELEASES_PATTERNS).split(","));
            } else {
                this.liveReleasesPatterns = Arrays.stream(LiveReleasesPatterns.values())
                        .map(LiveReleasesPatterns::getPattern)
                        .toList();
                info("\"live_releases_patterns\" option is empty. using defaults: ");
                info("{}", liveReleasesPatterns);
            }
        } catch (final IOException ex) {
            error("Error loading properties: {}", ex.getMessage(), ex);
        }
        return done;
    }
}
