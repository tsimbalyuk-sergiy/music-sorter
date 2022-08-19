package dev.tsvinc.music.sort.domain;

import java.util.List;
import java.util.Objects;

public class AppProperties {

    private String sourceFolder;
    private String targetFolder;
    private boolean sortByArtist;
    private boolean skipLiveReleases;
    private List<String> liveReleasesPatterns;
    private DbProperties dbProperties;

    public AppProperties(
            final String sourceFolder,
            final String targetFolder,
            final boolean sortByArtist,
            final boolean skipLiveReleases,
            final List<String> liveReleasesPatterns,
            final DbProperties dbProperties) {
        this.sourceFolder = sourceFolder;
        this.targetFolder = targetFolder;
        this.sortByArtist = sortByArtist;
        this.skipLiveReleases = skipLiveReleases;
        this.liveReleasesPatterns = liveReleasesPatterns;
        this.dbProperties = dbProperties;
    }

    public AppProperties() {}

    public static AppPropertiesBuilder builder() {
        return new AppPropertiesBuilder();
    }

    public String getSourceFolder() {
        return this.sourceFolder;
    }

    public void setSourceFolder(final String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public String getTargetFolder() {
        return this.targetFolder;
    }

    public void setTargetFolder(final String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public boolean isSortByArtist() {
        return this.sortByArtist;
    }

    public void setSortByArtist(final boolean sortByArtist) {
        this.sortByArtist = sortByArtist;
    }

    public boolean isSkipLiveReleases() {
        return this.skipLiveReleases;
    }

    public void setSkipLiveReleases(final boolean skipLiveReleases) {
        this.skipLiveReleases = skipLiveReleases;
    }

    public List<String> getLiveReleasesPatterns() {
        return this.liveReleasesPatterns;
    }

    public void setLiveReleasesPatterns(final List<String> liveReleasesPatterns) {
        this.liveReleasesPatterns = liveReleasesPatterns;
    }

    public DbProperties getDbProperties() {
        return this.dbProperties;
    }

    public void setDbProperties(final DbProperties dbProperties) {
        this.dbProperties = dbProperties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || this.getClass() != o.getClass()) {
            return false;
        }
        final var that = (AppProperties) o;
        return this.sortByArtist == that.sortByArtist
                && this.skipLiveReleases == that.skipLiveReleases
                && Objects.equals(this.sourceFolder, that.sourceFolder)
                && Objects.equals(this.targetFolder, that.targetFolder)
                && Objects.equals(this.liveReleasesPatterns, that.liveReleasesPatterns)
                && Objects.equals(this.dbProperties, that.dbProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.sourceFolder,
                this.targetFolder,
                this.sortByArtist,
                this.skipLiveReleases,
                this.liveReleasesPatterns,
                this.dbProperties);
    }

    @Override
    public String toString() {
        return "AppProperties{"
                + "sourceFolder='"
                + this.sourceFolder
                + '\''
                + ", targetFolder='"
                + this.targetFolder
                + '\''
                + ", sortByArtist="
                + this.sortByArtist
                + ", skipLiveReleases="
                + this.skipLiveReleases
                + ", liveReleasesPatterns="
                + this.liveReleasesPatterns
                + ", dbProperties="
                + this.dbProperties
                + '}';
    }

    public static class AppPropertiesBuilder {

        private String sourceFolder;
        private String targetFolder;
        private boolean sortByArtist;
        private boolean skipLiveReleases;
        private List<String> liveReleasesPatterns;
        private DbProperties dbProperties;

        AppPropertiesBuilder() {}

        public AppProperties.AppPropertiesBuilder sourceFolder(final String sourceFolder) {
            this.sourceFolder = sourceFolder;
            return this;
        }

        public AppProperties.AppPropertiesBuilder targetFolder(final String targetFolder) {
            this.targetFolder = targetFolder;
            return this;
        }

        public AppProperties.AppPropertiesBuilder sortByArtist(final boolean sortByArtist) {
            this.sortByArtist = sortByArtist;
            return this;
        }

        public AppProperties.AppPropertiesBuilder skipLiveReleases(final boolean skipLiveReleases) {
            this.skipLiveReleases = skipLiveReleases;
            return this;
        }

        public AppProperties.AppPropertiesBuilder dbProperties(final DbProperties dbProperties) {
            this.dbProperties = dbProperties;
            return this;
        }

        public AppProperties.AppPropertiesBuilder liveReleasesPatterns(final List<String> liveReleasesPatterns) {
            this.liveReleasesPatterns = liveReleasesPatterns;
            return this;
        }

        public AppProperties build() {
            return new AppProperties(
                    this.sourceFolder,
                    this.targetFolder,
                    this.sortByArtist,
                    this.skipLiveReleases,
                    this.liveReleasesPatterns,
                    this.dbProperties);
        }
    }
}
