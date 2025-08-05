package dev.tsvinc.music.sort.domain;

public record Metadata(String genre, String format, String artist, int year, int audioFilesCount, boolean invalid) {

    public static Metadata createInvalid() {
        return new Metadata(null, null, null, 0, 0, true);
    }

    public boolean isValid() {
        return !invalid;
    }

    public Metadata withGenre(String genre) {
        return new Metadata(genre, format, artist, year, audioFilesCount, invalid);
    }

    public Metadata withArtist(String artist) {
        return new Metadata(genre, format, artist, year, audioFilesCount, invalid);
    }

    public Metadata withYear(int year) {
        return new Metadata(genre, format, artist, year, audioFilesCount, invalid);
    }

    public Metadata withFormat(String format) {
        return new Metadata(genre, format, artist, year, audioFilesCount, invalid);
    }
}
