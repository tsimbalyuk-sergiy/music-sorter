package dev.tsvinc.music.sort.domain;

import java.util.List;
import java.util.Objects;

public class AppProperties {
  private String sourceFolder;
  private String targetFolder;
  private boolean sortByArtist;
  private boolean skipLiveReleases;
  private List<String> liveReleasesPatterns;

  public AppProperties(
      String sourceFolder,
      String targetFolder,
      boolean sortByArtist,
      boolean skipLiveReleases,
      List<String> liveReleasesPatterns) {
    this.sourceFolder = sourceFolder;
    this.targetFolder = targetFolder;
    this.sortByArtist = sortByArtist;
    this.skipLiveReleases = skipLiveReleases;
    this.liveReleasesPatterns = liveReleasesPatterns;
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
        && Objects.equals(liveReleasesPatterns, that.liveReleasesPatterns);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        sourceFolder, targetFolder, sortByArtist, skipLiveReleases, liveReleasesPatterns);
  }

  public String toString() {
    return "AppProperties(sourceFolder="
        + this.getSourceFolder()
        + ", targetFolder="
        + this.getTargetFolder()
        + ", sortByArtist="
        + this.isSortByArtist()
        + ", skipLiveReleases="
        + this.isSkipLiveReleases()
        + ", liveReleasesPatterns="
        + this.getLiveReleasesPatterns()
        + ")";
  }

  public static class AppPropertiesBuilder {

    private String sourceFolder;
    private String targetFolder;
    private boolean sortByArtist;
    private boolean skipLiveReleases;
    private List<String> liveReleasesPatterns;

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

    public AppProperties.AppPropertiesBuilder liveReleasesPatterns(
        List<String> liveReleasesPatterns) {
      this.liveReleasesPatterns = liveReleasesPatterns;
      return this;
    }

    public AppProperties build() {
      return new AppProperties(
          sourceFolder, targetFolder, sortByArtist, skipLiveReleases, liveReleasesPatterns);
    }

    public String toString() {
      return "AppProperties.AppPropertiesBuilder(sourceFolder="
          + this.sourceFolder
          + ", targetFolder="
          + this.targetFolder
          + ", sortByArtist="
          + this.sortByArtist
          + ", skipLiveReleases="
          + this.skipLiveReleases
          + ", liveReleasesPatterns="
          + this.liveReleasesPatterns
          + ")";
    }
  }
}
