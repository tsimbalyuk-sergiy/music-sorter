package dev.tsvinc.music.sort.domain;

import lombok.Builder;

public record Metadata(String genre, String format, String artist, int year, int audioFilesCount, boolean invalid) {
    @Builder
    public Metadata {}
}
