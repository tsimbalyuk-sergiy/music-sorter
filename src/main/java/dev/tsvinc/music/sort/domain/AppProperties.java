package dev.tsvinc.music.sort.domain;

import lombok.Builder;

import java.util.List;

public record AppProperties(
        String sourceFolder,
        String targetFolder,
        boolean sortByArtist,
        boolean skipLiveReleases,
        List<String> liveReleasesPatterns) {

    @SuppressWarnings({"java:S6207", "java:S1186"})
    @Builder
    public AppProperties {
        // need this to make the builder work without loads of boilerplate
    }
}
