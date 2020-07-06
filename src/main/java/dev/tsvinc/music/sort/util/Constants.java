package dev.tsvinc.music.sort.util;

import java.util.List;

public class Constants {

  public static final String MP_3_FORMAT = "mp3";
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
  public static final List<String> LIVE_RELEASES_PATTERNS_DEFAULT = List
      .of("-SAT-", "-DVBS-", "-SBD-", "-DAB-", "-FM-", "-CABLE-", "-DVBC-", "-DVBT-", "-LINE-", "-STREAM-");
  public static final String NITRITE_SINGLETON = "NITRITE_SINGLETON";
  public static final String DB_LOCATION = "db_location";
  public static final String DB_DEFAULT_FILE_NAME = "nitrite.db";
  public static final String DB_DEFAULT_USERNAME = "nitrite";
  public static final String DB_DEFAULT_PASSWORD = "nitrite";
  public static final String DB_PASSWORD = "db_password";
  public static final String DB_USERNAME = "db_username";
  private Constants() {
  }
}
