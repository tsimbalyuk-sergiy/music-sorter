package dev.tsvinc.music.sort.domain;

import java.util.List;

public record AppProperties(
        String sourceFolder,
        String targetFolder,
        boolean sortByArtist,
        boolean skipLiveReleases,
        List<String> liveReleasesPatterns) {

    public static AppPropertiesBuilder builder() {
        return new AppPropertiesBuilder();
    }

    public static class AppPropertiesBuilder {

        private String sourceFolder;
        private String targetFolder;
        private boolean sortByArtist;
        private boolean skipLiveReleases;
        private List<String> liveReleasesPatterns;

        AppPropertiesBuilder() {}

        public AppPropertiesBuilder sourceFolder(String sourceFolder) {
            this.sourceFolder = sourceFolder;
            return this;
        }

        public AppPropertiesBuilder targetFolder(String targetFolder) {
            this.targetFolder = targetFolder;
            return this;
        }

        public AppPropertiesBuilder sortByArtist(boolean sortByArtist) {
            this.sortByArtist = sortByArtist;
            return this;
        }

        public AppPropertiesBuilder skipLiveReleases(boolean skipLiveReleases) {
            this.skipLiveReleases = skipLiveReleases;
            return this;
        }

        public AppPropertiesBuilder liveReleasesPatterns(List<String> liveReleasesPatterns) {
            this.liveReleasesPatterns = liveReleasesPatterns;
            return this;
        }

        public AppProperties build() {
            return new AppProperties(sourceFolder, targetFolder, sortByArtist, skipLiveReleases, liveReleasesPatterns);
        }

        public String toString() {
            return "AppProperties.AppPropertiesBuilder(sourceFolder=" + this.sourceFolder + ", targetFolder="
                    + this.targetFolder + ", sortByArtist=" + this.sortByArtist + ", skipLiveReleases="
                    + this.skipLiveReleases + ", liveReleasesPatterns=" + this.liveReleasesPatterns + ")";
        }
    }
}
