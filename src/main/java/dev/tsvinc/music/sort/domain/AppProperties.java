package dev.tsvinc.music.sort.domain;

import java.util.List;
import lombok.Builder;

public record AppProperties(
        String sourceFolder,
        String targetFolder,
        boolean sortByArtist,
        boolean skipLiveReleases,
        List<String> liveReleasesPatterns) {

    @Builder
    public AppProperties {}
}
