package dev.tsvinc.music.sort.domain;

import io.vavr.collection.List;

public record AppProperties(
        String sourceFolder,
        String targetFolder,
        boolean sortByArtist,
        boolean skipLiveReleases,
        List<String> liveReleasesPatterns,
        boolean checksumValidationEnabled) {

    public AppProperties withSourceFolder(String sourceFolder) {
        return new AppProperties(
                sourceFolder,
                targetFolder,
                sortByArtist,
                skipLiveReleases,
                liveReleasesPatterns,
                checksumValidationEnabled);
    }

    public AppProperties withTargetFolder(String targetFolder) {
        return new AppProperties(
                sourceFolder,
                targetFolder,
                sortByArtist,
                skipLiveReleases,
                liveReleasesPatterns,
                checksumValidationEnabled);
    }

    public AppProperties withSortByArtist(boolean sortByArtist) {
        return new AppProperties(
                sourceFolder,
                targetFolder,
                sortByArtist,
                skipLiveReleases,
                liveReleasesPatterns,
                checksumValidationEnabled);
    }
}
