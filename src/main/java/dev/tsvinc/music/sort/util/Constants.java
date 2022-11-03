package dev.tsvinc.music.sort.util;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class Constants {

    public static final String MP3_FORMAT = "mp3";
    public static final String FLAC_FORMAT = "flac";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String MP3 = "*.mp3";
    public static final String FLAC = "*.flac";
    public static final String ERROR_CREATING_DIRECTORY = "Error creating directory: {}, {}";
    public static final String SOURCE_FOLDER = "source";
    public static final String TARGET_FOLDER = "target";
    public static final String LIVE_RELEASES_SKIP = "live_releases_skip";
    public static final String SORT_BY_ARTIST = "sort_by_artist";
    public static final String LIVE_RELEASES_PATTERNS = "live_releases_patterns";
    public static final List<String> LIVE_RELEASES_PATTERNS_DEFAULT =
            List.of("-SAT-", "-DVBS-", "-SBD-", "-DAB-", "-FM-", "-CABLE-", "-DVBC-", "-DVBT-", "-LINE-", "-STREAM-");
    public static final String CHECKSUM = "checksum";
    public static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
    public static final Pattern ZERO_TO_NINE_PATTERN = Pattern.compile(".*\\d.*");
    public static final Pattern LETTERS_NUMBERS_SPACES_PATTERN = Pattern.compile("[^A-Za-z0-9\\-\\s&]+");
    public static final Pattern VA_PATTERN = Pattern.compile("((VA)|(va))(-|_-).*");
    public static final Pattern DECIMAL_PATTERN = Pattern.compile("[^\\d.]");
    public static final String CONFIG_PATH = System.getProperty("user.home") + File.separator + ".config";
    public static final String APP_CONFIG_DIR_PATH = CONFIG_PATH + File.separator + "music-sorter";
    private Constants() {}
}
