package dev.tsvinc.music.sort.domain;

import java.util.List;

public record AppProperties(
        String sourceFolder,
        String targetFolder,
        boolean sortByArtist,
        boolean skipLiveReleases,
        List<String> liveReleasesPatterns) {}
