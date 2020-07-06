package dev.tsvinc.music.sort.domain;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class AppProperties {

  private String sourceFolder;
  private String targetFolder;
  private boolean sortByArtist;
  private boolean skipLiveReleases;
  private List<String> liveReleasesPatterns;
  private String dbLocation;
  private String dbUsername;
  private String dbPassword;

  public AppProperties(
      String sourceFolder,
      String targetFolder,
      boolean sortByArtist,
      boolean skipLiveReleases,
      List<String> liveReleasesPatterns,
      String dbLocation,
      String dbUsername,
      String dbPassword) {
    this.sourceFolder = sourceFolder;
    this.targetFolder = targetFolder;
    this.sortByArtist = sortByArtist;
    this.skipLiveReleases = skipLiveReleases;
    this.liveReleasesPatterns = liveReleasesPatterns;
    this.dbLocation = dbLocation;
    this.dbUsername = dbUsername;
    this.dbPassword = dbPassword;
  }

  public AppProperties() {}

  public static AppPropertiesBuilder builder() {
    return new AppPropertiesBuilder();
  }

  public String getSourceFolder() {
    return this.sourceFolder;
  }

  public void setSourceFolder(String sourceFolder) {
    this.sourceFolder = sourceFolder;
  }

  public String getTargetFolder() {
    return this.targetFolder;
  }

  public void setTargetFolder(String targetFolder) {
    this.targetFolder = targetFolder;
  }

  public boolean isSortByArtist() {
    return this.sortByArtist;
  }

  public void setSortByArtist(boolean sortByArtist) {
    this.sortByArtist = sortByArtist;
  }

  public boolean isSkipLiveReleases() {
    return this.skipLiveReleases;
  }

  public void setSkipLiveReleases(boolean skipLiveReleases) {
    this.skipLiveReleases = skipLiveReleases;
  }

  public List<String> getLiveReleasesPatterns() {
    return this.liveReleasesPatterns;
  }

  public void setLiveReleasesPatterns(List<String> liveReleasesPatterns) {
    this.liveReleasesPatterns = liveReleasesPatterns;
  }

  public String getDbLocation() {
    return dbLocation;
  }

  public void setDbLocation(String dbLocation) {
    this.dbLocation = dbLocation;
  }

  public String getDbUsername() {
    return dbUsername;
  }

  public void setDbUsername(String dbUsername) {
    this.dbUsername = dbUsername;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AppProperties that = (AppProperties) o;
    return sortByArtist == that.sortByArtist
        && skipLiveReleases == that.skipLiveReleases
        && Objects.equals(sourceFolder, that.sourceFolder)
        && Objects.equals(targetFolder, that.targetFolder)
        && Objects.equals(liveReleasesPatterns, that.liveReleasesPatterns)
        && Objects.equals(dbLocation, that.dbLocation)
        && Objects.equals(dbUsername, that.dbUsername)
        && Objects.equals(dbPassword, that.dbPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        sourceFolder,
        targetFolder,
        sortByArtist,
        skipLiveReleases,
        liveReleasesPatterns,
        dbLocation,
        dbUsername,
        dbPassword);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AppProperties.class.getSimpleName() + "[", "]")
        .add("sourceFolder='" + sourceFolder + "'")
        .add("targetFolder='" + targetFolder + "'")
        .add("sortByArtist=" + sortByArtist)
        .add("skipLiveReleases=" + skipLiveReleases)
        .add("liveReleasesPatterns=" + liveReleasesPatterns)
        .add("dbLocation='" + dbLocation + "'")
        .add("dbUsername='" + dbUsername + "'")
        .add("dbPassword='" + dbPassword + "'")
        .toString();
  }

  public static class AppPropertiesBuilder {

    private String sourceFolder;
    private String targetFolder;
    private boolean sortByArtist;
    private boolean skipLiveReleases;
    private List<String> liveReleasesPatterns;
    private String dbLocation;
    private String dbUsername;
    private String dbPassword;

    AppPropertiesBuilder() {}

    public AppProperties.AppPropertiesBuilder sourceFolder(String sourceFolder) {
      this.sourceFolder = sourceFolder;
      return this;
    }

    public AppProperties.AppPropertiesBuilder targetFolder(String targetFolder) {
      this.targetFolder = targetFolder;
      return this;
    }

    public AppProperties.AppPropertiesBuilder sortByArtist(boolean sortByArtist) {
      this.sortByArtist = sortByArtist;
      return this;
    }

    public AppProperties.AppPropertiesBuilder skipLiveReleases(boolean skipLiveReleases) {
      this.skipLiveReleases = skipLiveReleases;
      return this;
    }

    public AppProperties.AppPropertiesBuilder dbLocation(String dbLocation) {
      this.dbLocation = dbLocation;
      return this;
    }

    public AppProperties.AppPropertiesBuilder liveReleasesPatterns(
        List<String> liveReleasesPatterns) {
      this.liveReleasesPatterns = liveReleasesPatterns;
      return this;
    }

    public AppProperties.AppPropertiesBuilder dbUsername(String dbUsername) {
      this.dbUsername = dbUsername;
      return this;
    }

    public AppProperties.AppPropertiesBuilder dbPassword(String dbPassword) {
      this.dbPassword = dbPassword;
      return this;
    }

    public AppProperties build() {
      return new AppProperties(
          sourceFolder,
          targetFolder,
          sortByArtist,
          skipLiveReleases,
          liveReleasesPatterns,
          dbLocation,
          dbUsername,
          dbPassword);
    }
  }
}
